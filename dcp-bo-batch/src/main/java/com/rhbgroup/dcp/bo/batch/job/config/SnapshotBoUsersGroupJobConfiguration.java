package com.rhbgroup.dcp.bo.batch.job.config;

import com.rhbgroup.dcp.bo.batch.framework.core.BaseJobConfiguration;
import com.rhbgroup.dcp.bo.batch.job.step.InitialDBBatchSystemDateStepBuilder;
import com.rhbgroup.dcp.bo.batch.job.step.InitialDBBillerPaymentConfigStepBuilder;
import com.rhbgroup.dcp.bo.batch.job.step.SnapshotBoUsersGroupStepBuilder;
import com.rhbgroup.dcp.bo.batch.job.step.SnapshotBoUsersStepBuilder;
import com.rhbgroup.dcp.bo.batch.job.step.tasklet.DeleteDuplicateSnapshotBoUsersGroupTasklet;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

@Configuration
@Lazy
public class SnapshotBoUsersGroupJobConfiguration extends BaseJobConfiguration {
    private static final String JOB_NAME = "SnapshotBoUsersGroupJob";

    @Autowired
    private SnapshotBoUsersGroupStepBuilder snapshotBoUsersGroupStep;
    @Autowired
    private SnapshotBoUsersStepBuilder snapshotBoUsersStep;
    @Autowired
    private DeleteDuplicateSnapshotBoUsersGroupTasklet deleteDuplicateTasklet;

    @Bean
    protected Step deleteDuplicate() {
        return getStepBuilderFactory().get("deleteDuplicate")
                .tasklet(deleteDuplicateTasklet)
                .listener(this.batchJobCommonStepListener)
                .build();
    }

    @Bean(JOB_NAME)
    public Job buildJob() {
       return getDefaultJobBuilder(JOB_NAME)
                .next(deleteDuplicate())
                .next(this.snapshotBoUsersStep.buildStep())
                .next(this.snapshotBoUsersGroupStep.buildStep())
                .build();
    }
}
