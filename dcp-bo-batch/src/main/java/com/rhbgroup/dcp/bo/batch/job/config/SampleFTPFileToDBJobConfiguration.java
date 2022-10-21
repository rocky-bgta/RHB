package com.rhbgroup.dcp.bo.batch.job.config;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import com.rhbgroup.dcp.bo.batch.framework.core.BaseFileToDBJobConfiguration;
import com.rhbgroup.dcp.bo.batch.job.step.SampleJobCommitStagingStepBuilder;
import com.rhbgroup.dcp.bo.batch.job.step.SampleJobReadFileToStagingStepBuilder;

import org.springframework.beans.factory.annotation.Value;

@Configuration
@Lazy
public class SampleFTPFileToDBJobConfiguration extends BaseFileToDBJobConfiguration {
    @Value("${job.sample.from.ftp.sourcefile.folder}")
    private String sourceFileFolder;
    @Value("${job.sample.from.ftp.sourcefile.name}")
    private String sourceFileName;
    @Value("${job.sample.from.ftp.sourcefile.name.dateformat}")
    private String sourceFileDateFormat;

    @Autowired
    private SampleJobCommitStagingStepBuilder sampleJobCommitStagingStepBuilder;
    @Autowired
    private SampleJobReadFileToStagingStepBuilder sampleJobReadFileToStagingStepBuilder;


    @Bean(name = "SampleFTPFileToDBJob")
    public Job BuildJob() {
        Step readFileToStagingStep=this.sampleJobReadFileToStagingStepBuilder.buildStep();
        Step commitStagingStep=this.sampleJobCommitStagingStepBuilder.buildStep();
        Step finalStep=moveFiletoSuccessOrFailedFolder();

         Job job= getDefaultFileToDBJobBuilder("SampleFTPFileToDBJob")
                 .next(copyFTPFileToLocal(sourceFileFolder,sourceFileName,sourceFileDateFormat))
                 .next(readFileToStagingStep).on("FAILED").to(finalStep)
                 .from(readFileToStagingStep).on("*").to(commitStagingStep)
                 .from(commitStagingStep).on("*").to(finalStep)
                 .end()
                 .build();
        return job;
    }
}
