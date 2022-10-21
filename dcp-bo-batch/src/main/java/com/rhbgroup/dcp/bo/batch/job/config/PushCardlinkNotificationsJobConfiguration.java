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
import com.rhbgroup.dcp.bo.batch.job.config.properties.PushCardlinkNotificationsJobConfigProperties;
import com.rhbgroup.dcp.bo.batch.job.step.SendPushCardlinkNotificationStepBuilder;
/*
 * DCPBL-15361 Push Cardlink notification
 * @author: Ema
 * */
@Configuration
@Lazy
public class PushCardlinkNotificationsJobConfiguration extends BaseJobConfiguration implements JmsConfiguration {
	
	private static final Logger logger = Logger.getLogger(PushCardlinkNotificationsJobConfiguration.class);
	
	private static final String JOB_NAME = "PushCardlinkNotificationsProcessorJob";
	
	@Autowired
	private SendPushCardlinkNotificationStepBuilder sendCardlinkNotificationStep;
	
	@Bean(name = JOB_NAME)
	public Job buildJob() {
		logger.info(String.format("Building job [%s]", JOB_NAME));
		Job job= getDefaultJobBuilder(JOB_NAME)
                .next(sendCardlinkNotificationStep.buildStep())
                .build();
		
      	logger.info(String.format("[%s] job build successfully", JOB_NAME));
        return job;
    }

	@Bean("SmsJMSConfig")
	@Lazy
	@Override
	public JMSConfig createJMSConfig(@Qualifier("CardlinkNotificationsJobConfigProperties") BaseJobConfigProperties jobConfigProperties, @Qualifier("SmsJMSConfigProperties") JMSConfigProperties jmsConfigProperties) throws NamingException {
		PushCardlinkNotificationsJobConfigProperties properties = (PushCardlinkNotificationsJobConfigProperties) jobConfigProperties;
		return JMSUtils.setupJMS(null, properties.getJmsQueue(), jmsConfigProperties);
	}

}
