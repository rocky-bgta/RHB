package com.rhbgroup.dcp.bo.batch.job.config;

import org.apache.log4j.Logger;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.SimpleJobBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.rhbgroup.dcp.bo.batch.framework.core.BaseFileToDBJobConfiguration;
import com.rhbgroup.dcp.bo.batch.job.config.properties.LoadCardlinkNotificationsJobConfigProperties;
import com.rhbgroup.dcp.bo.batch.job.step.LoadCardlinkNotificationsFileToStagingStepBuilder;
import com.rhbgroup.dcp.bo.batch.job.step.LoadNotificationValidatorStepBuilder;
@Component
@Lazy
public class LoadCardlinkNotificationsJobConfiguration extends BaseFileToDBJobConfiguration{
	private static final Logger logger = Logger.getLogger(LoadCardlinkNotificationsJobConfiguration.class);
	private static final String JOB_NAME = "LoadCardlinkNotificationsJob";
	
	@Autowired
	private LoadCardlinkNotificationsJobConfigProperties configProperties ;
	
	@Autowired
	private LoadNotificationValidatorStepBuilder validatorStep;
	
	@Autowired
	private LoadCardlinkNotificationsFileToStagingStepBuilder loadFileStep;
	
	@Bean(name = JOB_NAME)
	public Job buildJob() {
		logger.info(String.format("Building job %s",JOB_NAME));
		SimpleJobBuilder job = getDefaultFileToDBJobBuilder(JOB_NAME);
		String sourceFileFolder = configProperties.getFtpFolder();
		String sourceFileName = configProperties.getName();
		String sourceFileDateFormat = configProperties.getNameDateFormat();
		int dayDiff = configProperties.getDayDiff();
		Step copyFtpFileStep = copyFTPFileToLocal(sourceFileFolder, sourceFileName, sourceFileDateFormat, dayDiff);
		job.next(copyFtpFileStep);
		job.next(validatorStep.buildStep());
		job.next(loadFileStep.buildStep());
		return job.build();
	}
}
