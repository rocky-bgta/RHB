package com.rhbgroup.dcp.bo.batch.framework.common.tasklet;

import com.jaspersoft.jasperserver.dto.serverinfo.ServerInfo;
import com.jaspersoft.jasperserver.jaxrs.client.core.Session;
import com.rhbgroup.dcp.bo.batch.framework.constants.BatchErrorCode;
import com.rhbgroup.dcp.bo.batch.framework.exception.BatchException;
import com.rhbgroup.dcp.bo.batch.framework.jasper.JasperClientConfigProperties;
import com.rhbgroup.dcp.bo.batch.framework.jasper.JasperReportConfig;
import com.rhbgroup.dcp.bo.batch.framework.jasper.JasperReportConfig.ReportConfig;
import com.rhbgroup.dcp.bo.batch.framework.utils.BatchUtils;
import com.rhbgroup.dcp.bo.batch.framework.utils.BeanUtils;
import com.rhbgroup.dcp.bo.batch.framework.utils.DateUtils;
import com.rhbgroup.dcp.bo.batch.framework.utils.JasperClientUtils;
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

import java.io.File;
import java.io.InputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter.BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter.BATCH_JOB_PARAMETER_REPORT_ID_KEY;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter.BATCH_JOB_PARAMETER_JOB_BATCH_PROCESS_START_DATE_KEY;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter.BATCH_JOB_PARAMETER_JOB_BATCH_PROCESS_END_DATE_KEY;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.General.DEFAULT_DATE_FORMAT;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.ReportFolder.REPORT_FOLDER_EXPORT_DIRECTORY;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.ReportJobContextParameter.*;
import static org.apache.commons.lang3.time.DateUtils.*;

@Component
@Lazy
public class RunReportWithDateRangeTasklet implements Tasklet, InitializingBean {
	
	private static final String DEFAULT_PROCESS_START_DATE_PARAM_KEY = "start_date";
	private static final String DEFAULT_PROCESS_END_DATE_PARAM_KEY = "end_date";
	private static final String REPORT_BILLER_CODE_PARAM_KEY = "biller_code";
	private static final Object BATCH_PROCESS_DATE = "batchProcessDate";
	private static final String START_DATE = "startDate";
	private static final String END_DATE = "endDate";
	private static final String START_DATE_STR = "startDateStr";
	private static final String END_DATE_STR = "endDateStr";

    private static final Logger logger = Logger.getLogger(RunReportWithDateRangeTasklet.class);
    
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
    		String batchSystemDateStr=(String)chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().get(BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY);
    		if(batchSystemDateStr == null) {
                throw new BatchException(BatchErrorCode.GENERIC_SYSTEM_ERROR, "Missing mandatory batch parameter");
            }

			Date batchProcessDate=addMonths(BatchUtils.getProcessingDate(batchSystemDateStr, DEFAULT_DATE_FORMAT),-1);
			
			Map<Object, Object> dates = getStartAndEndDate(chunkContext, batchProcessDate);
			
			if(dates.containsKey(BATCH_PROCESS_DATE)){
				batchProcessDate = (Date) dates.get(BATCH_PROCESS_DATE);
			}
			Date startDate = (Date) dates.get(START_DATE);
			Date endDate = (Date) dates.get(END_DATE);
			String startDateStr = (String) dates.get(START_DATE_STR);
			String endDateStr = (String) dates.get(START_DATE_STR);
		         
            JasperReportConfig.ReportConfig reportConfig = getReportConfig(chunkContext, startDateStr, endDateStr);
           
