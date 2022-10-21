package com.rhbgroup.dcp.bo.batch.job.step.tasklet;

import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobContextParameter.BATCH_JOB_CONTEXT_PARAMETER_TEMP_IN_FILE_FULL_PATH_KEY;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.log4j.Logger;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.rhbgroup.dcp.bo.batch.framework.constants.BatchErrorCode;
import com.rhbgroup.dcp.bo.batch.framework.exception.BatchException;
import com.rhbgroup.dcp.bo.batch.job.repository.BatchStagedNotificationRawRepositoryImpl;

@Component
@Lazy
public class LoadNotificationValidatorTasklet implements Tasklet {
	private static final Logger logger = Logger.getLogger(LoadNotificationValidatorTasklet.class);

	@Autowired
	private BatchStagedNotificationRawRepositoryImpl notificationRawRepositoryImpl;
	
	@Override
	public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
		String logMsg;
		try {
	        String fileFullPath = chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().getString(BATCH_JOB_CONTEXT_PARAMETER_TEMP_IN_FILE_FULL_PATH_KEY);
			File loadNotificationFile = new File(fileFullPath);
	        String fileName = loadNotificationFile.getName();
	        int countFile = notificationRawRepositoryImpl.findNotificationFileLoaded(fileName);
	        if(countFile>0) {
	        	logMsg=String.format("File %s has been loaded before", fileName);
	        	logger.error(logMsg);
				throw new BatchException(BatchErrorCode.FILE_VALIDATION_ERROR, logMsg );
	        }else {
	        	logMsg=String.format("File %s has not been loaded before", fileName);
	        	logger.info(logMsg);	    
	        }
		}catch(Exception ex) {
			logger.error(String.format("Exception while checking file is loaded,%s", ex));
			throw new BatchException(BatchErrorCode.DB_SYSTEM_ERROR, BatchErrorCode.DB_SYSTEM_ERROR_MESSAGE, ex);
		}
		return RepeatStatus.FINISHED;
	}
}
