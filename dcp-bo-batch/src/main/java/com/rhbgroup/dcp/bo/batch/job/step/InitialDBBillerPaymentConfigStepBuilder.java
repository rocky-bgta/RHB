package com.rhbgroup.dcp.bo.batch.job.step;

import com.rhbgroup.dcp.bo.batch.framework.core.BaseStepBuilder;
import com.rhbgroup.dcp.bo.batch.job.step.tasklet.InitialDBBillerPaymentConfigTasklet;
import org.springframework.batch.core.Step;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
@Lazy
public class InitialDBBillerPaymentConfigStepBuilder extends BaseStepBuilder {
    @Autowired
    private InitialDBBillerPaymentConfigTasklet initialDBBillerPaymentConfigTasklet;

    @Override
    public Step buildStep() {
        return getDefaultStepBuilder("InitialDBBillerPaymentConfigStep")
                .tasklet(this.initialDBBillerPaymentConfigTasklet)
                .build();
    }
}
