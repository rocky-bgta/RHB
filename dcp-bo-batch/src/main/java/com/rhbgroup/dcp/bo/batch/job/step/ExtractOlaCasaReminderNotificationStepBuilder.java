package com.rhbgroup.dcp.bo.batch.job.step;

import org.springframework.batch.core.Step;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.rhbgroup.dcp.bo.batch.framework.core.BaseStepBuilder;
import com.rhbgroup.dcp.bo.batch.job.step.tasklet.ExtractOlaCasaReminderNotificationTasklet;
import com.rhbgroup.dcp.bo.batch.job.step.tasklet.InsertBatchReportSummaryTasklet;
import com.rhbgroup.dcp.bo.batch.job.step.tasklet.UpdateBillerAccountStatusTasklet;

@Component
@Lazy
public class ExtractOlaCasaReminderNotificationStepBuilder extends BaseStepBuilder {
    @Autowired
    private ExtractOlaCasaReminderNotificationTasklet extractOlaCasaReminderNotificationTasklet;

    @Override
    public Step buildStep() {
        return getDefaultStepBuilder("ExtractOlaCasaReminderNotificationStep")
                .tasklet(this.extractOlaCasaReminderNotificationTasklet)
                .build();
    }
}
