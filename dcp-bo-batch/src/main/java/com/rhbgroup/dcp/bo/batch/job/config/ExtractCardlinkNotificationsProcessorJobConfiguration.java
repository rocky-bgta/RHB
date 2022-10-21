package com.rhbgroup.dcp.bo.batch.job.config;

import org.apache.log4j.Logger;
import org.springframework.batch.core.Job;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.rhbgroup.dcp.bo.batch.framework.core.BaseJobConfiguration;
import com.rhbgroup.dcp.bo.batch.job.step.ExtractAndStoreCardlinkNotificationStepBuilder;

/**
 * DCPBL-15269
 * @author simon sew
 *
 */
@Component
@Lazy
public class ExtractCardlinkNotificationsProcessorJobConfiguration extends BaseJobConfiguration {
	
	private static final Logger logger = Logger.getLogger(ExtractCardlinkNotificationsProcessorJobConfiguration.class);
	
	private static final String JOB_NAME = "ExtractCardlinkNotificationsProcessorJob";
	
	@Autowired
	private ExtractAndStoreCardlinkNotificationStepBuilder extractAndStoreCardLinkNotificationStepBuilder;
		
	@Bean(name = JOB_NAME)
    public Job buildJob() {
		logger.info(String.format("Building job [%s]", JOB_NAME));
		Job job= getDefaultJobBuilder(JOB_NAME)
                .next(extractAndStoreCardLinkNotificationStepBuilder.buildStep())
                .build();
		
      	logger.info(String.format("[%s] job build successfully", JOB_NAME));
        return job;
    }
}	