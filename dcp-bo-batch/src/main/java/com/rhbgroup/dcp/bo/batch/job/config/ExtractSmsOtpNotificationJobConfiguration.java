package com.rhbgroup.dcp.bo.batch.job.config;
import org.apache.log4j.Logger;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import com.rhbgroup.dcp.bo.batch.framework.core.BaseDBToFileJobConfiguration;
import com.rhbgroup.dcp.bo.batch.job.step.ExtractSmsOtpNotificationStepBuilder;
import com.rhbgroup.dcp.bo.batch.job.step.tasklet.ExtractSmsOtpNotificationCopyFileToFTPFolder;

@Configuration
@Lazy
public class ExtractSmsOtpNotificationJobConfiguration extends BaseDBToFileJobConfiguration {
    private static final String JOBNAME = "ExtractSmsOtpNotificationJob";
    private static final String MOVE_LOCAL_FILES_TO_FTP_STEP_NAME = "MoveLocalFilesToFTPStep";

    @Autowired
    private ExtractSmsOtpNotificationStepBuilder extractSmsOtpNotificationStepBuilder;
    
    @Autowired
    private ExtractSmsOtpNotificationCopyFileToFTPFolder copyFileToFTPTasklet;
    
    static final Logger logger = Logger.getLogger(ExtractSmsOtpNotificationJobConfiguration.class);

    @Bean(name = JOBNAME)
    public Job buildJob() {
        Step firstTimeLoginBuilderStep = this.extractSmsOtpNotificationStepBuilder.buildStep();
        
        Step copyStep = getStepBuilderFactory().get(MOVE_LOCAL_FILES_TO_FTP_STEP_NAME)
        		.tasklet(copyFileToFTPTasklet)
        		.listener(this.batchJobCommonStepListener)
        		.build();

        return getDefaultJobBuilder(JOBNAME)
                .next(firstTimeLoginBuilderStep)
                .next(copyStep)
                .build();
    }
}