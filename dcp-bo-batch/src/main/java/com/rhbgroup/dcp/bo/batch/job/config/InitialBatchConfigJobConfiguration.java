package com.rhbgroup.dcp.bo.batch.job.config;

import org.springframework.batch.core.Job;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import com.rhbgroup.dcp.bo.batch.framework.core.BaseJobConfiguration;
import com.rhbgroup.dcp.bo.batch.job.step.InitialDBBatchSystemDateStepBuilder;
import com.rhbgroup.dcp.bo.batch.job.step.InitialDBBillerPaymentConfigStepBuilder;
import com.rhbgroup.dcp.bo.batch.job.step.InitialDBBillerPaymentFileStepBuilder;

@Configuration
@Lazy
public class InitialBatchConfigJobConfiguration extends BaseJobConfiguration {
    private static final String JOB_NAME = "InitialBatchConfigJob";

    @Autowired
    InitialDBBatchSystemDateStepBuilder initialDBBatchSystemDateStepBuilder;
    @Autowired
    InitialDBBillerPaymentConfigStepBuilder initialDBBillerPaymentConfigStepBuilder;
    @Autowired
    InitialDBBillerPaymentFileStepBuilder initialDBBillerPaymentFileStepBuilder;

    @Bean(JOB_NAME)
    public Job buildJob() {
       return getDefaultJobBuilder(JOB_NAME)
                .next(this.initialDBBatchSystemDateStepBuilder.buildStep())
                .next(this.initialDBBillerPaymentConfigStepBuilder.buildStep())
                .next(this.initialDBBillerPaymentFileStepBuilder.buildStep())
                .build();
    }
}
