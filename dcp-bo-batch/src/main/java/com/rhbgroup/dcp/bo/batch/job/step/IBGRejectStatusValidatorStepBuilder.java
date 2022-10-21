package com.rhbgroup.dcp.bo.batch.job.step;

import org.apache.log4j.Logger;
import org.springframework.batch.core.Step;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.rhbgroup.dcp.bo.batch.framework.core.BaseStepBuilder;
import com.rhbgroup.dcp.bo.batch.job.step.tasklet.IBGRejectStatusValidatorTasklet;

@Component
@Lazy
public class IBGRejectStatusValidatorStepBuilder extends BaseStepBuilder {
	
	private static final Logger logger = Logger.getLogger(IBGRejectStatusValidatorStepBuilder.class);
	private static final String STEP_NAME="IBGRejectStatusValidator";
	
	@Autowired
	private IBGRejectStatusValidatorTasklet ibgRejectStatusValidatorTasklet;

	@Bean("IBGRejectStatusValidator")
	public Step buildStep() {
		logger.info(String.format("Build step %s", STEP_NAME));
		Step ibgRejectStatusValidatorStep = getDefaultStepBuilder(STEP_NAME)
				.tasklet(this.ibgRejectStatusValidatorTasklet)
				.build();
		return ibgRejectStatusValidatorStep;
	}

}
