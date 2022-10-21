package com.rhbgroup.dcp.bo.batch.framework.core;

import com.rhbgroup.dcp.bo.batch.framework.common.tasklet.RunReportWithDateRangeTasklet;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.SimpleJobBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import com.rhbgroup.dcp.bo.batch.framework.common.listener.BatchJobCommonExecutionListener;
import com.rhbgroup.dcp.bo.batch.framework.common.tasklet.ReadBatchParameterFromDBTasklet;
import com.rhbgroup.dcp.bo.batch.framework.common.tasklet.RunAsnbReportTasklet;
import com.rhbgroup.dcp.bo.batch.framework.common.tasklet.RunReportTasklet;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Configuration
public class BaseRunReportJobConfiguration extends BaseJobConfiguration{
    @Autowired
    private BatchJobCommonExecutionListener runReportJobListener;
    @Autowired
    private ReadBatchParameterFromDBTasklet readBatchParameterFromDBTasklet;
    @Autowired
    private RunReportTasklet runReportTasklet;
    @Autowired
    private RunReportWithDateRangeTasklet runReportWithDateRangeTasklet;
    @Autowired
    private RunAsnbReportTasklet runAsnbReportTasklet;

    protected Step readBatchParameters() {
        return getStepBuilderFactory().get("readBatchParameters")
                .tasklet(this.readBatchParameterFromDBTasklet)
                .listener(this.batchJobCommonStepListener)
                .build();
    }

	protected Step runReport() {
       return getStepBuilderFactory().get("runReport")
               .tasklet(this.runReportTasklet)
               .listener(this.batchJobCommonStepListener)
               .build();
    }
	
	protected Step runAsnbReportTasklet() {
	       return getStepBuilderFactory().get("runAsnbReport")
	               .tasklet(this.runAsnbReportTasklet)
	               .listener(this.batchJobCommonStepListener)
	               .build();
	    }

    protected Step runReportWithDateRange() {
        return getStepBuilderFactory().get("runReportWithDateRange")
                .tasklet(this.runReportWithDateRangeTasklet)
                .listener(this.batchJobCommonStepListener)
                .build();
    }
    
   
    
	
    protected SimpleJobBuilder getDefaultRunReportJobBuilder(String jobName)
    {
        return getJobBuilderFactory().get(jobName)
                .incrementer(getDefaultIncrementer())
                .start(readBatchParameters())
                .listener(this.runReportJobListener);
    }
}
