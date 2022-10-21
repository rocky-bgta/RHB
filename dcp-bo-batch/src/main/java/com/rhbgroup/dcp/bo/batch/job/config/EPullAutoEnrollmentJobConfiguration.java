package com.rhbgroup.dcp.bo.batch.job.config;

import com.rhbgroup.dcp.bo.batch.framework.common.tasklet.UploadLocalFileToFTPServer;
import com.rhbgroup.dcp.bo.batch.framework.core.BaseDBToFileJobConfiguration;
import com.rhbgroup.dcp.bo.batch.job.config.properties.EPullAutoEnrollmentJobProperties;
import com.rhbgroup.dcp.bo.batch.job.step.EPullAutoEnrollmentStepBuilder;
import org.apache.log4j.Logger;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
@Lazy
public class EPullAutoEnrollmentJobConfiguration extends BaseDBToFileJobConfiguration {
    static final Logger logger = Logger.getLogger(EPullAutoEnrollmentJobConfiguration.class);

    private static final String JOBNAME = "EPullAutoEnrollmentJob";

    @Autowired
    EPullAutoEnrollmentJobProperties configProperties;
    @Autowired
    EPullAutoEnrollmentStepBuilder stepBuilder;
    @Autowired
    UploadLocalFileToFTPServer uploadTasklet;

    @Bean(name = JOBNAME)
    public Job buildJob() {


        return getDefaultJobBuilder(JOBNAME)
                .next(stepBuilder.buildStep())
                .next(sendToFTP())
                .build();
    }

    private Step sendToFTP() {
        String targetFileFolder = configProperties.getFtpfolder();
        String targetFileName = configProperties.getTxtProperty("filename");
        String targetFileDateFormat = configProperties.getNamedateformat();
        uploadTasklet.init(targetFileFolder, targetFileName, targetFileDateFormat);
        logger.info("targetFileFolder: " + targetFileFolder);
        logger.info("targetFileName: " + targetFileName);
        logger.info("targetFileDateFormat: " + targetFileDateFormat);;
        return getStepBuilderFactory().get("sendToFTP")
                .tasklet(uploadTasklet)
                .listener(this.batchJobCommonStepListener)
                .build();
    }

}
