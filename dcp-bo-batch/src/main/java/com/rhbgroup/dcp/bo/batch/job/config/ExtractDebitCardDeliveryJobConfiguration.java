package com.rhbgroup.dcp.bo.batch.job.config;
import com.rhbgroup.dcp.bo.batch.job.config.properties.ExtractDebitCardDeliveryJobConfigProperties;
import com.rhbgroup.dcp.bo.batch.job.step.ExtractDebitCardDeliveryStepBuilder;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import com.rhbgroup.dcp.bo.batch.framework.core.BaseDBToFileJobConfiguration;

@Configuration
@Lazy
public class ExtractDebitCardDeliveryJobConfiguration extends BaseDBToFileJobConfiguration {
    private static final String JOBNAME = "ExtractDebitCardDeliveryJob";
    @Autowired
    private ExtractDebitCardDeliveryJobConfigProperties configProperties;
    @Autowired
    private ExtractDebitCardDeliveryStepBuilder ExtractDebitCardDeliveryStepBuilder;


    @Bean(name = JOBNAME)
    public Job buildJob() {
        String targetFileFolder = configProperties.getFtpfolder();
        String targetFileName = configProperties.getName();
        String targetFileDateFormat = configProperties.getNamedateformat();

        Step builderStep = this.ExtractDebitCardDeliveryStepBuilder.buildStep();
        Step moveStep = moveLocalFileToFTP(targetFileFolder,targetFileName,targetFileDateFormat);

        return getDefaultJobBuilder(JOBNAME)
                .next(builderStep)
                .next(moveStep)
                .build();
    }
}