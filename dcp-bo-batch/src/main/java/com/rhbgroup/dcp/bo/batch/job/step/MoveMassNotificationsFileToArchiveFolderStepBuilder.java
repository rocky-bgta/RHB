package com.rhbgroup.dcp.bo.batch.job.step;

import org.apache.log4j.Logger;
import org.springframework.batch.core.Step;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.rhbgroup.dcp.bo.batch.framework.core.BaseStepBuilder;
import com.rhbgroup.dcp.bo.batch.job.step.tasklet.MoveFiletoArchiveFolderTasklet;

@Component
@Lazy
public class MoveMassNotificationsFileToArchiveFolderStepBuilder extends BaseStepBuilder {
	
	private static final Logger logger = Logger.getLogger(MoveMassNotificationsFileToArchiveFolderStepBuilder.class);
	
	private static final String STEP_NAME = "MoveMassNotificationsFileToArchiveFolderStep";
	
	@Autowired
	private MoveFiletoArchiveFolderTasklet moveFiletoArchiveFolderTasklet;

	@Override
	@Bean(name = STEP_NAME)
	public Step buildStep() {
		logger.info(String.format("Building step [%s]", STEP_NAME));
		
		Step step = getDefaultStepBuilder(STEP_NAME)
                .tasklet(moveFiletoArchiveFolderTasklet)
                .build();
		
		logger.info(String.format("[%s] step build successfully", STEP_NAME));
		return step;
	}

}
