package com.rhbgroup.dcp.bo.batch.job.config;

import org.springframework.batch.core.Job;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import com.rhbgroup.dcp.bo.batch.framework.core.BaseJobConfiguration;
import com.rhbgroup.dcp.bo.batch.job.step.UpdateBillerAccountStatusStepBuilder;

@Configuration
@Lazy
public class BillerMaintenanceAutoAgingJobConfiguration extends BaseJobConfiguration {
    private static final String JOB_NAME = "BillerMaintenanceAutoAgingJob";

    @Autowired
    UpdateBillerAccountStatusStepBuilder updateBillerAccountStatusStepBuilder;

    @Bean(JOB_NAME)
    public Job buildJob() {
       return getDefaultJobBuilder(JOB_NAME)
                .next(this.updateBillerAccountStatusStepBuilder.buildStep())
                .build();
    }
}
