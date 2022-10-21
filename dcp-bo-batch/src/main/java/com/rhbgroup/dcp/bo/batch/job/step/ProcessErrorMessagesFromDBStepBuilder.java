package com.rhbgroup.dcp.bo.batch.job.step;

import org.springframework.batch.core.Step;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.rhbgroup.dcp.bo.batch.framework.common.tasklet.ProcessErrorMessagesFromDBTasklet;
import com.rhbgroup.dcp.bo.batch.framework.core.BaseStepBuilder;

@Component
@Lazy
public class ProcessErrorMessagesFromDBStepBuilder extends BaseStepBuilder {
    @Autowired
    private ProcessErrorMessagesFromDBTasklet processErrorMessagesFromDBTasklet;

    @Override
    public Step buildStep() {
        return getDefaultStepBuilder("ProcessErrorMessagesFromDBStep")
                .tasklet(this.processErrorMessagesFromDBTasklet)
                .build();
    }
}
