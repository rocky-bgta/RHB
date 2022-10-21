package com.rhbgroup.dcp.bo.batch.job.config;
import com.rhbgroup.dcp.bo.batch.framework.common.tasklet.CopyFTPFileToInputFolderTasklet;
import com.rhbgroup.dcp.bo.batch.framework.core.DcpBatchApplicationContext;
import com.rhbgroup.dcp.bo.batch.job.config.properties.FTPIBKPrepaidConfigProperties;
import com.rhbgroup.dcp.bo.batch.job.config.properties.PrepaidReloadFileFromIBKJobConfigProperties;
import com.rhbgroup.dcp.bo.batch.job.step.PrepaidReloadFileFromIBKStepBuilder;
import org.apache.log4j.Logger;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.SimpleJobBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import com.rhbgroup.dcp.bo.batch.framework.core.BaseFileToDBJobConfiguration;

import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.SystemFolder.BATCH_SYSTEM_FOLDER_INPUT_DIRECTORY;

@Configuration
@Lazy
public class PrepaidReloadFileFromIBKJobConfiguration extends BaseFileToDBJobConfiguration {

    private static final String JOBNAME = "LoadIBKPrepaidReloadJob";
    @Autowired
    private PrepaidReloadFileFromIBKJobConfigProperties configProperties;
    @Autowired
    private PrepaidReloadFileFromIBKStepBuilder prepaidReloadFileFromIBKStepBuilder;
    @Autowired
    private FTPIBKPrepaidConfigProperties ftpConfigProperties;
    @Autowired
    private DcpBatchApplicationContext dcpBatchApplicationContext;
    @Value(BATCH_SYSTEM_FOLDER_INPUT_DIRECTORY)
    private String inputFolderFullPath;

    @Bean
    protected Step downloadFromFTPCASA(String targetFileFolder, String  targetFileName, String  targetFileDateFormat) {
        CopyFTPFileToInputFolderTasklet downloadFromFTPTaskletCASA = new CopyFTPFileToInputFolderTasklet();
        downloadFromFTPTaskletCASA.initFTPConfig(ftpConfigProperties);
        downloadFromFTPTaskletCASA.initInputFolderPath(inputFolderFullPath);
        downloadFromFTPTaskletCASA.init(targetFileFolder,targetFileName,targetFileDateFormat);
        return getStepBuilderFactory().get("downloadFromFTPCASA")
                .tasklet(downloadFromFTPTaskletCASA)
                .listener(this.batchJobCommonStepListener)
                .build();
    }

    @Bean
    protected Step downloadFromFTPCC(String targetFileFolder, String  targetFileName, String  targetFileDateFormat) {
        CopyFTPFileToInputFolderTasklet downloadFromFTPTaskletCC = new CopyFTPFileToInputFolderTasklet();
        downloadFromFTPTaskletCC.initFTPConfig(ftpConfigProperties);
        downloadFromFTPTaskletCC.initInputFolderPath(inputFolderFullPath);
        downloadFromFTPTaskletCC.init(targetFileFolder,targetFileName,targetFileDateFormat);
        return getStepBuilderFactory().get("downloadFromFTPCC")
                .tasklet(downloadFromFTPTaskletCC)
                .listener(this.batchJobCommonStepListener)
                .build();
    }

    @Bean(name = JOBNAME)
    public Job buildJob() {

        String sourceFileFolder = configProperties.getFtpfolder();
        String sourceFileNameCASA = configProperties.getNamecasa();
        String sourceFileNameCC = configProperties.getNamecc();
        String sourceFileDateFormat = configProperties.getNamedateformat();
        Step copyFtpFileStepCASA = downloadFromFTPCASA(sourceFileFolder,sourceFileNameCASA,sourceFileDateFormat);
        Step copyFtpFileStepCC = downloadFromFTPCC(sourceFileFolder,sourceFileNameCC,sourceFileDateFormat);
        Step readFileToStagingStep=this.prepaidReloadFileFromIBKStepBuilder.buildStep();
        Step moveFiletoSuccessOrFailedFolderStep=moveFiletoSuccessOrFailedFolder();
        SimpleJobBuilder job = getDefaultFileToDBJobBuilder(JOBNAME);

        job.next(copyFtpFileStepCASA);
        job.next(readFileToStagingStep);
        job.next(moveFiletoSuccessOrFailedFolderStep);
        job.next(copyFtpFileStepCC);
        job.next(readFileToStagingStep);
        job.next(moveFiletoSuccessOrFailedFolderStep);
        return job.build();
    }
}