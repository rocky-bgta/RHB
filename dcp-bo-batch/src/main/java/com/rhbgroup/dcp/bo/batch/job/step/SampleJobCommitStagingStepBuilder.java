package com.rhbgroup.dcp.bo.batch.job.step;

import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.rhbgroup.dcp.bo.batch.framework.core.BaseStepBuilder;
import com.rhbgroup.dcp.bo.batch.job.step.tasklet.SampleJobCommitStagingTasklet;

@Component
@Lazy
public class SampleJobCommitStagingStepBuilder extends BaseStepBuilder {
    @Autowired
    private SampleJobCommitStagingTasklet sampleJobCommitStagingTasklet;

    @Override
    public Step buildStep() {
        return getDefaultStepBuilder("SampleJobCommitStagingStep")
                .tasklet(this.sampleJobCommitStagingTasklet)
                .build();
    }
}
