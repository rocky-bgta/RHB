package com.rhbgroup.dcp.bo.batch.job.config;

import org.apache.log4j.Logger;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import com.rhbgroup.dcp.bo.batch.framework.core.BaseJobConfiguration;
import com.rhbgroup.dcp.bo.batch.job.step.ExtractCurrencyRateToStagingTableStepBuilder;
import com.rhbgroup.dcp.bo.batch.job.step.UpdateCurrencyRateConfigStepBuilder;

@Configuration
@Lazy
public class ExtractAndUpdateExchangeRateJobConfiguration extends BaseJobConfiguration {
	private static final Logger logger = Logger.getLogger(ExtractAndUpdateExchangeRateJobConfiguration.class);

	private static final String JOBNAME = "ExtractAndUpdateExchangeRateJob";
	@Autowired
	private ExtractCurrencyRateToStagingTableStepBuilder extractCurrencyRateToStagingStepBuilder;
	@Autowired
	private UpdateCurrencyRateConfigStepBuilder updateCurrencyRateConfigStepBuilder;

	@Bean(name = JOBNAME)
	public Job buildJob() {
		logger.info(String.format("Building job [%s]", JOBNAME));
		Step stagingStep = this.extractCurrencyRateToStagingStepBuilder.buildStep();
		Step updateStagingStep = this.updateCurrencyRateConfigStepBuilder.buildStep();
		
		Job job = getDefaultJobBuilder(JOBNAME)
				.next(stagingStep)
				.next(updateStagingStep)
				.build();
		
		logger.info(String.format("[%s] job build successfully", JOBNAME));
		return job;
	}

}
