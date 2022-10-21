package com.rhbgroup.dcp.bo.batch.job.step.tasklet;

import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BillerPaymentOutbound.STEP_EXECUTION_STATUS;

import java.io.File;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import java.nio.file.Paths;

import com.rhbgroup.dcp.bo.batch.framework.utils.FTPUtils;
import com.rhbgroup.dcp.bo.batch.framework.config.properties.FTPConfigProperties;
import com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant;
@Component
@Lazy
@Scope("prototype")
public class BillerPaymentFileOutboundFtpTasklet implements Tasklet, InitializingBean  {
    static final Logger logger = Logger.getLogger(BillerPaymentFileOutboundFtpTasklet.class);
    
    private FTPConfigProperties ftpConfigProperties;
	
	private String targetFileFolderPath;
	private String contextSourceFile;
	String logMsg="";
	
	public void initConnection(FTPConfigProperties ftpConfigProperties) {
		this.ftpConfigProperties = ftpConfigProperties;
		logMsg = String.format("init connection: ftpHost=%s, ftpPort=%s, ftpUsername=%s"
				, ftpConfigProperties.getHost()
				, ftpConfigProperties.getPort()
				, ftpConfigProperties.getUsername());
		logger.info(logMsg);
	}
	
	public void initPath(String contextSourceFile, String targetFileFolderPath) {
		logMsg = String.format("init path : contextSourceFile=%s, targetFileFolderPath=%s",contextSourceFile, targetFileFolderPath );
		logger.info(logMsg);
		this.contextSourceFile = contextSourceFile;
		this.targetFileFolderPath = targetFileFolderPath;
	}

	@Override
	public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext)  {
		try {
			int prevStep = chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().getInt(STEP_EXECUTION_STATUS);
			String message=String.format("copy file to ftp,previous step status=%s", prevStep);
			logger.info(message);
			if( BatchSystemConstant.ExitCode.FAILED == prevStep ) {
				logger.info("previous step failed, skip this step");
				return RepeatStatus.FINISHED;
			}
			String sourceFilePath =chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().getString(contextSourceFile);
			logger.info (String.format ("copy from source File Path %s, to destination %s", sourceFilePath,targetFileFolderPath ));
			File sourceFileFullPath= Paths.get(StringUtils.replace(sourceFilePath, "\\", File.separator)).toFile();
			FTPUtils.uploadFileToFTP(sourceFileFullPath.getAbsolutePath(), targetFileFolderPath, ftpConfigProperties);
        	chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().put(STEP_EXECUTION_STATUS ,BatchSystemConstant.ExitCode.SUCCESS);
        }catch(Exception ex) {
        	chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().put(STEP_EXECUTION_STATUS ,BatchSystemConstant.ExitCode.FAILED);
        	logger.info(String.format("exception copy biller payment file to ftp: %s ", ex.getMessage()));
        	logger.error(ex);
        }
		
		return RepeatStatus.FINISHED;
	}
	
	@Override
	public void afterPropertiesSet() throws Exception {
		// Do nothing because this is the implementation of Spring Batch
	}

}
