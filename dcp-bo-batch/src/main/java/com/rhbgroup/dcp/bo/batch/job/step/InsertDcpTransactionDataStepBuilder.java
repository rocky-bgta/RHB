package com.rhbgroup.dcp.bo.batch.job.step;

import org.springframework.batch.core.Step;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.rhbgroup.dcp.bo.batch.framework.core.BaseStepBuilder;
import com.rhbgroup.dcp.bo.batch.job.step.tasklet.InsertDcpTransactionDataTasklet;
import com.rhbgroup.dcp.bo.batch.job.step.tasklet.UpdateBillerAccountStatusTasklet;

@Component
@Lazy
public class InsertDcpTransactionDataStepBuilder extends BaseStepBuilder {
    @Autowired
    private InsertDcpTransactionDataTasklet insertDcpTransactionDataTasklet;

    @Override
    public Step buildStep() {
        return getDefaultStepBuilder("InsertDcpTransactionDataStep")
                .tasklet(this.insertDcpTransactionDataTasklet)
                .build();
    }
}
