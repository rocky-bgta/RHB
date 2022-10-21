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
import com.rhbgroup.dcp.bo.batch.job.model.BatchBillerPaymentConfig;
import com.rhbgroup.dcp.bo.batch.job.repository.BatchBillerPaymentConfigRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.job.step.GetIBKBillerPaymentFileStepBuilder;
import com.rhbgroup.dcp.bo.batch.job.step.LoadIBKBillerPaymentFileToStagingStepBuilder;
import com.rhbgroup.dcp.bo.batch.job.step.MoveIBKBillerPaymentFileToSuccessFolderStepBuilder;
import com.rhbgroup.dcp.bo.batch.job.step.tasklet.DeleteExistingIBKBillerTxnsTasklet;

@Configuration
@Lazy
public class LoadIBKBillerPaymentJobConfiguration extends BaseJobConfiguration {
	
	private static final Logger logger = Logger.getLogger(LoadIBKBillerPaymentJobConfiguration.class);
	
	private static final String JOB_NAME = "LoadIBKBillerPaymentJob";
	
	@Autowired
	private GetIBKBillerPaymentFileStepBuilder getIBKBillerPaymentFileStepBuilder;
	
	@Autowired
	private LoadIBKBillerPaymentFileToStagingStepBuilder loadIBKBillerPaymentFileToStagingStepBuilder;
	
	@Autowired
	private MoveIBKBillerPaymentFileToSuccessFolderStepBuilder moveIBKBillerPaymentFileToSuccessFolderStepBuilder;
	
	@Autowired
	private DeleteExistingIBKBillerTxnsTasklet deleteExistingIBKBillerTxnsTasklet;
	
	@Bean("BatchBillerPaymentConfigQueue")
	@Lazy
	public Queue<BatchBillerPaymentConfig> getBatchBillerPaymentConfigQueue(BatchBillerPaymentConfigRepositoryImpl batchBillerPaymentConfigRepository) {
		return new ConcurrentLinkedQueue<>(batchBillerPaymentConfigRepository.getBatchBillerPaymentConfigsForLoadIBKBillerPaymentJob());
	}
	
	@Bean(JOB_NAME)
	@Lazy
	public Job buildJob(@Qualifier("BatchBillerPaymentConfigQueue") Queue<BatchBillerPaymentConfig> queue) {
		logger.info(String.format("Building job [%s]", JOB_NAME));
		logger.debug(String.format("BatchBillerPaymentConfig queue [%s]", queue));
		SimpleJobBuilder jobBuilder = getJobBuilderFactory().get(JOB_NAME)
				.incrementer(getDefaultIncrementer())
				.start(readBatchParameters())
				.listener(this.commonExecutionListener);
		
		
		for(int i=0; i< queue.size(); i++) {
			// Step to FTP get biller payment file
			Step getIBKBillerPaymentFileStep = getIBKBillerPaymentFileStepBuilder.buildStep();
			// Step to load biller payment file content to staging
			Step loadIBKBillerFileToStagingStep = loadIBKBillerPaymentFileToStagingStepBuilder.buildStep();
			// Step to store the input file to the success folder if everything positive
			Step moveIBKBillerPaymentFileToSuccessFolderStep = moveIBKBillerPaymentFileToSuccessFolderStepBuilder.buildStep();
			
			jobBuilder
				.next(getIBKBillerPaymentFileStep)
				.next(deleteExistingIBKBillerTxnsStep())
				.next(loadIBKBillerFileToStagingStep)
				.next(moveIBKBillerPaymentFileToSuccessFolderStep);
		}
		
		Job job = jobBuilder.build();
		
		logger.info(String.format("[%s] job build successfully", JOB_NAME));
		return job;				
	}
	
	private Step deleteExistingIBKBillerTxnsStep() {
		return getStepBuilderFactory().get("deleteExistingIBKBillerTxnsStep")
			.tasklet(deleteExistingIBKBillerTxnsTasklet)
			.listener(batchJobCommonStepListener)
			.build();
	}
	
}
