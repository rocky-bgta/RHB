package com.rhbgroup.dcp.bo.batch.job.config;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.log4j.Logger;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.SimpleJobBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import com.rhbgroup.dcp.bo.batch.framework.core.BaseJobConfiguration;
import com.rhbgroup.dcp.bo.batch.job.model.BillerPaymentInboundConfig;
import com.rhbgroup.dcp.bo.batch.job.repository.BatchBillerDynamicPaymentConfigRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.job.step.GetIBKBillerDynamicPaymentFileStepBuilder;
import com.rhbgroup.dcp.bo.batch.job.step.LoadIBKBillerDynamicPaymentFileToStagingStepBuilder;
import com.rhbgroup.dcp.bo.batch.job.step.tasklet.DeleteExistingIBKDynamicBillerTxnsTasklet;

@Configuration
@Lazy
public class LoadIBKBillerDynamicPaymentJobConfiguration extends BaseJobConfiguration{

private static final Logger logger = Logger.getLogger(LoadIBKBillerDynamicPaymentJobConfiguration.class);
	
	private static final String JOB_NAME = "LoadIBKBillerDynamicPaymentJob";
	
	@Autowired
	private GetIBKBillerDynamicPaymentFileStepBuilder getIBKBillerDynamicPaymentFileStepBuilder;
	
	@Autowired
	private LoadIBKBillerDynamicPaymentFileToStagingStepBuilder loadIBKBillerDynamicPaymentFileToStagingStepBuilder;

	@Autowired
	private DeleteExistingIBKDynamicBillerTxnsTasklet deleteExistingIBKDynamicBillerTxnsTasklet;

	@Bean("BillerPaymentInboundConfigQueue")
	@Lazy
	public Queue<BillerPaymentInboundConfig> getBatchBillerPaymentConfigQueue(BatchBillerDynamicPaymentConfigRepositoryImpl batchBillerPaymentConfigRepository) {
		return new ConcurrentLinkedQueue<>(batchBillerPaymentConfigRepository.getBillerConfigInbound());
	}
	
	@Bean(JOB_NAME)
	@Lazy
	public Job buildJob(@Qualifier("BillerPaymentInboundConfigQueue") Queue<BillerPaymentInboundConfig> queue) {
		logger.info(String.format("Building job [%s]", JOB_NAME));
		logger.info(String.format("BillerPaymentInboundConfig queue [%s]", queue));
		SimpleJobBuilder jobBuilder = getJobBuilderFactory().get(JOB_NAME)
				.incrementer(getDefaultIncrementer())
				.start(readBatchParameters())
				.listener(this.commonExecutionListener);
		
		for(int i=0; i< queue.size(); i++) {
			// Step to FTP get biller payment file
			Step getIBKBillerDynamicPaymentFileStep = getIBKBillerDynamicPaymentFileStepBuilder.buildStep();
			// Step to load biller payment file content to staging
			Step loadIBKBillerDynamicPaymentFileToStagingStep = loadIBKBillerDynamicPaymentFileToStagingStepBuilder.buildStep();
		
			jobBuilder
				.next(getIBKBillerDynamicPaymentFileStep)
				.next(deleteExistingIBKBillerDynamicTxnsStep())
				.next(loadIBKBillerDynamicPaymentFileToStagingStep);
		}
		
		Job job = jobBuilder.build();
		
		logger.info(String.format("[%s] job build successfully", JOB_NAME));
		return job;				
	}
	
	private Step deleteExistingIBKBillerDynamicTxnsStep() {
		return getStepBuilderFactory().get("deleteExistingIBKBillerTxnsStep")
			.tasklet(deleteExistingIBKDynamicBillerTxnsTasklet)
			.listener(batchJobCommonStepListener)
			.build();
	}
}
