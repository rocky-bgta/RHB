package com.rhbgroup.dcp.bo.batch.job.step.tasklet;

import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BillerPaymentOutbound.STEP_EXECUTION_STATUS;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BillerPaymentOutbound.JOB_EXECUTION_STATUS;
import java.util.Queue;

import org.apache.log4j.Logger;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.rhbgroup.dcp.bo.batch.framework.constants.BatchErrorCode;
import com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant;
import com.rhbgroup.dcp.bo.batch.framework.exception.BatchException;
import com.rhbgroup.dcp.bo.batch.job.model.BillerDynamicPaymentOutboundConfig;

@Component
@Lazy
public class BillerDynamicPaymentRemoveQueueItemTasklet implements Tasklet, InitializingBean {

    static final Logger logger = Logger.getLogger(BillerDynamicPaymentRemoveQueueItemTasklet.class);

	@Autowired
	@Qualifier("BillDynamicPaymentConfigOutboundQueue")
	private Queue<BillerDynamicPaymentOutboundConfig> queue ;
	
	@Override
	public void afterPropertiesSet() throws Exception {
		// Do nothing because this is the implementation of Spring Batch
	}

	@Override
	public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws BatchException {
		try {
			int prevStepStatus = chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().getInt(STEP_EXECUTION_STATUS );
			logger.info(String.format("before clear item queue size=%s, prevStepStatus=%s", queue.size(), prevStepStatus));
			if(!queue.isEmpty()) {
				queue.poll();
			}
			logger.info(String.format("after clear item queue size=%s", queue.size()));
			chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().put(STEP_EXECUTION_STATUS ,BatchSystemConstant.ExitCode.SUCCESS);
		
			if(prevStepStatus==BatchSystemConstant.ExitCode.FAILED) {
				chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().put(JOB_EXECUTION_STATUS ,BatchSystemConstant.ExitCode.FAILED);
			}
			if(chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().containsKey(JOB_EXECUTION_STATUS)) {
				int jobStatus = chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().getInt(JOB_EXECUTION_STATUS);
				if(jobStatus==BatchSystemConstant.ExitCode.FAILED && queue.size()==0) {
					throw new BatchException(BatchErrorCode.GENERIC_SYSTEM_ERROR,"Biller Payment File Job Failed.See log file for detail");
				}
			}
		}catch(BatchException ex) {
			logger.info(String.format("Batch Exception Biller payment remove item from config queue: %s ", ex.getMessage()));
			logger.error(ex);
			throw ex;		
		} catch (Exception ex) {
			logger.info(String.format("Exception Biller payment remove item from config queue: %s ", ex.getMessage()));
			logger.error(ex);
		}
		return RepeatStatus.FINISHED;
	}

}
