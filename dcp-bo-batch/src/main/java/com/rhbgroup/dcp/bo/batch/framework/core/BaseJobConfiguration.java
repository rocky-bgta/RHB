package com.rhbgroup.dcp.bo.batch.framework.core;

import javax.sql.DataSource;

import org.springframework.batch.core.JobParametersIncrementer;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.job.builder.SimpleJobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import com.rhbgroup.dcp.bo.batch.framework.common.listener.BatchJobCommonExecutionListener;
import com.rhbgroup.dcp.bo.batch.framework.common.listener.BatchJobCommonStepListener;
import com.rhbgroup.dcp.bo.batch.framework.common.tasklet.ReadBatchParameterFromDBTasklet;

import lombok.Getter;

@Getter
@EnableBatchProcessing
public class BaseJobConfiguration {
    @Autowired
    protected ReadBatchParameterFromDBTasklet readBatchParameterFromDBTasklet;

    @Autowired
    protected BatchJobCommonExecutionListener commonExecutionListener;

    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Autowired
    private DataSource dataSource;

    @Autowired
    protected JobBuilderFactory jobBuilderFactory;

    @Autowired
    protected StepBuilderFactory stepBuilderFactory;

    @Autowired
    protected BatchJobCommonStepListener batchJobCommonStepListener;
    
    protected JobParametersIncrementer getDefaultIncrementer() {
        // Simple Spring-provided JobParametersIncrementer implementation that increments the run id.
        return new RunIdIncrementer();
    }

    protected Step readBatchParameters() {
        return getStepBuilderFactory().get("readBatchParameters")
                .tasklet(this.readBatchParameterFromDBTasklet)
                .listener(this.batchJobCommonStepListener)
                .build();
    }

    protected SimpleJobBuilder getDefaultJobBuilder(String jobName)
    {
        return getJobBuilderFactory().get(jobName)
                .incrementer(getDefaultIncrementer())
                .start(readBatchParameters())
                .listener(this.commonExecutionListener);
    }
    
}
