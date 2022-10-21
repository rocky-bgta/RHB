package com.rhbgroup.dcp.bo.batch.job.config;

import org.apache.log4j.Logger;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.job.builder.SimpleJobBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import com.rhbgroup.dcp.bo.batch.framework.core.BaseFileToDBJobConfiguration;
import com.rhbgroup.dcp.bo.batch.job.config.properties.LoadEMUnitTrustJobConfigProperties;
import com.rhbgroup.dcp.bo.batch.job.step.LoadEMUnitTrustAccountFileToDBStepBuilder;
import com.rhbgroup.dcp.bo.batch.job.step.LoadEMUnitTrustAccountHoldingFileToDBStepBuilder;
import com.rhbgroup.dcp.bo.batch.job.step.LoadEMUnitTrustCustomerFileToDBStepBuilder;
import com.rhbgroup.dcp.bo.batch.job.step.LoadEMUnitTrustCustomerRelationFileToDBStepBuilder;
import com.rhbgroup.dcp.bo.batch.job.step.LoadEMUnitTrustGetFileStepBuilder;
import com.rhbgroup.dcp.bo.batch.job.step.LoadEMUnitTrustFinalUpdateStepBuilder;
import com.rhbgroup.dcp.bo.batch.job.step.LoadEMUnitTrustFundMasterFileToDBStepBuilder;
import com.rhbgroup.dcp.bo.batch.job.step.LoadEMUnitTrustTargetDatasetStepBuilder;

@Configuration
@Lazy
public class LoadEMUnitTrustJobConfiguration extends BaseFileToDBJobConfiguration {
	
	private static final Logger logger = Logger.getLogger(LoadEMUnitTrustJobConfiguration.class);
	
	private static final String JOB_NAME = "LoadEMUnitTrustJob";
	
	@Autowired
	private LoadEMUnitTrustJobConfigProperties jobConfigProperties;
	
	@Autowired
	private LoadEMUnitTrustGetFileStepBuilder fileValidatorStep;
	
	@Autowired
	private LoadEMUnitTrustTargetDatasetStepBuilder targetDatasetStep;
	
	@Autowired
	private LoadEMUnitTrustCustomerFileToDBStepBuilder loadCustomerFileStep;
	
	@Autowired
	private LoadEMUnitTrustCustomerRelationFileToDBStepBuilder loadCustomerRelFileStep;
	
	@Autowired
	private LoadEMUnitTrustAccountFileToDBStepBuilder loadAccountFileStep;
	
	@Autowired
	private LoadEMUnitTrustAccountHoldingFileToDBStepBuilder loadAccountHldFileStep;
	
	@Autowired
	private LoadEMUnitTrustFundMasterFileToDBStepBuilder loadFundFileStep;
	
	@Autowired
	private LoadEMUnitTrustFinalUpdateStepBuilder finalUpdateStep;
	
	@Bean(JOB_NAME)
	@Lazy
	public Job buildJob() {
		logger.info(String.format("Building job [%s]", JOB_NAME));
		
		SimpleJobBuilder jobBuilder = getJobBuilderFactory().get(JOB_NAME)
			.incrementer(getDefaultIncrementer())
			.start(readBatchParameters())
			.listener(this.commonExecutionListener);
		
		jobBuilder.next(fileValidatorStep.buildStep());
		jobBuilder.next(targetDatasetStep.buildStep());
		jobBuilder.next(loadCustomerFileStep.buildStep());
		jobBuilder.next(loadCustomerRelFileStep.buildStep()); 
		jobBuilder.next(loadAccountFileStep.buildStep()); 
		jobBuilder.next(loadAccountHldFileStep.buildStep()); 
		jobBuilder.next(loadFundFileStep.buildStep()); 
		jobBuilder.next(finalUpdateStep.buildStep());
		Job job = jobBuilder.build();
		logger.info(String.format("[%s] job build successfully", JOB_NAME));
		return job;				
	}
	
}
