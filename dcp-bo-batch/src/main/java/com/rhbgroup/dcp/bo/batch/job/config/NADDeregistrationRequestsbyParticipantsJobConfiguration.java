package com.rhbgroup.dcp.bo.batch.job.config;

import com.rhbgroup.dcp.bo.batch.framework.core.BaseDBToFileJobConfiguration;
import com.rhbgroup.dcp.bo.batch.job.step.NADDeregistrationRequestsbyParticipantsJobStepBuilder;
import org.apache.log4j.Logger;
import org.springframework.batch.core.Job;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

@Configuration
@Lazy
public class NADDeregistrationRequestsbyParticipantsJobConfiguration extends BaseDBToFileJobConfiguration {
    static final Logger logger = Logger.getLogger(NADDeregistrationRequestsbyParticipantsJobStepBuilder.class);

    @Autowired
    private NADDeregistrationRequestsbyParticipantsJobStepBuilder nADDeregistrationRequestsbyParticipantsJobStepBuilder;

    @Value("${job.nadderegistrationrequestsbyparticipantsjob.ftpfolder}")
    private String targetFileFolder;
    @Value("${job.nadderegistrationrequestsbyparticipantsjob.name}")
    private String targetFileName;
    @Value("${job.nadderegistrationrequestsbyparticipantsjob.namedateformat}")
    private String targetFileDateFormat;

    @Bean(name = "NADDeregistrationRequestsbyParticipantsJob")
    public Job BuildJob() {
        Job job= getDefaultJobBuilder("NADDeregistrationRequestsbyParticipantsJob")
                .next(this.nADDeregistrationRequestsbyParticipantsJobStepBuilder.buildStep())
                .next(moveLocalFileToFTP(targetFileFolder,targetFileName,targetFileDateFormat))
                .build();

        String message = String.format("File is moved to FTP with targetFileFolder: %s, targetFile : %s, targetFileDateFormat : %s", targetFileFolder,targetFileName,targetFileDateFormat) ;
        logger.info(message);

        return job;
    }
}
