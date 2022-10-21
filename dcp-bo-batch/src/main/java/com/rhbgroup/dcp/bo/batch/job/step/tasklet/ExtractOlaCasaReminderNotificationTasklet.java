package com.rhbgroup.dcp.bo.batch.job.step.tasklet;

import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter.BATCH_JOB_PARAMETER_JOB_EXECUTION_ID_KEY;
import org.apache.log4j.Logger;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.rhbgroup.dcp.bo.batch.framework.constants.BatchErrorCode;
import com.rhbgroup.dcp.bo.batch.framework.exception.BatchException;
import com.rhbgroup.dcp.bo.batch.framework.repository.OlaCasaReminderRepositoryImpl;

@Component
@Lazy
public class ExtractOlaCasaReminderNotificationTasklet implements Tasklet, InitializingBean {
    private static final Logger logger = Logger.getLogger(ExtractOlaCasaReminderNotificationTasklet.class);
    
    @Autowired
    private OlaCasaReminderRepositoryImpl olaCasaReminderRepositoryImpl;
    
	String jobExecutionId="";
    
    @Override
    public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
        logger.info("Initializing Summary Config..");
		try {
			jobExecutionId = chunkContext.getStepContext().getStepExecution().getJobExecution().getJobParameters().getString(BATCH_JOB_PARAMETER_JOB_EXECUTION_ID_KEY);
			logger.info(String.format("jobExecutionId = %s", jobExecutionId));
			olaCasaReminderRepositoryImpl.insertSummary(jobExecutionId);

		} catch(Exception ex) {
				String errorMsg = String.format("Exception: exception=%s",ex.getMessage());
				logger.error(errorMsg);
				chunkContext.getStepContext().getStepExecution().getJobExecution().setExitStatus(ExitStatus.FAILED);
				throw new BatchException(BatchErrorCode.DB_SYSTEM_ERROR,errorMsg,ex);
		}
        return RepeatStatus.FINISHED;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        // Do nothing because this is the implementation of Spring Batch
    }
}
