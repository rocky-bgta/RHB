package com.rhbgroup.dcp.bo.batch.job.step;

import org.springframework.batch.core.Step;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.rhbgroup.dcp.bo.batch.framework.core.BaseStepBuilder;
import com.rhbgroup.dcp.bo.batch.job.step.tasklet.UpdateBillerAccountStatusTasklet;

@Component
@Lazy
public class UpdateBillerAccountStatusStepBuilder extends BaseStepBuilder {
    @Autowired
    private UpdateBillerAccountStatusTasklet updateBillerAccountStatusTasklet;

    @Override
    public Step buildStep() {
        return getDefaultStepBuilder("UpdateBillerAccountStatusStep")
                .tasklet(this.updateBillerAccountStatusTasklet)
                .build();
    }
}
