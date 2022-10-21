package com.rhbgroup.dcp.bo.batch.framework.common.tasklet;

import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobContextParameter.BATCH_JOB_CONTEXT_PARAMETER_TEMP_IN_FILE_FULL_PATH_KEY;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter.BATCH_JOB_PARAMETER_JOB_NAME_KEY;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.SystemFolder.BATCH_SYSTEM_FOLDER_FAILED_DIRECTORY;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.SystemFolder.BATCH_SYSTEM_FOLDER_SUCCESS_DIRECTORY;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.rhbgroup.dcp.bo.batch.framework.constants.BatchErrorCode;
import com.rhbgroup.dcp.bo.batch.framework.exception.BatchException;

@Component
@Lazy
public class MoveFiletoSuccessOrFailedFolderTasklet implements Tasklet {
    private static final Logger logger = Logger.getLogger(MoveFiletoSuccessOrFailedFolderTasklet.class);

    @Value(BATCH_SYSTEM_FOLDER_SUCCESS_DIRECTORY)
    private String successFolderPath;
    
    @Value(BATCH_SYSTEM_FOLDER_FAILED_DIRECTORY)
    private String failedFolderPath;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
    	logger.info(String.format("Executing tasklet [%s]", this.getClass().getSimpleName()));
    	
    	String batchJobName = chunkContext.getStepContext().getStepExecution().getJobExecution().getJobParameters().getString(BATCH_JOB_PARAMETER_JOB_NAME_KEY);

    	String sourceFilePath = null;
    	if(chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().containsKey(BATCH_JOB_CONTEXT_PARAMETER_TEMP_IN_FILE_FULL_PATH_KEY)) {
    		sourceFilePath = chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().getString(BATCH_JOB_CONTEXT_PARAMETER_TEMP_IN_FILE_FULL_PATH_KEY);
    	} else {
    		logger.warn("No FTP input filepath detected in context, skip the job");
    		return RepeatStatus.FINISHED;
    	}

        File sourceFile = Paths.get(sourceFilePath).toFile();
        File targetFilePath = null;
        
        if((!chunkContext.getStepContext().getStepExecution().getJobExecution().getAllFailureExceptions().isEmpty())
                ||  chunkContext.getStepContext().getStepExecution().getJobExecution().getStatus()==BatchStatus.FAILED) {
        	targetFilePath=Paths.get(failedFolderPath, batchJobName).toFile();
        } else {
        	targetFilePath=Paths.get(successFolderPath, batchJobName).toFile();
        }
        
        try {
        	logger.debug(String.format("Moving source file [%s] to target folder [%s]", sourceFile, targetFilePath));
        	FileUtils.copyToDirectory(sourceFile, targetFilePath);
        	FileUtils.deleteQuietly(sourceFile);
        } catch(IOException e) {
        	String errorMessage = String.format("Error happened while moving source file [%s] to target folder [%s]", sourceFile, targetFilePath);
			logger.error(errorMessage, e);
			throw new BatchException(BatchErrorCode.GENERIC_SYSTEM_ERROR, BatchErrorCode.GENERIC_SYSTEM_ERROR_MESSAGE, e);
        }
        
        logger.info(String.format("Tasklet [%s] executed successfully", this.getClass().getSimpleName()));
        return RepeatStatus.FINISHED;
    }

}