            logger.info(String.format("Report Config Loaded: %s", BeanUtils.toStringUsingJackson(reportConfig) ));
            if(chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().get(REPORT_UNIT_URI) != null) {
        		String reportUnitUri = chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().getString(REPORT_UNIT_URI);
                if(StringUtils.isNotEmpty(reportUnitUri)) {
                	logger.info(String.format("Context found for Report Unit URI. Overriding config parameter from context.. [%s]",reportUnitUri));
                	reportConfig.setUri(reportUnitUri);
                }
            }
    	    reportConfig.addParameter(DEFAULT_PROCESS_START_DATE_PARAM_KEY, startDateStr); //TO-DO: param name to be configurable / by convention
			reportConfig.addParameter(DEFAULT_PROCESS_END_DATE_PARAM_KEY, endDateStr);
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
				String reportFileName = "/" + BatchUtils.generateSourceFileName(fileName, batchProcessDate);
				logger.info(String.format("Writing report file.. [%s]", reportFileName));
				File targetFile = new File(targetFilePath, reportFileName);
				FileUtils.copyInputStreamToFile(inputStream, targetFile);
				reportFileFullPathList.add(targetFile.getAbsolutePath());
			}

			logger.info("Writing report file completed");
			chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().put( REPORT_JOB_PARAMETER_REPORT_FILES_PATH_KEY, reportFileFullPathList);

			// Context for BillerPaymentFileJob
			String firstFileName = reportConfig.getFilename() + reportConfig.getJasperReportOutputFormat(reportConfig.getFileFormats()[0]).fileExtension;
			String firstReportFileName = "/" + BatchUtils.generateSourceFileName(firstFileName, batchProcessDate);
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

	private Map<Object, Object> getStartAndEndDate(ChunkContext chunkContext, Date batchProcessDate) throws ParseException, BatchException {
		Map<Object, Object> dates = new HashMap<>();
		Date startDate = null;
		Date endDate = null;
		String startDateStr = null;
		String endDateStr = null;
			
		 // startdate and enddate job parameter found
        if (chunkContext.getStepContext().getJobParameters().containsKey(BATCH_JOB_PARAMETER_JOB_BATCH_PROCESS_START_DATE_KEY)
				&& chunkContext.getStepContext().getJobParameters().containsKey(BATCH_JOB_PARAMETER_JOB_BATCH_PROCESS_END_DATE_KEY)){
        	startDateStr = chunkContext.getStepContext().getJobParameters().get(BATCH_JOB_PARAMETER_JOB_BATCH_PROCESS_START_DATE_KEY).toString();
        	endDateStr = chunkContext.getStepContext().getJobParameters().get(BATCH_JOB_PARAMETER_JOB_BATCH_PROCESS_END_DATE_KEY).toString();
        	batchProcessDate = BatchUtils.getProcessingDate(endDateStr, DEFAULT_DATE_FORMAT);
        	dates.put(BATCH_PROCESS_DATE, batchProcessDate);
		// Both startdate and enddate job parameter not found
		}else if (!(chunkContext.getStepContext().getJobParameters().containsKey(BATCH_JOB_PARAMETER_JOB_BATCH_PROCESS_START_DATE_KEY)
				|| chunkContext.getStepContext().getJobParameters().containsKey(BATCH_JOB_PARAMETER_JOB_BATCH_PROCESS_END_DATE_KEY))){
        	startDate = truncate(batchProcessDate, Calendar.MONTH);
			endDate = addDays(ceiling(batchProcessDate, Calendar.MONTH),-1);
			startDateStr = DateUtils.formatDateString(startDate, DEFAULT_DATE_FORMAT);
			endDateStr = DateUtils.formatDateString(endDate, DEFAULT_DATE_FORMAT);

		// Either startdate or enddate job parameter not found
		}else{
        	String errorMessage = ": Either startdate or enddate job parameter not found";
        	throw new BatchException(BatchErrorCode.FIELD_VALIDATION_ERROR, BatchErrorCode.FIELD_VALIDATION_ERROR_MESSAGE +errorMessage);
		}
                
        dates.put(START_DATE, startDate);
        dates.put(END_DATE, endDate);
        dates.put(START_DATE_STR, startDateStr);
        dates.put(END_DATE_STR, endDateStr);
        
		return dates;
	}

	private ReportConfig getReportConfig(ChunkContext chunkContext, String startDateStr, String endDateStr) throws BatchException {
		String reportID=chunkContext.getStepContext().getStepExecution().getJobExecution().getJobParameters().getString(BATCH_JOB_PARAMETER_REPORT_ID_KEY);
        if(null == reportID) {
        	reportID=chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().getString(BATCH_JOB_PARAMETER_REPORT_ID_KEY);
        }
        if(StringUtils.isEmpty(reportID)) {
	    	throw new BatchException(BatchErrorCode.GENERIC_SYSTEM_ERROR, "Missing mandatory batch parameter");
        }
        
        logger.info(String.format("Initiating Report Batch Task.. [ReportID:%s, ProcessDate:%s to %s]",reportID, startDateStr, endDateStr));
        logger.info(String.format("Getting Report Config.. [%s]",reportID ));
        JasperReportConfig.ReportConfig reportConfig = jasperReportConfig.getReportConfigById(reportID);
        if(reportConfig == null) {
	    	throw new BatchException(BatchErrorCode.GENERIC_SYSTEM_ERROR, "Missing report config");
        }
        
		return reportConfig;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		// Do nothing because this is the implementation of Spring Batch
	}
}
