package com.rhbgroup.dcp.bo.batch.job.config;

import com.rhbgroup.dcp.bo.batch.framework.core.BaseRunReportJobConfiguration;
import com.rhbgroup.dcp.bo.batch.framework.repository.CommonStagingImpl;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.SimpleJobBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

@Configuration
@Lazy
public class ExtractCustomerEPullEnrollmentJobConfiguration extends BaseRunReportJobConfiguration {

    private static final String JOB_NAME = "ExtractCustomerEPullEnrollmentJob";
    private static final String EPULL_MAX_RETRY = "dcp.epull.max.retry";

    @Autowired
    CommonStagingImpl commonStagingImpl;

    private Tasklet extractCustomerTasklet = (stepContribution, chunkContext) -> {
        Long jobExecutionId = chunkContext.getStepContext().getStepExecution().getJobExecution().getId();
        String maxRetry = (String) chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().get(EPULL_MAX_RETRY);
        String truncateTable = "tbl_batch_staged_user_epull_enrollment";
        String insertIntoStagingTable = String.format(
                "insert into tbl_batch_staged_user_epull_enrollment (job_execution_id, user_id, epull_status, created_time, updated_time) " +
                        "select %d, id, 0, getdate(), getdate() from dcp.dbo.tbl_user_profile where epull_status <> 0 and epull_status <= %s", jobExecutionId, maxRetry);
        commonStagingImpl.executeStagingTable(truncateTable, insertIntoStagingTable);
        return RepeatStatus.FINISHED;
    };

    @Bean(name = JOB_NAME)
    public Job BuildJob() {
        SimpleJobBuilder jobBuilder = getDefaultJobBuilder(JOB_NAME)
                .next(extractCustomerSteps());
        return jobBuilder.build();
    }

    protected Step extractCustomerSteps() {
        return getStepBuilderFactory().get("extractCustomer")
                .tasklet(extractCustomerTasklet)
                .listener(this.batchJobCommonStepListener)
                .build();
    }
}
