package com.rhbgroup.dcp.bo.batch.job.step;

import com.rhbgroup.dcp.bo.batch.framework.core.BaseStepBuilder;
import com.rhbgroup.dcp.bo.batch.job.step.tasklet.InitialDBBatchSystemDateTasklet;
import org.springframework.batch.core.Step;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
@Lazy
public class InitialDBBatchSystemDateStepBuilder extends BaseStepBuilder {
    @Autowired
    private InitialDBBatchSystemDateTasklet initialDBBatchSystemDateTasklet;

    @Override
    public Step buildStep() {
        return getDefaultStepBuilder("InitialDBBatchSystemDateStep")
                .tasklet(this.initialDBBatchSystemDateTasklet)
                .build();
    }
}
