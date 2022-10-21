package com.rhbgroup.dcp.bo.batch.job.config;

import com.rhbgroup.dcp.bo.batch.framework.common.tasklet.CopyOutputFolderFileToFTPTasklet;
import com.rhbgroup.dcp.bo.batch.framework.config.properties.FTPConfigProperties;
import com.rhbgroup.dcp.bo.batch.job.config.properties.PrepaidReloadExtractionJobConfigProperties;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import com.rhbgroup.dcp.bo.batch.framework.core.BaseDBToFileJobConfiguration;
import com.rhbgroup.dcp.bo.batch.job.step.PrepaidReloadExtractionCASAStepBuilder;
import com.rhbgroup.dcp.bo.batch.job.step.PrepaidReloadExtractionCCStepBuilder;

@Configuration
@Lazy
public class PrepaidReloadExtractionJobConfiguration extends BaseDBToFileJobConfiguration {

    private static final String JOBNAME = "PrepaidReloadExtractionJob";
    @Autowired
    private PrepaidReloadExtractionJobConfigProperties configProperties;
    @Autowired
    private PrepaidReloadExtractionCASAStepBuilder prepaidReloadExtractionCASAStepBuilder;
    @Autowired
    private PrepaidReloadExtractionCCStepBuilder prepaidReloadExtractionCCStepBuilder;
    @Autowired
    private FTPConfigProperties ftpConfigProperties;

    @Bean
    protected Step moveLocalFileToFTPCASA(String targetFileFolder, String  targetFileName, String  targetFileDateFormat) {
        CopyOutputFolderFileToFTPTasklet moveLocalFileToFTPTaskletCASA = new CopyOutputFolderFileToFTPTasklet();
        moveLocalFileToFTPTaskletCASA.initFTPConfig(ftpConfigProperties);
        moveLocalFileToFTPTaskletCASA.init(targetFileFolder,targetFileName,targetFileDateFormat);
        return getStepBuilderFactory().get("moveLocalFileToFTPCASA")
                .tasklet(moveLocalFileToFTPTaskletCASA)
                .listener(this.batchJobCommonStepListener)
                .build();
    }
    @Bean
    protected Step moveLocalFileToFTPCC(String targetFileFolder, String  targetFileName, String  targetFileDateFormat) {
        CopyOutputFolderFileToFTPTasklet moveLocalFileToFTPTaskletCC = new CopyOutputFolderFileToFTPTasklet();
        moveLocalFileToFTPTaskletCC.initFTPConfig(ftpConfigProperties);
        moveLocalFileToFTPTaskletCC.init(targetFileFolder,targetFileName,targetFileDateFormat);
        return getStepBuilderFactory().get("moveLocalFileToFTPCC")
                .tasklet(moveLocalFileToFTPTaskletCC)
                .listener(this.batchJobCommonStepListener)
                .build();
    }

    @Bean(name = JOBNAME)
    public Job buildJob() {

        String targetFileFolder = configProperties.getFtpfolder();
        String targetFileNameCASA = configProperties.getNamecasa();
        String targetFileNameCC = configProperties.getNamecc();
        String targetFileDateFormat = configProperties.getNamedateformat();

        return getDefaultJobBuilder(JOBNAME)
                .next(this.prepaidReloadExtractionCASAStepBuilder.buildStep())
                .next(moveLocalFileToFTPCASA(targetFileFolder,targetFileNameCASA,targetFileDateFormat))
                .next(this.prepaidReloadExtractionCCStepBuilder.buildStep())
                .next(moveLocalFileToFTPCC(targetFileFolder,targetFileNameCC,targetFileDateFormat))
                .build();
    }
}
