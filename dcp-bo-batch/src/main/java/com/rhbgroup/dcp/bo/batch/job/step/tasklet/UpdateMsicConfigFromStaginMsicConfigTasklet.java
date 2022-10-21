package com.rhbgroup.dcp.bo.batch.job.step.tasklet;

import org.apache.log4j.Logger;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.rhbgroup.dcp.bo.batch.framework.constants.BatchErrorCode;
import com.rhbgroup.dcp.bo.batch.framework.exception.BatchException;
import com.rhbgroup.dcp.bo.batch.job.repository.MsicConfigRepositoryImpl;

@Component
@Lazy
public class UpdateMsicConfigFromStaginMsicConfigTasklet implements Tasklet{
	
	private static final Logger logger = Logger.getLogger(UpdateMsicConfigFromStaginMsicConfigTasklet.class);

	@Autowired
	private MsicConfigRepositoryImpl msicConfigRepositoryImpl;
	
	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
		logger.info("Initializing msic config..");
		try {
			Long jobExecutionId = chunkContext.getStepContext().getStepExecution().getJobExecution().getId();
			msicConfigRepositoryImpl.updateOrInsertMsicConfigs(jobExecutionId);
		
		} catch (Exception ex) {
			String errorMsg = String.format("Exception: exception=%s", ex.getMessage());
			logger.error(errorMsg);
			chunkContext.getStepContext().getStepExecution().getJobExecution().setExitStatus(ExitStatus.FAILED);
			throw new BatchException(BatchErrorCode.DB_SYSTEM_ERROR, errorMsg, ex);
		}
		return RepeatStatus.FINISHED;
	}
}
