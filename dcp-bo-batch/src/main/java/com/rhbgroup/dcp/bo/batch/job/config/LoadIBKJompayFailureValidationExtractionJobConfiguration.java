package com.rhbgroup.dcp.bo.batch.job.config;

import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.SystemFolder.BATCH_SYSTEM_FOLDER_INPUT_DIRECTORY;

import org.apache.log4j.Logger;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import com.rhbgroup.dcp.bo.batch.framework.common.tasklet.CopyFTPFileToInputFolderTasklet;
import com.rhbgroup.dcp.bo.batch.framework.core.BaseFileToDBJobConfiguration;
import com.rhbgroup.dcp.bo.batch.job.config.properties.FTPIBKConfigProperties;
import com.rhbgroup.dcp.bo.batch.job.config.properties.LoadIBKJompayFailureValidationExtractionJobConfigProperties;
import com.rhbgroup.dcp.bo.batch.job.step.LoadIBKJompayFailureDeleteExistingBatchStepBuilder;
import com.rhbgroup.dcp.bo.batch.job.step.LoadIBKJompayFailureValidationExtractionStepBuilder;

@Configuration
@Lazy
public class LoadIBKJompayFailureValidationExtractionJobConfiguration extends BaseFileToDBJobConfiguration {

	private static final Logger logger = Logger.getLogger(LoadIBKJompayFailureValidationExtractionJobConfiguration.class);
	
	private static final String JOB_NAME = "LoadIBKJompayFailureValidationExtractionJob";
	
    @Value(BATCH_SYSTEM_FOLDER_INPUT_DIRECTORY)
    private String inputFolderFullPath;
    
	@Autowired
	private LoadIBKJompayFailureValidationExtractionStepBuilder loadIBKJompayFailureValidationExtractionStepBuilder;
	
	@Autowired
	private	LoadIBKJompayFailureDeleteExistingBatchStepBuilder loadIBKJompayFailureDeleteExistingBatchStepBuilder;
	
	@Autowired
	private LoadIBKJompayFailureValidationExtractionJobConfigProperties jobConfigProperties;
	
	@Autowired
    private FTPIBKConfigProperties ftpIBKConfigProperties;
	
	private Step downloadIBKFailureFileStep(String sourceFileFolder,String sourceFileName,String sourceFileDateFormat) {
        CopyFTPFileToInputFolderTasklet downloadIBKFailureTasklet= new CopyFTPFileToInputFolderTasklet();
        downloadIBKFailureTasklet.initFTPConfig(ftpIBKConfigProperties);
        downloadIBKFailureTasklet.initInputFolderPath(inputFolderFullPath);
        downloadIBKFailureTasklet.init(sourceFileFolder,sourceFileName,sourceFileDateFormat);
        downloadIBKFailureTasklet.initDayDiff(jobConfigProperties.getDayDiff());
        return getStepBuilderFactory().get("downloadIBKFailureFromFTP")
                .tasklet(downloadIBKFailureTasklet)
                .listener(this.batchJobCommonStepListener)
                .build();
	}
	
	@Bean(JOB_NAME)
	@Lazy
	public Job buildJob() {
		logger.info(String.format("Building job [%s]", JOB_NAME));
		Job job = getDefaultFileToDBJobBuilder(JOB_NAME)
				.next(downloadIBKFailureFileStep(jobConfigProperties.getFtpFolder(), jobConfigProperties.getName(), jobConfigProperties.getNameDateFormat()))
				.next(loadIBKJompayFailureDeleteExistingBatchStepBuilder.buildStep())
				.next(loadIBKJompayFailureValidationExtractionStepBuilder.buildStep())
				.next(moveFiletoSuccessOrFailedFolder())
				.build();
		
		logger.info(String.format("[%s] job build successfully", JOB_NAME));
		return job;				
	}
	
}
