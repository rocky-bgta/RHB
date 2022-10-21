package com.rhbgroup.dcp.bo.batch.job.config;
import org.apache.log4j.Logger;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import com.rhbgroup.dcp.bo.batch.framework.core.BaseDBToFileJobConfiguration;
import com.rhbgroup.dcp.bo.batch.job.step.ExtractDailyFirstTimeLoginStepBuilder;
import com.rhbgroup.dcp.bo.batch.job.step.ExtractDailyLoginStepBuilder;
import com.rhbgroup.dcp.bo.batch.job.step.ExtractDailyPodStepBuilder;
import com.rhbgroup.dcp.bo.batch.job.step.tasklet.ExtractDailyDataCopyFileToFTPFolder;

@Configuration
@Lazy
public class ExtractLoginDataJobConfiguration extends BaseDBToFileJobConfiguration {
    private static final String JOBNAME = "ExtractLoginDataJob";
    private static final String MOVE_LOCAL_FILES_TO_FTP_STEP_NAME = "MoveLocalFilesToFTPStep";

    @Autowired
    private ExtractDailyFirstTimeLoginStepBuilder extractDailyFirstTimeLoginStepBuilder;

    @Autowired
    private ExtractDailyLoginStepBuilder extractDailyLoginStepBuilder;

    @Autowired
    private ExtractDailyPodStepBuilder extractDailyPodStepBuilder;
    
    @Autowired
    private ExtractDailyDataCopyFileToFTPFolder copyFileToFTPTasklet;
    
    static final Logger logger = Logger.getLogger(ExtractLoginDataJobConfiguration.class);

    @Bean(name = JOBNAME)
    public Job buildJob() {
        Step firstTimeLoginBuilderStep = this.extractDailyFirstTimeLoginStepBuilder.buildStep();
        Step loginBuilderStep = this.extractDailyLoginStepBuilder.buildStep();
        Step podBuilderStep = this.extractDailyPodStepBuilder.buildStep();
        
        Step copyStep = getStepBuilderFactory().get(MOVE_LOCAL_FILES_TO_FTP_STEP_NAME)
        		.tasklet(copyFileToFTPTasklet)
        		.listener(this.batchJobCommonStepListener)
        		.build();

        return getDefaultJobBuilder(JOBNAME)
                .next(firstTimeLoginBuilderStep)
                .next(loginBuilderStep)
                .next(podBuilderStep)
                .next(copyStep)
                .build();
    }
}