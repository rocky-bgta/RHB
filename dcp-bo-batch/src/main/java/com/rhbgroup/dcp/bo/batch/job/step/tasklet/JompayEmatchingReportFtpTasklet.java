package com.rhbgroup.dcp.bo.batch.job.step.tasklet;

import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter.BATCH_JOB_PARAMETER_JOB_NAME_KEY;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.JompayEmatchingParameter.JOMPAY_OUTBOUND_TXT_FILE;

import java.io.File;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.rhbgroup.dcp.bo.batch.framework.config.properties.FTPConfigProperties;
import com.rhbgroup.dcp.bo.batch.framework.constants.BatchErrorCode;
import com.rhbgroup.dcp.bo.batch.framework.exception.BatchException;
import com.rhbgroup.dcp.bo.batch.framework.utils.FTPUtils;

@Component
@Lazy
public class JompayEmatchingReportFtpTasklet implements Tasklet {
    static final Logger logger = Logger.getLogger(JompayEmatchingReportFtpTasklet.class);
    
	@Autowired
	private FTPConfigProperties ftpConfig;
	
	@Value("${job.jompayematchingreportjob.ftpfolder}")
	private String ftpTargetFolder;
	
	@Override
	public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
		// TODO Auto-generated method stub
		String logMsg;		
		try {
			String jobname=chunkContext.getStepContext().getStepExecution().getJobExecution().getJobParameters().getString(BATCH_JOB_PARAMETER_JOB_NAME_KEY);
			logMsg = String.format("JompayEmatchingReport upload file to Ftp server job=%s, ftpTargetFolder=%s",jobname,ftpTargetFolder);
			logger.info(logMsg);
			String sourceContextOutputFile=chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().getString(JOMPAY_OUTBOUND_TXT_FILE);
			String sourceFileFullPath = StringUtils.replace(sourceContextOutputFile, "\\", File.separator);
			logMsg = String.format("JompayEmatchingReport upload file to Ftp server sourceFileFullPath=%s",sourceFileFullPath);
			logger.info(logMsg);
			logger.info("Uploading...");
			FTPUtils.uploadFileToFTP(sourceFileFullPath, ftpTargetFolder, ftpConfig);
			logger.info("JompayEmatchingReport upload Complete.");
		}catch(Exception ex) {
			logger.info(String.format("JompayEmatchingReport copy file to Ftp server exception=%s",ex.getMessage()));
			throw new BatchException(BatchErrorCode.FTP_SYSTEM_ERROR, BatchErrorCode.FTP_SYSTEM_ERROR_MESSAGE, ex );
		}
		return RepeatStatus.FINISHED;
	}

}
