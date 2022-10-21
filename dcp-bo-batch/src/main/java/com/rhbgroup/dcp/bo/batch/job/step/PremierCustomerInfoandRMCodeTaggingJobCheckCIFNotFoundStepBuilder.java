package com.rhbgroup.dcp.bo.batch.job.step;

import com.rhbgroup.dcp.bo.batch.framework.core.BaseStepBuilder;
import com.rhbgroup.dcp.bo.batch.job.step.tasklet.PremierCustomerInfoandRMCodeTaggingJobCheckCIFNotFoundTasklet;
import org.apache.log4j.Logger;
import org.springframework.batch.core.Step;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
@Lazy
public class PremierCustomerInfoandRMCodeTaggingJobCheckCIFNotFoundStepBuilder extends BaseStepBuilder {
	
	private static final Logger logger = Logger.getLogger(PremierCustomerInfoandRMCodeTaggingJobCheckCIFNotFoundStepBuilder.class);
	private static final String STEP_NAME="PremierCustomerInfoandRMCodeTaggingJobCheckCIFNotFound";
	
	@Autowired
	private PremierCustomerInfoandRMCodeTaggingJobCheckCIFNotFoundTasklet premierCustomerInfoandRMCodeTaggingJobCheckCIFNotFoundTasklet;

	@Bean(STEP_NAME)
	public Step buildStep() {
		logger.info(String.format("Build step %s", STEP_NAME));
		Step premierCustomerInfoandRMCodeTaggingValidatorStep = getDefaultStepBuilder(STEP_NAME)
				.tasklet(this.premierCustomerInfoandRMCodeTaggingJobCheckCIFNotFoundTasklet)
				.build();
		return premierCustomerInfoandRMCodeTaggingValidatorStep;
	}
}
