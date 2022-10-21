package com.rhbgroup.dcp.bo.batch.job.step;

import org.apache.log4j.Logger;
import org.springframework.batch.core.Step;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.rhbgroup.dcp.bo.batch.framework.core.BaseStepBuilder;
import com.rhbgroup.dcp.bo.batch.job.step.tasklet.UpdateMsicConfigFromStaginMsicConfigTasklet;

@Component
@Lazy
public class UpdateMsicConfigFromStaginMsicConfigStepBuilder extends BaseStepBuilder {
	static final Logger logger = Logger.getLogger(UpdateMsicConfigFromStaginMsicConfigStepBuilder.class);
	static final String STEP_NAME = "UpdateMsicConfigFromStaginMsicConfigStep";

	@Autowired
	private UpdateMsicConfigFromStaginMsicConfigTasklet msicConfigTasklet;

	@Override
	@Bean(name = STEP_NAME)
	public Step buildStep() {

		return getDefaultStepBuilder(STEP_NAME).tasklet(msicConfigTasklet).build();
	}

}
