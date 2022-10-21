package com.rhbgroup.dcp.bo.batch.job.step;

import org.springframework.batch.core.Step;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.rhbgroup.dcp.bo.batch.framework.core.BaseStepBuilder;
import com.rhbgroup.dcp.bo.batch.job.step.tasklet.InsertBatchReportSummaryTasklet;

@Component
@Lazy
public class InsertBatchReportSummaryStepBuilder extends BaseStepBuilder {
    @Autowired
    private InsertBatchReportSummaryTasklet insertBatchReportSummaryTasklet;

    @Override
    public Step buildStep() {
        return getDefaultStepBuilder("InsertBatchReportSummaryStep")
                .tasklet(this.insertBatchReportSummaryTasklet)
                .build();
    }
}
