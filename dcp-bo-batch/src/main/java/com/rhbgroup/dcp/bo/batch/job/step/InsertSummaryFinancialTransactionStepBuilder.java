package com.rhbgroup.dcp.bo.batch.job.step;

import org.springframework.batch.core.Step;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.rhbgroup.dcp.bo.batch.framework.core.BaseStepBuilder;
import com.rhbgroup.dcp.bo.batch.job.step.tasklet.SummaryFinancialTransactionTasklet;

@Component
@Lazy
public class InsertSummaryFinancialTransactionStepBuilder extends BaseStepBuilder {
    @Autowired
    private SummaryFinancialTransactionTasklet summaryFinancialTransactionTasklet;

    @Override
    public Step buildStep() {
        return getDefaultStepBuilder("InsertSummaryFinancialTransactionStep")
                .tasklet(this.summaryFinancialTransactionTasklet)
                .build();
    }
}
