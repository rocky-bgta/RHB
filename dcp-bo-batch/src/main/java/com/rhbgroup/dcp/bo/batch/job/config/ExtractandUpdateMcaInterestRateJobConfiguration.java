package com.rhbgroup.dcp.bo.batch.job.config;

import org.apache.log4j.Logger;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import com.rhbgroup.dcp.bo.batch.framework.core.BaseJobConfiguration;
import com.rhbgroup.dcp.bo.batch.job.step.ExtractInterestRateToMcaInterestRateTableStepBuilder;
import com.rhbgroup.dcp.bo.batch.job.step.UpdateMcaCurrencyInterestRateConfigStepBuilder;

@Configuration
@Lazy
public class ExtractandUpdateMcaInterestRateJobConfiguration extends BaseJobConfiguration {

	private static final Logger logger = Logger.getLogger(ExtractandUpdateMcaInterestRateJobConfiguration.class);
	
	private static final String JOBNAME = "ExtractandUpdateMcaInterestRateJob";
	
	@Autowired
	private ExtractInterestRateToMcaInterestRateTableStepBuilder extractInterestRateToMcaInterestRateTableStepBuilder;
	
	@Autowired
	private UpdateMcaCurrencyInterestRateConfigStepBuilder updateMcaCurrencyInterestRateConfigStepBuilder;
	
	@Bean(name = JOBNAME)
	public Job buildJob() {
		logger.info(String.format("Building job [%s]", JOBNAME));
		Step mcaIntstRtTblStep = this.extractInterestRateToMcaInterestRateTableStepBuilder.buildStep();
		Step updateMcaCrrncyInsrtRtStepBuilder = this.updateMcaCurrencyInterestRateConfigStepBuilder.buildStep();
		
		Job job = getDefaultJobBuilder(JOBNAME)
				.next(mcaIntstRtTblStep)
				.next(updateMcaCrrncyInsrtRtStepBuilder)
				.build();
		
		logger.info(String.format("[%s] job build successfully", JOBNAME));
		return job;
	}
	
	
}
