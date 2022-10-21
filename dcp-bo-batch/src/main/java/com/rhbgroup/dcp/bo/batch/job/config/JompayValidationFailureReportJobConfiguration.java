package com.rhbgroup.dcp.bo.batch.job.config;


import org.apache.log4j.Logger;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import com.rhbgroup.dcp.bo.batch.framework.common.tasklet.CopyOutputFolderFileToFTPTasklet;
import com.rhbgroup.dcp.bo.batch.framework.core.BaseDBToFileJobConfiguration;
import com.rhbgroup.dcp.bo.batch.job.config.properties.FTPIBKConfigProperties;
import com.rhbgroup.dcp.bo.batch.job.config.properties.JompayFailureValidationExtractionJobConfigProperties;
import com.rhbgroup.dcp.bo.batch.job.step.FailedJompayTxnsFromDBToFileStepBuider;

@Configuration
@Lazy
public class JompayValidationFailureReportJobConfiguration extends BaseDBToFileJobConfiguration {

	private static final Logger logger = Logger.getLogger(JompayValidationFailureReportJobConfiguration.class);
	
	private static final String JOB_NAME = "JompayValidationFailureReportJob";
	
	@Autowired
	private FailedJompayTxnsFromDBToFileStepBuider failedJompayTxnsFromDBToFileStepBuider;
	
	@Autowired
	private JompayFailureValidationExtractionJobConfigProperties jobConfigProperties;
	
	@Autowired
	private FTPIBKConfigProperties ftpIBKConfigProperties;
	
	private Step moveFileToFTPStep(String targetFileFolder, String targetFileName,String targetFileDateFormat) {
		CopyOutputFolderFileToFTPTasklet copyLocalFileToFTPTasklet = new CopyOutputFolderFileToFTPTasklet();
		copyLocalFileToFTPTasklet.initFTPConfig(ftpIBKConfigProperties);
		copyLocalFileToFTPTasklet.init(targetFileFolder,targetFileName,targetFileDateFormat);
        return getStepBuilderFactory().get("moveFileToFTPStep")
                .tasklet(copyLocalFileToFTPTasklet)
                .listener(this.batchJobCommonStepListener)
                .build();
	}
	
	@Bean(name = JOB_NAME)
    public Job buildJob() {
		logger.info(String.format("Building job [%s]", JOB_NAME));
		Job job= getDefaultJobBuilder(JOB_NAME)
                .next(failedJompayTxnsFromDBToFileStepBuider.buildStep())
                .next(moveFileToFTPStep(jobConfigProperties.getFtpFolder(), jobConfigProperties.getName(), jobConfigProperties.getNameDateFormat()))
                .build();
		
      	logger.info(String.format("[%s] job build successfully", JOB_NAME));
        return job;
    }
	
}
