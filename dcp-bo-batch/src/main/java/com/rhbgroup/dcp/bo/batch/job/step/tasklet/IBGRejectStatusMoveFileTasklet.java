package com.rhbgroup.dcp.bo.batch.job.step.tasklet;

import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchIBGRejectStatusParameter.BATCH_IBG_REJECT_STATUS_EXEC_FILE_NAME;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchIBGRejectStatusParameter.BATCH_IBG_REJECT_STATUS_VALIDATING_STATUS;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.ExitCode.FAILED;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.ExitCode.SUCCESS;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter.BATCH_JOB_PARAMETER_JOB_NAME_KEY;
import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.rhbgroup.dcp.bo.batch.framework.constants.BatchErrorCode;
import com.rhbgroup.dcp.bo.batch.framework.exception.BatchException;
import com.rhbgroup.dcp.bo.batch.framework.repository.BatchParameterRepositoryImpl;


@Component
@Lazy
public class IBGRejectStatusMoveFileTasklet implements Tasklet {

	@Value("${dcp.bo.batch.successfolder.path}")
	private String successFolder;
	
	@Value("${dcp.bo.batch.failedfolder.path}")
	private String failFolder;
	
	@Value("${dcp.bo.batch.inputfolder.path}")
	private String sourceFolder;
	
	@Autowired
	private BatchParameterRepositoryImpl batchParameterImpl;
	
	private static final Logger logger = Logger.getLogger(IBGRejectStatusMoveFileTasklet.class);

	String jobExecutionId="";

	@Override
	public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
		// TODO Auto-generated method stub
		String logMsg ="";
		try {
			jobExecutionId = chunkContext.getStepContext().getStepExecution().getJobExecution().getId().toString();
			logMsg = String.format("IBG Reject status move file after processing jobExecutionId=%s", jobExecutionId);
			logger.info(logMsg);
			String jobname=chunkContext.getStepContext().getStepExecution().getJobExecution().getJobParameters().getString(BATCH_JOB_PARAMETER_JOB_NAME_KEY);
			int validateStatus = SUCCESS;
			
			if(null!=chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().get(BATCH_IBG_REJECT_STATUS_VALIDATING_STATUS)) {
				validateStatus=chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().getInt(BATCH_IBG_REJECT_STATUS_VALIDATING_STATUS);
			}

			logMsg = String.format("IBG Reject status move file after processing validate Status=%s", validateStatus);
			logger.info(logMsg);

			String ibgFileName = chunkContext.getStepContext().getStepExecution().getJobExecution()
					.getExecutionContext().getString(BATCH_IBG_REJECT_STATUS_EXEC_FILE_NAME);
			logMsg = String.format("IBG Reject status move file after processing ibg File Name=%s", ibgFileName);
			logger.info(logMsg);
			String sourceFilePath = StringUtils.replace(sourceFolder
								.concat(File.separator).concat(jobname).concat(File.separator)
								.concat(ibgFileName),"\\", File.separator);
			File sourceFile = new File(sourceFilePath);
			File destFile = null;
			if (SUCCESS == validateStatus) {
				String destFilePath = StringUtils.replace(successFolder.concat(File.separator)
							.concat(jobname)
							.concat(File.separator)
							.concat(ibgFileName),"\\", File.separator);
				destFile = new File(destFilePath);
				logMsg = String.format("Move file to success folder=%s, jobExecutionId=%s",destFilePath,jobExecutionId);
				batchParameterImpl.updateIBGRejectLastProcessedValue(jobExecutionId);	
				logger.info("Update TBL_BATCH_CONFIG param_value with emptyId");
				logMsg = String.format("Update TBL_BATCH_CONFIG with jobExecutionId=%s",jobExecutionId);
				logger.info(logMsg);
			} else if (FAILED == validateStatus) {
				String destFilePath = StringUtils.replace(failFolder.concat(File.separator)
						.concat(jobname)
						.concat(File.separator).concat(ibgFileName),"\\", File.separator);
				destFile = new File(destFilePath);
				String emptyId="";
				batchParameterImpl.updateIBGRejectLastProcessedValue(emptyId);
				logger.info("Update TBL_BATCH_CONFIG param_value with emptyId");
				logMsg = String.format("Move file to failed folder=%s, jobExecutionId=%s",destFilePath,jobExecutionId);
				logger.info(logMsg);
			}
			FileUtils.copyFile(sourceFile, destFile);
			logMsg = String.format("IBG Reject status copy file after processing to destination=%s",destFile != null ? destFile.getAbsolutePath() : null);
        	FileUtils.deleteQuietly(sourceFile);
			logMsg = String.format("IBG Reject status delete file =%s",sourceFile.getAbsolutePath());
			logger.info(logMsg);
		}catch(IOException ex) {
			logger.info(String.format("IBG Reject status move file after processing exception=%s",ex.getMessage()));
			throw new BatchException(BatchErrorCode.FILE_NOT_FOUND, logMsg,ex);
		}catch(Exception ex) {
			logger.info(String.format("IBG Reject status move file after processing exception=%s",ex.getMessage()));
			throw new BatchException(BatchErrorCode.GENERIC_SYSTEM_ERROR, logMsg,ex);
		}
		return RepeatStatus.FINISHED;
	}

}
