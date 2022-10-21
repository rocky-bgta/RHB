package com.rhbgroup.dcp.bo.batch.job.step;

import org.apache.log4j.Logger;
import org.springframework.batch.core.Step;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.rhbgroup.dcp.bo.batch.framework.core.BaseStepBuilder;
import com.rhbgroup.dcp.bo.batch.job.step.tasklet.UpdateDepositProductFromStaginDepositProductTasklet;

@Component
@Lazy
public class UpdateDepositProductFromStaginDepositProductStepBuilder extends BaseStepBuilder {
	static final Logger logger = Logger.getLogger(UpdateDepositProductFromStaginDepositProductStepBuilder.class);
	static final String STEP_NAME = "UpdateDepositProductFromStaginDepositProductStep";

	@Autowired
	private UpdateDepositProductFromStaginDepositProductTasklet depositProductTasklet;

	@Override
	@Bean(name = STEP_NAME)
	public Step buildStep() {

		return getDefaultStepBuilder(STEP_NAME).tasklet(depositProductTasklet).build();
	}

}
