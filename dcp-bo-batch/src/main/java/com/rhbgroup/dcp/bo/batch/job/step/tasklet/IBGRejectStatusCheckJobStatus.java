package com.rhbgroup.dcp.bo.batch.job.step.tasklet;

import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchIBGRejectStatusParameter.BATCH_IBG_REJECT_STATUS_VALIDATING_STATUS;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.ExitCode.SUCCESS;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.ExitCode.FAILED;

import org.apache.log4j.Logger;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.rhbgroup.dcp.bo.batch.framework.constants.BatchErrorCode;
import com.rhbgroup.dcp.bo.batch.framework.exception.BatchException;

@Component
@Lazy
public class IBGRejectStatusCheckJobStatus implements Tasklet{
	
	private static final Logger logger = Logger.getLogger(IBGRejectStatusCheckJobStatus.class);

	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
		String logMsg;
		logger.info("IBG Reject Status check job status");
		int validateStatus = SUCCESS;
		if(null!=chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().get(BATCH_IBG_REJECT_STATUS_VALIDATING_STATUS)) {
			validateStatus=chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().getInt(BATCH_IBG_REJECT_STATUS_VALIDATING_STATUS);
		}
		if(FAILED == validateStatus) {
			logMsg = String.format("IBG Reject status job-validating status failed, validating status = %s",validateStatus);
			logger.warn(logMsg);
			chunkContext.getStepContext().getStepExecution().getJobExecution().addFailureException(new BatchException(BatchErrorCode.FIELD_VALIDATION_ERROR, logMsg ));
		}else {
			logger.info("IBG Reject Status job status success");
		}
		return RepeatStatus.FINISHED;
	}

}
