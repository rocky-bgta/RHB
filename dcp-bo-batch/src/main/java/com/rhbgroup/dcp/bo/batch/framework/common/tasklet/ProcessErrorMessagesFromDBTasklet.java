package com.rhbgroup.dcp.bo.batch.framework.common.tasklet;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.rhbgroup.dcp.bo.batch.framework.constants.BatchErrorCode;
import com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobContextParameter;
import com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter;
import com.rhbgroup.dcp.bo.batch.framework.exception.BatchException;
import com.rhbgroup.dcp.bo.batch.job.model.BatchSuspense;
import com.rhbgroup.dcp.bo.batch.job.repository.BatchSuspenseRepositoryImpl;

@Component
@Lazy
public class ProcessErrorMessagesFromDBTasklet implements Tasklet {
	
    private static final Logger logger = Logger.getLogger(ProcessErrorMessagesFromDBTasklet.class);
    
    @Value("${job.common.error.messages.max.limit}")
    private int errorMessagesMaxLimit;
    
    @Autowired
	BatchSuspenseRepositoryImpl batchSuspenseRepository;
    
    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
    	String jobName = chunkContext.getStepContext().getStepExecution().getJobExecution().getJobParameters().getString(BatchJobParameter.BATCH_JOB_PARAMETER_JOB_NAME_KEY);
    	
    	String jobExecutionId = null;
    	
    	if(chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().containsKey(BatchJobContextParameter.BATCH_JOB_CONTEXT_PARAMETER_TEMP_LAST_PROCESSED_JOB_EXECUTION_ID)) {
    		jobExecutionId = chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().getString(BatchJobContextParameter.BATCH_JOB_CONTEXT_PARAMETER_TEMP_LAST_PROCESSED_JOB_EXECUTION_ID);
    	} else {
    		logger.info(String.format("No last processed job execution detected in the context, finishing the tasklet [%s]", this.getClass().getSimpleName()));
    		return RepeatStatus.FINISHED;
    	}
    		
    	List<BatchSuspense> batchSuspenses = batchSuspenseRepository.getByJobNameAndJobExecutionId(jobName, jobExecutionId, errorMessagesMaxLimit);
    	
    	for(BatchSuspense batchSuspense : batchSuspenses) {
    		logger.error(String.format("BatchSuspense JobName::[%s] ExecutionId::[%s] %s", jobName, jobExecutionId, batchSuspense));
    	}
    	
    	// If batch suspense is more than one, we will treat the batch job as FAILED
    	if(!batchSuspenses.isEmpty()) {
    		throw new BatchException(BatchErrorCode.FIELD_VALIDATION_ERROR, BatchErrorCode.FIELD_VALIDATION_ERROR_MESSAGE);
    	}
    	
    	return RepeatStatus.FINISHED;
    }
}