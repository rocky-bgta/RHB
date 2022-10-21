package com.rhbgroup.dcp.bo.batch.job.step;

import com.rhbgroup.dcp.bo.batch.framework.core.BaseStepBuilder;
import com.rhbgroup.dcp.bo.batch.job.step.tasklet.GSTCentralizedFileUpdateJobValidatorTasklet;
import org.apache.log4j.Logger;
import org.springframework.batch.core.Step;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
@Lazy
public class GSTCentralizedFileUpdateJobValidatorStepBuilder extends BaseStepBuilder {
	
	private static final Logger logger = Logger.getLogger(GSTCentralizedFileUpdateJobValidatorStepBuilder.class);
	private static final String STEP_NAME="GSTCentralizedFileUpdateJobValidator";
	
	@Autowired
	private GSTCentralizedFileUpdateJobValidatorTasklet gstCentralizedFileUpdateJobValidatorTasklet;

	@Bean("GSTCentralizedFileUpdateJobValidator")
	public Step buildStep() {
		logger.info(String.format("Build step %s", STEP_NAME));
		Step gstCentralizedFileUpdateValidatorStep = getDefaultStepBuilder(STEP_NAME)
				.tasklet(this.gstCentralizedFileUpdateJobValidatorTasklet)
				.build();
		return gstCentralizedFileUpdateValidatorStep;
	}
}
