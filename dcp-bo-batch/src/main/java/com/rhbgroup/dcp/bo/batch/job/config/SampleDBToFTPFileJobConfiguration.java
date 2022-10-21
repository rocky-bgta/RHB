package com.rhbgroup.dcp.bo.batch.job.config;
import org.springframework.batch.core.Job;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import com.rhbgroup.dcp.bo.batch.framework.core.BaseDBToFileJobConfiguration;
import com.rhbgroup.dcp.bo.batch.job.step.SampleJobReadDBToFileStepBuilder;

@Configuration
@Lazy
public class SampleDBToFTPFileJobConfiguration extends BaseDBToFileJobConfiguration {
    @Autowired
    private SampleJobReadDBToFileStepBuilder sampleJobReadDBToFileStepBuilder;
    @Value("${job.sample.to.ftp.targetfile.folder}")
    private String targetFileFolder;
    @Value("${job.sample.to.ftp.targetfile.name}")
    private String targetFileName;
    @Value("${job.sample.to.ftp.targetfile.name.dateformat}")
    private String targetFileDateFormat;


    @Bean(name = "SampleDBToFTPFileJob")
    public Job BuildJob() {
      Job job= getDefaultJobBuilder("SampleDBToFTPFileJob")
                .next(this.sampleJobReadDBToFileStepBuilder.buildStep())
                .next(moveLocalFileToFTP(targetFileFolder,targetFileName,targetFileDateFormat))
                .build();

        return job;
    }
}
