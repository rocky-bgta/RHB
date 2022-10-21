package com.rhbgroup.dcp.bo.batch.job.step.tasklet;

import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.SystemFolder.BATCH_SYSTEM_FOLDER_INPUT_DIRECTORY;

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
public class MoveFiletoArchiveFolderTasklet implements Tasklet {
    private static final Logger logger = Logger.getLogger(MoveFiletoArchiveFolderTasklet.class);
    
    private static final String ARCHIVE_FILE_FOLDER = "dcp_mass_notification_from_archive";
    
    private static final String BATCH_EXECUTION_CONTEXT_INPUT_FILE_FULL_PATH = "inputFolderFullPath";
    
    @Value(BATCH_SYSTEM_FOLDER_INPUT_DIRECTORY)
	private String sourceFilePath;
    
    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
    	logger.info(String.format("Executing tasklet [%s]", this.getClass().getSimpleName()));
    	
    	File sourceFile = null;
    	if (chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().containsKey(BATCH_EXECUTION_CONTEXT_INPUT_FILE_FULL_PATH)) {
    		sourceFile = Paths.get(chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().getString(BATCH_EXECUTION_CONTEXT_INPUT_FILE_FULL_PATH)).toFile();
			
			File targetFilePath = null;
			if((chunkContext.getStepContext().getStepExecution().getJobExecution().getAllFailureExceptions().isEmpty())) {
				if (chunkContext.getStepContext().getStepExecution().getJobExecution().getStatus() == BatchStatus.COMPLETED) {
					targetFilePath=Paths.get(sourceFilePath, ARCHIVE_FILE_FOLDER).toFile();
					processFileContent(sourceFile, targetFilePath);
				} else {
					logger.warn("Skip archive file due to previous job status is not COMPLETED");	
				}        	
			} else {
				logger.warn("Skip archive file due to exception(s) detected in the context");	
			}
    	} else {
			logger.warn("Skip archive file due to file not found in context");
		}		
        
        logger.info(String.format("Tasklet [%s] executed successfully", this.getClass().getSimpleName()));
        return RepeatStatus.FINISHED;
    }

	private void processFileContent(File sourceFile, File targetFilePath) throws BatchException {
		try {
			logger.debug(String.format("Moving source file [%s] to target folder [%s]", sourceFile, targetFilePath));
			FileUtils.copyToDirectory(sourceFile, targetFilePath);
			FileUtils.deleteQuietly(sourceFile);
		} catch(IOException e) {
			String errorMessage = String.format("Error happened while moving source file [%s] to target folder [%s]", sourceFile, targetFilePath);
			logger.error(errorMessage, e);
			throw new BatchException(BatchErrorCode.GENERIC_SYSTEM_ERROR, BatchErrorCode.GENERIC_SYSTEM_ERROR_MESSAGE, e);
		} catch(Exception e) {
			String errorMessage = String.format("Error happened while moving source file [%s] to target folder [%s]", sourceFile, targetFilePath);
			logger.error(errorMessage, e);
			throw new BatchException(BatchErrorCode.GENERIC_SYSTEM_ERROR, BatchErrorCode.GENERIC_SYSTEM_ERROR_MESSAGE, e);
		}
	}

}
