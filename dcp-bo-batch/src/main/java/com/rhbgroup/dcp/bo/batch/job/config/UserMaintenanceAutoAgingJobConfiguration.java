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
import com.rhbgroup.dcp.bo.batch.job.config.properties.UserMaintenanceAutoAgingJobConfigProperties;
import com.rhbgroup.dcp.bo.batch.job.step.ProcessErrorMessagesFromDBStepBuilder;
import com.rhbgroup.dcp.bo.batch.job.step.UpdateUserAccountStatusStepBuilder;

@Configuration
@Lazy
public class UserMaintenanceAutoAgingJobConfiguration extends BaseJobConfiguration implements JmsConfiguration {

	private static final Logger logger = Logger.getLogger(UserMaintenanceAutoAgingJobConfiguration.class);
	
	private static final String JOB_NAME = "UserMaintenanceAutoAgingJob";
	
	@Autowired
	private UpdateUserAccountStatusStepBuilder updateUserAccountStepBuilder;
	
	@Autowired
	private ProcessErrorMessagesFromDBStepBuilder processErrorMessagesFromDBStepBuilder;
	
	@Bean(JOB_NAME)
	public Job buildJob() {
		logger.info(String.format("Building job [%s]", JOB_NAME));	
		Job job = getDefaultJobBuilder(JOB_NAME)
			.next(updateUserAccountStepBuilder.buildStep())
			.next(processErrorMessagesFromDBStepBuilder.buildStep())
			.build();		
		
		logger.info(String.format("[%s] job build successfully", JOB_NAME));
		return job;
	}

	@Bean("AuditJMSConfig")
	@Lazy
	@Override
	public JMSConfig createJMSConfig(@Qualifier("UserMaintenanceAutoAgingJobConfigProperties") BaseJobConfigProperties jobConfigProperties, @Qualifier("AuditJMSConfigProperties") JMSConfigProperties jmsConfigProperties) throws NamingException {
		UserMaintenanceAutoAgingJobConfigProperties properties = (UserMaintenanceAutoAgingJobConfigProperties) jobConfigProperties;
		return JMSUtils.setupJMS(null, properties.getJmsQueue(), jmsConfigProperties);
	}
	
}
