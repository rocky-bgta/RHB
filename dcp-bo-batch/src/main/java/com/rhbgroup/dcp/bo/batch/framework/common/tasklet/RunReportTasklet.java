package com.rhbgroup.dcp.bo.batch.framework.common.tasklet;

import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter.BATCH_JOB_PARAMETER_REPORT_ID_KEY;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.General.DEFAULT_DATE_FORMAT;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.ReportFolder.REPORT_FOLDER_EXPORT_DIRECTORY;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.ReportJobContextParameter.*;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.jaspersoft.jasperserver.dto.serverinfo.ServerInfo;
import com.jaspersoft.jasperserver.jaxrs.client.core.Session;
import com.rhbgroup.dcp.bo.batch.framework.constants.BatchErrorCode;
import com.rhbgroup.dcp.bo.batch.framework.exception.BatchException;
import com.rhbgroup.dcp.bo.batch.framework.jasper.JasperClientConfigProperties;
import com.rhbgroup.dcp.bo.batch.framework.jasper.JasperReportConfig;
import com.rhbgroup.dcp.bo.batch.framework.utils.BatchUtils;
import com.rhbgroup.dcp.bo.batch.framework.utils.BeanUtils;
import com.rhbgroup.dcp.bo.batch.framework.utils.DateUtils;
import com.rhbgroup.dcp.bo.batch.framework.utils.JasperClientUtils;

@Component
@Lazy
public class RunReportTasklet implements Tasklet, InitializingBean {
	
	private static final String DEFAULT_PROCESS_DATE_PARAM_KEY = "date";
	private static final String REPORT_BILLER_CODE_PARAM_KEY = "biller_code";
	private static final String DELIMITER_SLASH = "/";
    private static final Logger logger = Logger.getLogger(RunReportTasklet.class);
    
    @Value(REPORT_FOLDER_EXPORT_DIRECTORY)
    private String reportExportPath;
    
    @Autowired
    private JasperClientConfigProperties jasperClientConfigProperties;
    
