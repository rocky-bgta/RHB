package com.rhbgroup.dcp.bo.batch.job.step;

import com.rhbgroup.dcp.bo.batch.framework.core.BaseStepBuilder;
import com.rhbgroup.dcp.bo.batch.job.step.tasklet.GSTCentralizedFileUpdateJobCommitStagingTasklet;
import org.springframework.batch.core.Step;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
@Lazy
public class GSTCentralizedFileUpdateJobCommitStagingStepBuilder extends BaseStepBuilder {

    @Autowired
    private GSTCentralizedFileUpdateJobCommitStagingTasklet gstCentralizedFileUpdateJobCommitStagingTasklet;

    @Override
    public Step buildStep() {
        return getDefaultStepBuilder("GSTCentralizedFileUpdateJobCommitStagingStep")
                .tasklet(this.gstCentralizedFileUpdateJobCommitStagingTasklet)
                .build();
    }
}
