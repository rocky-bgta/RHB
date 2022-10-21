package com.rhbgroup.dcp.bo.batch.framework.common.tasklet;

import com.rhbgroup.dcp.bo.batch.framework.config.properties.FTPConfigProperties;
import com.rhbgroup.dcp.bo.batch.framework.constants.BatchErrorCode;
import com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.FTP;
import com.rhbgroup.dcp.bo.batch.framework.exception.BatchException;
import com.rhbgroup.dcp.bo.batch.framework.exception.BatchValidationException;
import com.rhbgroup.dcp.bo.batch.framework.utils.DateUtils;
import com.rhbgroup.dcp.bo.batch.framework.utils.FTPUtils;
import com.rhbgroup.dcp.bo.batch.job.config.properties.AsnbFtpConfigProperties;
import com.rhbgroup.dcp.bo.batch.job.config.properties.HostFtpConfigProperties;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import java.text.ParseException;
import java.util.ArrayList;

import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobContextParameter.BATCH_JOB_CONTEXT_PARAMETER_TEMP_OUT_FILE_FULL_PATH_KEY;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter.*;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobPropertyFile.BATCH_JOB_PROPERTY_FILE_NAME_DATETIME_PLACEHOLDER;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.General.DEFAULT_DATE_FORMAT;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.ReportJobContextParameter.REPORT_JOB_PARAMETER_REPORT_FILES_PATH_KEY;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.ReportJobContextParameter.REPORT_JOB_PARAMETER_REPORT_FILE_PATH_KEY;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.ReportJobContextParameter.REPORT_JOB_PARAMETER_REPORT_FILE_SOURCE_PATH_KEY;

@Component
@Lazy
public class CopyOutputFolderFileToFTPTasklet implements Tasklet{

    private static final Logger logger = Logger.getLogger(CopyOutputFolderFileToFTPTasklet.class);
    private static final String ASNBREPORTJOB = "AsnbReportJob";
    
    
    private String targetFileFolderPath;
    private String targetFileName;
    private String targetFileDateFormat;
    private String outputFolder;
    private String sourceFolder;
    private String targetFilePathContextKey;
    private boolean asnbCheck;
    private String hostFolder;

    @Autowired
    private FTPConfigProperties ftpConfigProperties;
    
    @Autowired
    private AsnbFtpConfigProperties asnbFtpConfigProperties;
    
    @Autowired
    private HostFtpConfigProperties hostFtpConfigProperties;

    public void init(String targetFileFolderPath, String targetFileName, String targetFileDateFormat) {
        this.targetFileFolderPath = targetFileFolderPath;
        this.targetFileName = targetFileName;
        this.targetFileDateFormat = targetFileDateFormat;
    }
    
    public void init(String targetFileFolderPath, String targetFileName, String targetFileDateFormat, String outputFolder, String sourceFolder, boolean asnbCheck, String hostFolder) {
        this.targetFileFolderPath = targetFileFolderPath;
        this.targetFileName = targetFileName;
        this.targetFileDateFormat = targetFileDateFormat;
        this.outputFolder = outputFolder;
        this.sourceFolder = sourceFolder;
        this.asnbCheck = asnbCheck;
        this.hostFolder = hostFolder;
    }

    public void initFTPConfig(FTPConfigProperties ftpConfigProperties) {
        this.ftpConfigProperties = ftpConfigProperties;
    }
    
    public void initFTPConfig(AsnbFtpConfigProperties asnbFtpConfigProperties) {
        this.asnbFtpConfigProperties = asnbFtpConfigProperties;
    }
    
    public void initFTPConfig(HostFtpConfigProperties hostFtpConfigProperties) {
        this.hostFtpConfigProperties = hostFtpConfigProperties;
    }

    public void initByContext(String targetFilePathContextKey) {
        this.targetFilePathContextKey = targetFilePathContextKey;
    }