    @Autowired
    private JasperReportConfig jasperReportConfig;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
    	Session session = null;
    	try {

    		// Get batch processing date
			Date batchProcessDate = DateUtils.getBatchProcessingDate(chunkContext);
			String batchProcessDateStr = DateUtils.formatDateString(batchProcessDate, DEFAULT_DATE_FORMAT);

            String reportID=chunkContext.getStepContext().getStepExecution().getJobExecution().getJobParameters().getString(BATCH_JOB_PARAMETER_REPORT_ID_KEY);
            if(null == reportID) {
            	reportID=chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().getString(BATCH_JOB_PARAMETER_REPORT_ID_KEY);
            }
            if(StringUtils.isEmpty(reportID)) {
    	    	throw new BatchException(BatchErrorCode.GENERIC_SYSTEM_ERROR, "Missing mandatory batch parameter");
            }
            logger.info(String.format("Initiating Report Batch Task.. [ReportID:%s, ProcessDate:%s]",reportID, batchProcessDateStr));
            logger.info(String.format("Getting Report Config.. [%s]",reportID ));
            JasperReportConfig.ReportConfig reportConfig = jasperReportConfig.getReportConfigById(reportID);
            if(reportConfig == null) {
    	    	throw new BatchException(BatchErrorCode.GENERIC_SYSTEM_ERROR, "Missing report config");
            }
            logger.info(String.format("Report Config Loaded: %s", BeanUtils.toStringUsingJackson(reportConfig) ));
            if(chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().get(REPORT_UNIT_URI) != null) {
        		String reportUnitUri = chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().getString(REPORT_UNIT_URI);
                if(StringUtils.isNotEmpty(reportUnitUri)) {
                	logger.info(String.format("Context found for Report Unit URI. Overriding config parameter from context.. [%s]",reportUnitUri));
                	reportConfig.setUri(reportUnitUri);
                }
            }
    	    reportConfig.addParameter(DEFAULT_PROCESS_DATE_PARAM_KEY, batchProcessDateStr); //TO-DO: param name to be configurable / by convention
    	    //Section start for implementation specific : RunReportJob
    	    if(StringUtils.isNotEmpty(reportConfig.getTargetpath())) {
    	    	chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().put( REPORT_JOB_PARAMETER_REPORT_TARGET_PATH_KEY, reportConfig.getTargetpath() );
    	    }
    	    //Section end for implementation specific : RunReportJob
    	    //Section start for implementation specific : BillerPaymentFileJob
            if(chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().get(REPORT_BILLER_CODE) != null) {
	    	    String billerCode=chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().getString(REPORT_BILLER_CODE);
	    	    if(StringUtils.isNotEmpty(billerCode)) {
	    	    	logger.info(String.format("Context found for Biller Payment Job. Adding parameter for Billing Code.. [%s]",billerCode));
	    	    	reportConfig.addParameter(REPORT_BILLER_CODE_PARAM_KEY, billerCode);
	    	    }
            }
    	    //Section end for implementation specific : BillerPaymentFileJob
    		logger.info(String.format("Initiating session to Jasper Server.. [%s]", jasperClientConfigProperties.getUrl()));
    		session = JasperClientUtils.initSession(jasperClientConfigProperties);
    		logger.info("Session initiated. Getting server info..");
    		ServerInfo serverInfo = JasperClientUtils.getServerInfo(session);
    		logger.info(String.format("Connected to Jasper Server [version:%s, edition:%s]",serverInfo.getVersion(),serverInfo.getEdition()));
			String targetFilePath = StringUtils.replace(reportExportPath, "\\", File.separator);

			/*  Copy report files (all extension types) from Jasper to export folder.
			 *  reportFileFullPathList stores all file path (multiple file formats) and put into context.
			 */
			ArrayList<String> reportFileFullPathList= new ArrayList<>();

			for (String format: reportConfig.getFileFormats()) {
				logger.info(String.format("Submitting Run Report request.. [reportUri:%s, format:%s]",reportConfig.getUri(),reportConfig.getJasperReportOutputFormat(format).toString()));
				InputStream inputStream = JasperClientUtils.runReport(session, reportConfig, format);
				logger.info("Report Run Completed");
				String fileName = reportConfig.getFilename() + reportConfig.getJasperReportOutputFormat(format).fileExtension;
				String reportFileName = DELIMITER_SLASH + BatchUtils.generateSourceFileName(fileName, batchProcessDate);
				logger.info(String.format("Writing report file.. [%s]", reportFileName));
				File targetFile = new File(targetFilePath, reportFileName);
				FileUtils.copyInputStreamToFile(inputStream, targetFile);
				reportFileFullPathList.add(targetFile.getAbsolutePath());
			}

			logger.info("Writing report file completed");
			chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().put( REPORT_JOB_PARAMETER_REPORT_FILES_PATH_KEY, reportFileFullPathList);

			// Context for BillerPaymentFileJob
			String firstFileName = reportConfig.getFilename() + reportConfig.getJasperReportOutputFormat(reportConfig.getFileFormats()[0]).fileExtension;
			String firstReportFileName = DELIMITER_SLASH + BatchUtils.generateSourceFileName(firstFileName, batchProcessDate);
			File firstTargetFile = new File(targetFilePath, firstReportFileName);
			chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().put( REPORT_JOB_PARAMETER_REPORT_FILE_PATH_KEY, firstTargetFile.getAbsolutePath());
    	}catch(BatchException ex) {
    		throw ex;
    	}catch(Exception ex) {
    		logger.error(ex);
	    	throw new BatchException(BatchErrorCode.GENERIC_SYSTEM_ERROR, "Batch error during RunReport", ex);
    	}finally {
    		if(session != null) {
    			session.logout();
    		}
    	}
        return RepeatStatus.FINISHED;
    }

	@Override
	public void afterPropertiesSet() throws Exception {
		// Do nothing because this is the implementation of Spring Batch
	}
}
