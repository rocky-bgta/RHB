package com.rhbgroup.dcp.bo.batch.job.step;

import org.apache.log4j.Logger;
import org.springframework.batch.core.Step;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.rhbgroup.dcp.bo.batch.framework.core.BaseStepBuilder;
import com.rhbgroup.dcp.bo.batch.job.step.tasklet.IBGRejectStatusMoveFileTasklet;

@Component
@Lazy
public class IBGRejectStatusMoveFileStepBuilder extends BaseStepBuilder{
	
	private static final Logger logger = Logger.getLogger(IBGRejectStatusMoveFileStepBuilder.class);
	private static final String STEP_NAME="IBGRejectStatusMoveFile";
	
	@Autowired
	private IBGRejectStatusMoveFileTasklet moveFileTasklet;
	
	@Bean("IBGRejectStatusMoveFileStep")
	public Step buildStep() {
		logger.info(String.format("Build step %s", STEP_NAME));
		Step ibgRejectStatusMoveFileStep = getDefaultStepBuilder(STEP_NAME)
				.tasklet(this.moveFileTasklet)
				.build();
		return ibgRejectStatusMoveFileStep;
	}
}