    @Override
    public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws BatchException, ParseException {
        logger.info(String.format("Executing tasklet [%s]", this.getClass().getSimpleName()));

        
		if (asnbCheck) {
			String date = DateUtils.getProcessDate(chunkContext);
			logger.info("Date to be process is ::" + date+"    and Path is  ::::::"+sourceFolder + "/"+ASNBREPORTJOB);
			processUploadFiletoFTP(sourceFolder + "/" + ASNBREPORTJOB, outputFolder, asnbFtpConfigProperties, date);
			processUploadPostingFiletoFTP(sourceFolder + "/" + ASNBREPORTJOB, hostFolder, hostFtpConfigProperties);
			return RepeatStatus.FINISHED;
		} else {
			String targetFileFullPath = null;
			String sourceFileFullPath = null;
			String targetDateStr = null;

			// Get the system date from context
			String batchSystemDateStr = (String) chunkContext.getStepContext().getStepExecution().getJobExecution()
					.getExecutionContext().get(BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY);

			if (StringUtils.isEmpty(batchSystemDateStr)) {
				throw new BatchException(BatchErrorCode.CONFIG_SYSTEM_ERROR, "Missing mandatory batch job parameter");
			}

			logger.info(String.format("Initiating Copy to FTP Batch Task.. [BatchSystemDate:%s]", batchSystemDateStr));
			
			sourceFileFullPath = getSourceFileFullPath(chunkContext);	

			boolean isExtractBNMSubs = chunkContext.getStepContext().getStepExecution().getJobExecution()
					.getJobParameters().getString(BATCH_JOB_PARAMETER_JOB_NAME_KEY)
					.equalsIgnoreCase(BATCH_JOB_PARAMETER_EXTRACT_MONTHLY_SUBS_JOB);
			if (!(chunkContext.getStepContext().getStepExecution().getJobExecution().getJobParameters()
					.getString(BATCH_JOB_PARAMETER_JOB_NAME_KEY).equalsIgnoreCase(BATCH_JOB_PARAMETER_RUN_REPORT_JOB)
					|| chunkContext.getStepContext().getStepExecution().getJobExecution().getJobParameters()
							.getString(BATCH_JOB_PARAMETER_JOB_NAME_KEY)
							.equalsIgnoreCase(BATCH_JOB_PARAMETER_RUN_REPORT_WITH_DATE_RANGE_JOB)
					|| chunkContext.getStepContext().getStepExecution().getJobExecution().getJobParameters()
							.getString(BATCH_JOB_PARAMETER_JOB_NAME_KEY)
							.equalsIgnoreCase(BATCH_JOB_PARAMETER_AUDIT_LOG_REPORT_JOB)
					|| isExtractBNMSubs)) {
				targetDateStr = DateUtils.convertDateFormat(batchSystemDateStr, DEFAULT_DATE_FORMAT,
						targetFileDateFormat);
				targetFileFullPath = this.targetFileName.replace(BATCH_JOB_PROPERTY_FILE_NAME_DATETIME_PLACEHOLDER,
						targetDateStr);
				StringBuilder strBuilder = new StringBuilder(targetFileFolderPath);
				targetFileFullPath = strBuilder.append(FTP.FTP_SEPARATOR).append(targetFileFullPath).toString();

				processUploadFiletoFTP(sourceFileFullPath, targetFileFullPath, ftpConfigProperties);
			} else {
				targetFileFullPath = getTargetFileFullPath(chunkContext, isExtractBNMSubs, batchSystemDateStr);

				if (StringUtils.isEmpty(targetFileFullPath)) {
					throw new BatchException(BatchErrorCode.CONFIG_SYSTEM_ERROR,
							"Target filename yet to be initialized");
				}

				ArrayList<String> reportFileFullPathList = (ArrayList<String>) chunkContext.getStepContext()
						.getStepExecution().getJobExecution().getExecutionContext()
						.get(REPORT_JOB_PARAMETER_REPORT_FILES_PATH_KEY);
				for (String reportFilePath : reportFileFullPathList) {
					sourceFileFullPath = reportFilePath;
					processUploadFiletoFTP(sourceFileFullPath, targetFileFullPath, ftpConfigProperties);
				}
			}

			chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext()
					.put(REPORT_JOB_PARAMETER_REPORT_FILE_SOURCE_PATH_KEY, sourceFileFullPath);
			logger.info(String.format("Source path key %s",
					chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext()
							.getString(REPORT_JOB_PARAMETER_REPORT_FILE_SOURCE_PATH_KEY)));

			return RepeatStatus.FINISHED;

		}
        
    }
    
    public String getSourceFileFullPath(ChunkContext chunkContext) throws BatchException {
    	String sourceFileFullPath = null;
    	if (chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext()
				.containsKey(BATCH_JOB_CONTEXT_PARAMETER_TEMP_OUT_FILE_FULL_PATH_KEY)) {
			sourceFileFullPath = chunkContext.getStepContext().getStepExecution().getJobExecution()
					.getExecutionContext().getString(BATCH_JOB_CONTEXT_PARAMETER_TEMP_OUT_FILE_FULL_PATH_KEY);
		} else {
			if (chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext()
					.get(REPORT_JOB_PARAMETER_REPORT_FILE_PATH_KEY) != null) {
				sourceFileFullPath = chunkContext.getStepContext().getStepExecution().getJobExecution()
						.getExecutionContext().getString(REPORT_JOB_PARAMETER_REPORT_FILE_PATH_KEY);

				if (StringUtils.isEmpty(sourceFileFullPath)) {
					throw new BatchException(BatchErrorCode.CONFIG_SYSTEM_ERROR,
							"Job Context output file path not found");
				}
			}
		}
		return sourceFileFullPath;
    }
    
