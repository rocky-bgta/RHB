package com.rhbgroup.dcp.bo.batch.job.step.tasklet;

import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter.BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter.BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_FORMAT;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter.BATCH_JOB_PARAMETER_JOB_NAME_KEY;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter.BATCH_JOB_PARAMETER_JOB_BATCH_PROCESS_DATE_KEY;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchIBGRejectStatusParameter.BATCH_IBG_REJECT_STATUS_EXEC_FILE_NAME;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchIBGRejectStatusParameter.BATCH_IBG_REJECT_STATUS_RUN_WINDOW_PARAM;


import java.io.File;
import java.text.ParseException;
import java.util.Date;

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
import org.springframework.util.StringUtils;

import com.rhbgroup.dcp.bo.batch.framework.config.properties.FTPConfigProperties;
import com.rhbgroup.dcp.bo.batch.framework.constants.BatchErrorCode;
import com.rhbgroup.dcp.bo.batch.framework.exception.BatchException;
import com.rhbgroup.dcp.bo.batch.framework.exception.BatchValidationException;
import com.rhbgroup.dcp.bo.batch.framework.utils.DateUtils;
import com.rhbgroup.dcp.bo.batch.framework.utils.FTPUtils;

@Component
@Lazy
public class IBGRejectStatusGetFileTasklet implements Tasklet, InitializingBean {

	private static final Logger logger = Logger.getLogger(IBGRejectStatusGetFileTasklet.class);

	private String sourceFtpFilePath="";
	
	@Autowired
	private FTPConfigProperties ftpConfig;
	
	@Value("${job.updateibgrejectedstatusjob.ftpfolder}")
	private String sourceFtpFolder;
	
	@Value("${dcp.bo.batch.inputfolder.path}")
	private String targetFileFolder;
	
	@Value("${job.updateibgrejectedstatusjob.windowfiles}")
	private String windowfiles;
	
	
	@Override
	public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws BatchException {
		// TODO Auto-generated method stub
		String logMsg = "";
		try {
			String runParam= chunkContext.getStepContext().getStepExecution().getJobExecution().getJobParameters().getString(BATCH_IBG_REJECT_STATUS_RUN_WINDOW_PARAM);
			String batchSystemDateStr = chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().getString(BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY);
			String batchProcessDateStr = chunkContext.getStepContext().getStepExecution().getJobExecution().getJobParameters().getString(BATCH_JOB_PARAMETER_JOB_BATCH_PROCESS_DATE_KEY);
			String jobname = chunkContext.getStepContext().getStepExecution().getJobExecution().getJobParameters().getString(BATCH_JOB_PARAMETER_JOB_NAME_KEY);
			Date batchSystemDate = DateUtils.getDateFromString(batchSystemDateStr,BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_FORMAT);
			if(!StringUtils.isEmpty(batchProcessDateStr)) {
				batchSystemDate = DateUtils.getDateFromString(batchProcessDateStr,BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_FORMAT);
			}
			logMsg = String.format("Download IBG Reject Status file from FTP - batchSystemDateStr=%s,batchSystemDate=%s", batchSystemDateStr,batchSystemDate);
			logger.info(logMsg);
			if(null==runParam || runParam.isEmpty()) {
				runParam = chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().getString(BATCH_IBG_REJECT_STATUS_RUN_WINDOW_PARAM);
			}
			logMsg = String.format("Download IBG Reject Status file from FTP - select run param=%s,", runParam);
			logger.info(logMsg);
			String targetFileName="";
			String [] windowFileFmt = windowfiles.split(",");
	    	String pattern = (new StringBuffer().append("DCP_IBGSta_")
	    				.append(runParam)
	    				.append("_ddmmyy.txt")).toString();
			for (String filenameFmt : windowFileFmt) {
				if (filenameFmt.trim().equalsIgnoreCase(pattern)) {
					targetFileName = filenameFmt.trim();
					break;
				}
			}
			if(targetFileName.isEmpty()) {
				logMsg = String.format("Download IBG Reject Status file from FTP - unable to find matching file format for run param=%s,", runParam);
				logger.info(logMsg);
				throw new BatchException(BatchErrorCode.CONFIG_SYSTEM_ERROR,logMsg);
			}
			
			String fmtFileDate = DateUtils.formatDateString(batchSystemDate, "ddMMyy");
			targetFileName = targetFileName.replace("ddmmyy", fmtFileDate);
			sourceFtpFilePath = new StringBuffer().append(sourceFtpFolder)
					.append("/")
					.append(targetFileName).toString();
			String targetLocalPath = StringUtils.replace(targetFileFolder.concat(File.separator).concat(jobname), "\\", File.separator)  ;
			logMsg = String.format("Download IBG Reject Status file from FTP - targetFileName=%s, sourceFtpFilePath=%s, targetLocalFile=%s", targetFileName, sourceFtpFilePath, targetLocalPath);
			logger.info(logMsg);
			File targetJobFolder = new File(targetLocalPath);
			if (!targetJobFolder.exists()) {
				targetJobFolder.mkdirs();
			}
			String targetLocalFileFullPath = targetLocalPath.concat(File.separator).concat(targetFileName);
			
			logMsg = String.format("Download IBG Reject Status file from FTP, targetLocalFileFullPath=%s",targetLocalFileFullPath);
			logger.info(logMsg);

			FTPUtils.downloadFileFromFTP(sourceFtpFilePath, targetLocalPath, ftpConfig);
			chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().put(BATCH_IBG_REJECT_STATUS_EXEC_FILE_NAME, targetFileName);
		}catch( ParseException ex) {
			String errorMessage = "Get IBG Reject status-Exception when trying to pase config date";
			logger.error(errorMessage, ex);
			throw new BatchException(BatchErrorCode.CONFIG_SYSTEM_ERROR,BatchErrorCode.CONFIG_SYSTEM_ERROR_MESSAGE, ex);
		}catch(BatchValidationException e) {
			String errorMessage = String.format("Get IBG Reject status-Exception when trying to FTP-download file  %s", sourceFtpFilePath);
			logger.error(errorMessage, e);
			throw new BatchException(BatchErrorCode.FILE_NOT_FOUND,BatchErrorCode.FILE_NOT_FOUND_MESSAGE, e);
		}catch(Exception ex) {
			String errorMessage = String.format("Get IBG Reject status-Exception when trying to FTP-download file %s", ex.getMessage());
			logger.error(errorMessage, ex);
			throw new BatchException(BatchErrorCode.GENERIC_SYSTEM_ERROR,BatchErrorCode.GENERIC_SYSTEM_ERROR_MESSAGE, ex);
		}
		return RepeatStatus.FINISHED;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		// Do nothing because this is the implementation of Spring Batch
	}
}
