package com.rhbgroup.dcp.bo.batch.framework.common.tasklet;

import org.apache.log4j.Logger;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.rhbgroup.dcp.bo.batch.framework.model.BatchParameter;
import com.rhbgroup.dcp.bo.batch.framework.repository.BatchParameterRepositoryImpl;

import java.util.List;

@Component
@Lazy
public class ReadBatchParameterFromDBTasklet implements Tasklet, InitializingBean {
    private static final Logger logger = Logger.getLogger(ReadBatchParameterFromDBTasklet.class);
    @Autowired
    private BatchParameterRepositoryImpl batchParameterRepository;

    @Override
    public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
        logger.info("Initializing DB Batch Parameters..");
    	List<BatchParameter> batchParameters=batchParameterRepository.getBatchParametres();

        if(batchParameters!=null && !batchParameters.isEmpty())
        {

            logger.info(String.format("Loading DB Batch Parameters to Job Execution context.. [count: %d]", batchParameters.size()));
            for(BatchParameter parameter : batchParameters)
            {
                chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().put(
                        parameter.getName()
                        ,parameter.getValue());
            }
        }

        return RepeatStatus.FINISHED;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        // Do nothing because this is the implementation of Spring Batch
    }



}