    public String getTargetFileFullPath(ChunkContext chunkContext, boolean isExtractBNMSubs, String batchSystemDateStr) throws BatchException, ParseException {
    	String targetFileFullPath = null;
    	  if (StringUtils.isEmpty(targetFileName)) {
              if (StringUtils.isEmpty(targetFilePathContextKey)) {
                  throw new BatchException(BatchErrorCode.CONFIG_SYSTEM_ERROR, "Target path context yet to be initialized");
              } else {
                  if (chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().get(targetFilePathContextKey) == null) {
                      throw new BatchException(BatchErrorCode.CONFIG_SYSTEM_ERROR, "Target path context is empty");
                  } else {
                      targetFileFullPath = chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().getString(targetFilePathContextKey);
                      if(!isExtractBNMSubs) {
                          targetFileFullPath += "/";
                      }
                  }
              }
          } else {
              if (StringUtils.isNotEmpty(targetFileDateFormat)) {
                  targetFileFullPath = this.targetFileName.replace(BATCH_JOB_PROPERTY_FILE_NAME_DATETIME_PLACEHOLDER, DateUtils.convertDateFormat(batchSystemDateStr, DEFAULT_DATE_FORMAT, targetFileDateFormat));
              }
          }
    	 return targetFileFullPath;
    }


    public boolean processUploadFiletoFTP(String sourceFileFullPath,String targetFileFullPath,FTPConfigProperties ftpConfigProperties) throws BatchException {
        try{
            logger.info(String.format("Preparing file to copy.. [%s]", sourceFileFullPath));
            logger.info(String.format("Preparing target file.. [%s]", targetFileFullPath));
            logger.info(String.format("Preparing target FTP.. [%s:%s]", ftpConfigProperties.getHost(), ftpConfigProperties.getPort()));

            FTPUtils.uploadFileToFTP(sourceFileFullPath, targetFileFullPath, ftpConfigProperties, asnbCheck);

            logger.info(String.format("Tasklet [%s] executed successfully", this.getClass().getSimpleName()));

            return true;
        } catch (BatchValidationException ex) {
            String errorMessage = String.format("Failed to upload file [%s] to target FTP folder [%s] using FTP [%s:%s]", sourceFileFullPath, targetFileFullPath, ftpConfigProperties.getHost(), ftpConfigProperties.getPort());
            logger.error(errorMessage, ex);
            throw new BatchException(BatchErrorCode.CONFIG_SYSTEM_ERROR, BatchErrorCode.CONFIG_SYSTEM_ERROR_MESSAGE, ex);
        }
    }
    
    public boolean processUploadFiletoFTP(String sourceFileFullPath,String targetFileFullPath,AsnbFtpConfigProperties asnbFtpConfigProperties, String date) throws BatchException {
        try{
            logger.info(String.format("Preparing file to copy.. [%s]", sourceFileFullPath));
            logger.info(String.format("Preparing target file.. [%s]", targetFileFullPath));
            logger.info(String.format("Preparing target FTP.. [%s:%s]", asnbFtpConfigProperties.getHost(), asnbFtpConfigProperties.getPort()));

            FTPUtils.uploadFileToFTP(sourceFileFullPath, targetFileFullPath, asnbFtpConfigProperties, asnbCheck, date);

            logger.info(String.format("Tasklet [%s] executed successfully", this.getClass().getSimpleName()));

            return true;
        } catch (BatchValidationException ex) {
            String errorMessage = String.format("Failed to upload file [%s] to target FTP folder [%s] using FTP [%s:%s]", sourceFileFullPath, targetFileFullPath, ftpConfigProperties.getHost(), ftpConfigProperties.getPort());
            logger.error(errorMessage, ex);
            throw new BatchException(BatchErrorCode.CONFIG_SYSTEM_ERROR, BatchErrorCode.CONFIG_SYSTEM_ERROR_MESSAGE, ex);
        }
    }
    
    public boolean processUploadPostingFiletoFTP(String sourceFileFullPath,String targetFileFullPath,HostFtpConfigProperties hostFtpConfigProperties) throws BatchException {
        try{
            logger.info(String.format("Preparing posting file to copy.. [%s]", sourceFileFullPath));
            logger.info(String.format("Preparing target posting file.. [%s]", targetFileFullPath));
            logger.info(String.format("Preparing target posting FTP.. [%s:%s]", hostFtpConfigProperties.getHost(), hostFtpConfigProperties.getPort()));

            FTPUtils.uploadFileToFTP(sourceFileFullPath, targetFileFullPath, hostFtpConfigProperties, asnbCheck);

            logger.info(String.format("Tasklet [%s] executed posting file successfully", this.getClass().getSimpleName()));

            return true;
        } catch (BatchValidationException ex) {
            String errorMessage = String.format("Failed to upload posting file [%s] to target FTP folder [%s] using FTP [%s:%s]", sourceFileFullPath, targetFileFullPath, hostFtpConfigProperties.getHost(), hostFtpConfigProperties.getPort());
            logger.error(errorMessage, ex);
            throw new BatchException(BatchErrorCode.CONFIG_SYSTEM_ERROR, BatchErrorCode.CONFIG_SYSTEM_ERROR_MESSAGE, ex);
        }
    }

}
