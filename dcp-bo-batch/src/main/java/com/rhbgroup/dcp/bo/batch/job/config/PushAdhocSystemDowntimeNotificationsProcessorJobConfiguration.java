package com.rhbgroup.dcp.bo.batch.job.config;

import javax.naming.NamingException;

import org.apache.log4j.Logger;
import org.springframework.batch.core.Job;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import com.rhbgroup.dcp.bo.batch.framework.config.properties.BaseJobConfigProperties;
import com.rhbgroup.dcp.bo.batch.framework.config.properties.JMSConfigProperties;
import com.rhbgroup.dcp.bo.batch.framework.core.BaseJobConfiguration;
import com.rhbgroup.dcp.bo.batch.framework.core.JmsConfiguration;
import com.rhbgroup.dcp.bo.batch.framework.model.JMSConfig;
import com.rhbgroup.dcp.bo.batch.framework.utils.JMSUtils;
import com.rhbgroup.dcp.bo.batch.job.config.properties.PushAdhocSystemDowntimeNotificationsProcessorJobConfigProperties;
import com.rhbgroup.dcp.bo.batch.job.step.SendPushAdhocSystemDowntimeNotificationsStepBuilder;
/*
 * DCPBL-22726 Push Adhoc System Downtime Notifications Processor
 * @author: Vicneswari
 * */
@Configuration
@Lazy
public class PushAdhocSystemDowntimeNotificationsProcessorJobConfiguration extends BaseJobConfiguration implements JmsConfiguration {
	
	private static final Logger logger = Logger.getLogger(PushAdhocSystemDowntimeNotificationsProcessorJobConfiguration.class);
	
	private static final String JOB_NAME = "PushAdhocSystemDowntimeNotificationsProcessorJob";
	
	@Autowired
	private SendPushAdhocSystemDowntimeNotificationsStepBuilder sendPushAdhocSystemDowntimeNotificationsStepBuilder;
	
	@Bean(name = JOB_NAME)
	public Job buildJob() {
		logger.info(String.format("Building job [%s]", JOB_NAME));
		Job job= getDefaultJobBuilder(JOB_NAME)
                .next(sendPushAdhocSystemDowntimeNotificationsStepBuilder.buildStep())
                .build();
		
      	logger.info(String.format("[%s] job build successfully", JOB_NAME));
        return job;
    }

	@Bean("SmsJMSConfig")
	@Lazy
	@Override
	public JMSConfig createJMSConfig(@Qualifier("PushAdhocSystemDowntimeNotificationsProcessorJobConfigProperties") BaseJobConfigProperties jobConfigProperties, 
			@Qualifier("SmsJMSConfigProperties") JMSConfigProperties jmsConfigProperties) throws NamingException {
		PushAdhocSystemDowntimeNotificationsProcessorJobConfigProperties properties = (PushAdhocSystemDowntimeNotificationsProcessorJobConfigProperties) jobConfigProperties;
		return JMSUtils.setupJMS(null, properties.getJmsQueue(), jmsConfigProperties);
	}

}
