package com.rhbgroup.dcp.bo.batch.job.step.tasklet;


import com.rhbgroup.dcp.bo.batch.job.repository.SnapshotBoUsersGroupRepositoryImpl;
import org.apache.log4j.Logger;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import java.util.Date;


@Component
@Lazy
public class DeleteDuplicateSnapshotBoUsersGroupTasklet implements Tasklet {

    private static final Logger logger = Logger.getLogger(DeleteDuplicateSnapshotBoUsersGroupTasklet.class);

    @Autowired
    private SnapshotBoUsersGroupRepositoryImpl snapshotBoUsersGroupRepository;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        logger.info(String.format("Executing tasklet [%s]", this.getClass().getSimpleName()));

        int jobExecutionId = chunkContext.getStepContext().getStepExecution().getJobExecution().getId().intValue();
        Date createdTime = new Date();
        snapshotBoUsersGroupRepository.deleteUserGroupSameDayRecords(jobExecutionId,createdTime);

        return RepeatStatus.FINISHED;
    }
}
