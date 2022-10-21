package com.rhbgroup.dcp.bo.batch.job.step.tasklet;

import com.rhbgroup.dcp.bo.batch.framework.common.tasklet.ReadBatchParameterFromDBTasklet;
import com.rhbgroup.dcp.bo.batch.framework.constants.BatchErrorCode;
import com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter;
import com.rhbgroup.dcp.bo.batch.framework.exception.BatchException;
import com.rhbgroup.dcp.bo.batch.framework.model.BatchParameter;
import com.rhbgroup.dcp.bo.batch.framework.repository.BatchParameterRepositoryImpl;
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

@Component
@Lazy
public class InitialDBBillerPaymentConfigTasklet implements Tasklet, InitializingBean {
    private static final Logger logger = Logger.getLogger(ReadBatchParameterFromDBTasklet.class);
    @Autowired
    private BatchParameterRepositoryImpl batchParameterRepository;

    @Override
    public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
        logger.info("Initializing DB Biller Payment Config..");
		try {
			BatchParameter batchSystemDate = batchParameterRepository.getBatchParameter(BatchJobParameter.BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY);
	        batchParameterRepository.updateBillerPaymentConfig(batchSystemDate);
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
