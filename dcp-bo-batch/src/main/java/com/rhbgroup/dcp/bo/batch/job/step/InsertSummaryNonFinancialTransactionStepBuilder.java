package com.rhbgroup.dcp.bo.batch.job.step;

import org.springframework.batch.core.Step;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.rhbgroup.dcp.bo.batch.framework.core.BaseStepBuilder;
import com.rhbgroup.dcp.bo.batch.job.step.tasklet.SummaryNonFinancialTransactionTasklet;

@Component
@Lazy
public class InsertSummaryNonFinancialTransactionStepBuilder extends BaseStepBuilder {
    @Autowired
    private SummaryNonFinancialTransactionTasklet summaryNonFinancialTransactionTasklet;

    @Override
    public Step buildStep() {
        return getDefaultStepBuilder("InsertSummaryNonFinancialTransactionStep")
                .tasklet(this.summaryNonFinancialTransactionTasklet)
                .build();
    }
}
