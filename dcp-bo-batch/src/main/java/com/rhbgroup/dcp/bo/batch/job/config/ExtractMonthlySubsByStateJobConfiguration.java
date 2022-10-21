package com.rhbgroup.dcp.bo.batch.job.config;

import com.rhbgroup.dcp.bo.batch.framework.core.BaseDBToFileJobConfiguration;
import com.rhbgroup.dcp.bo.batch.job.step.ExtractMonthlySubsByStateStepBuilder;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.ReportJobContextParameter.REPORT_JOB_PARAMETER_REPORT_TARGET_PATH_KEY;

@Configuration
@Lazy
public class ExtractMonthlySubsByStateJobConfiguration extends BaseDBToFileJobConfiguration {

    private static final String JOBNAME = "ExtractMonthlySubsByStateJob";

    @Autowired
    ExtractMonthlySubsByStateStepBuilder extractMonthlySubsByStateStepBuilder;

    @Bean(name = JOBNAME)
    public Job buildJob() {
        return getDefaultJobBuilder(JOBNAME)
                .next(generateSteps())
                .next(uploadLocalFileToFtp())
                .build();
    }

    private Step generateSteps() {
        return extractMonthlySubsByStateStepBuilder.buildStep();
    }

    private Step uploadLocalFileToFtp() {
        moveLocalFileToFTPTasklet.initByContext(REPORT_JOB_PARAMETER_REPORT_TARGET_PATH_KEY);
        return getStepBuilderFactory().get("uploadLocalFileToFtp")
                .tasklet(this.moveLocalFileToFTPTasklet)
                .listener(this.batchJobCommonStepListener)
                .build();
    }
}