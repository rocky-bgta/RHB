package com.rhbgroup.dcp.bo.batch.job.config;

import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.SystemFolder.BATCH_SYSTEM_FOLDER_INPUT_DIRECTORY;

import org.apache.log4j.Logger;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.SimpleJobBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import com.rhbgroup.dcp.bo.batch.framework.common.tasklet.CopyFTPFileToInputFolderTasklet;
import com.rhbgroup.dcp.bo.batch.framework.core.BaseFileToDBJobConfiguration;
import com.rhbgroup.dcp.bo.batch.job.config.properties.FTPIBKConfigProperties;
import com.rhbgroup.dcp.bo.batch.job.config.properties.LoadIBKJompayEmatchJobConfigProperties;
import com.rhbgroup.dcp.bo.batch.job.step.LoadIBKJompayEmatchLoadFileToStagingStepBuilder;
import com.rhbgroup.dcp.bo.batch.job.step.LoadIBKJompayEmatchValidateFileStepBuilder;

@Configuration
@Lazy
public class LoadIBKJompayEmatchJobConfiguration  extends BaseFileToDBJobConfiguration{
	
	static final Logger logger = Logger.getLogger(LoadIBKJompayEmatchJobConfiguration.class);
	static final String JOB_NAME="LoadIBKJompayEmatchJob";
	
    @Value(BATCH_SYSTEM_FOLDER_INPUT_DIRECTORY)
    private String inputFolderFullPath;
    
	@Autowired
	private LoadIBKJompayEmatchJobConfigProperties configProperties;
	
	@Autowired
	private LoadIBKJompayEmatchValidateFileStepBuilder validateFileStep;
	
	@Autowired
	private LoadIBKJompayEmatchLoadFileToStagingStepBuilder loadFileToDBStep;
	
	@Autowired
    private FTPIBKConfigProperties ftpIBKConfigProperties;

	private Step downloadIBKJompayFileStep(String sourceFileFolder,String sourceFileName,String sourceFileDateFormat) {
        CopyFTPFileToInputFolderTasklet downloadIBKJompayTasklet= new CopyFTPFileToInputFolderTasklet();
        downloadIBKJompayTasklet.initFTPConfig(ftpIBKConfigProperties);
        downloadIBKJompayTasklet.initInputFolderPath(inputFolderFullPath);
        downloadIBKJompayTasklet.init(sourceFileFolder,sourceFileName,sourceFileDateFormat);
        return getStepBuilderFactory().get("downloadIBKJompayFromFTP")
                .tasklet(downloadIBKJompayTasklet)
                .listener(this.batchJobCommonStepListener)
                .build();
	}
	@Bean(name = JOB_NAME)
    public Job buildJob() {
    	String sourceFileFolder=configProperties.getFtpfolder();
    	String sourceFileName=configProperties.getName();
    	String sourceFileDateFormat=configProperties.getNamedateformat();
        Step copyFtpFileStep = downloadIBKJompayFileStep(sourceFileFolder,sourceFileName,sourceFileDateFormat);
        SimpleJobBuilder job = getDefaultFileToDBJobBuilder(JOB_NAME);
        job.next(copyFtpFileStep);
        job.next(validateFileStep.buildStep());
        job.next(loadFileToDBStep.buildStep());
        return job.build();
    }
    
}
