package com.rhbgroup.dcp.bo.batch.job.config;

import org.apache.log4j.Logger;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.job.builder.SimpleJobBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import com.rhbgroup.dcp.bo.batch.framework.core.BaseJobConfiguration;
import com.rhbgroup.dcp.bo.batch.job.step.LoadMassNotificationsFileToStagingStepBuilder;
import com.rhbgroup.dcp.bo.batch.job.step.MoveMassNotificationsFileToArchiveFolderStepBuilder;

@Configuration
@Lazy
public class LoadMassNotificationsJobConfiguration extends BaseJobConfiguration {
	
	private static final Logger logger = Logger.getLogger(LoadMassNotificationsJobConfiguration.class);
	
	private static final String JOB_NAME = "LoadMassNotificationsJob";
	
	@Autowired
	private LoadMassNotificationsFileToStagingStepBuilder loadMassNotificationsFileToStagingStepBuilder;
	
	@Autowired
	private MoveMassNotificationsFileToArchiveFolderStepBuilder moveMassNotificationsFileToArchiveFolderStepBuilder;
	
	@Bean(name = JOB_NAME)
	@Lazy
	public Job buildJob() {
		logger.info(String.format("Building job [%s]", JOB_NAME));
		
		SimpleJobBuilder job = getDefaultJobBuilder(JOB_NAME);
		job.next(loadMassNotificationsFileToStagingStepBuilder.buildStep());
		job.next(moveMassNotificationsFileToArchiveFolderStepBuilder.buildStep());
		
		logger.info(String.format("[%s] job build successfully", JOB_NAME));
		
		return job.build();
	}

}
