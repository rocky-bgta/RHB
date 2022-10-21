package com.rhbgroup.dcp.bo.batch.job.step.tasklet;

import com.rhbgroup.dcp.bo.batch.job.repository.PremierCustomerInfoandRMCodeTaggingRepositoryImpl;
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
public class PremierCustomerInfoandRMCodeTaggingJobTruncateStagingTasklet implements Tasklet, InitializingBean {

    @Autowired
    private PremierCustomerInfoandRMCodeTaggingRepositoryImpl premierCustomerInfoandRMCodeTaggingRepository;

    private String tableName = "TBL_BATCH_STAGED_PREMIER_RM_TAG";

    @Override
    public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {

        premierCustomerInfoandRMCodeTaggingRepository.truncateTable(tableName);

        return RepeatStatus.FINISHED;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        // Do nothing because this is the implementation of Spring Batch
    }
}
