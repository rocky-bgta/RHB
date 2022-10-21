package com.rhbgroup.dcp.bo.batch.job.step;

import org.springframework.batch.core.Step;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.rhbgroup.dcp.bo.batch.framework.core.BaseStepBuilder;
import com.rhbgroup.dcp.bo.batch.job.step.tasklet.InitialDBBillerPaymentFileTasklet;

@Component
@Lazy
public class InitialDBBillerPaymentFileStepBuilder extends BaseStepBuilder {
    @Autowired
    private InitialDBBillerPaymentFileTasklet initialDBBillerPaymentFileTasklet;

    @Override
    public Step buildStep() {
        return getDefaultStepBuilder("InitialDBBillerPaymentConfigStep")
                .tasklet(this.initialDBBillerPaymentFileTasklet)
                .build();
    }
}
