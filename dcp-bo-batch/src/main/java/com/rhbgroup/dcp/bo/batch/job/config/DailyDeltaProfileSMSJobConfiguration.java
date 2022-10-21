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
import com.rhbgroup.dcp.bo.batch.framework.core.BaseDBToFileJobConfiguration;
import com.rhbgroup.dcp.bo.batch.framework.core.JmsConfiguration;
import com.rhbgroup.dcp.bo.batch.framework.model.JMSConfig;
import com.rhbgroup.dcp.bo.batch.framework.utils.JMSUtils;
import com.rhbgroup.dcp.bo.batch.job.config.properties.DailyDeltaProfileSMSJobConfigProperties;
import com.rhbgroup.dcp.bo.batch.job.step.SendDailyDeltaProfileSMSStepBuilder;

@Configuration
@Lazy
public class DailyDeltaProfileSMSJobConfiguration extends BaseDBToFileJobConfiguration implements JmsConfiguration {

	private static final Logger logger = Logger.getLogger(DailyDeltaProfileSMSJobConfiguration.class);
	
	private static final String JOB_NAME = "DailyDeltaProfileSMSJob";
	
	@Autowired
	private SendDailyDeltaProfileSMSStepBuilder sendDailyDeltaProfileSMSStepBuilder;
	
	@Bean(name = JOB_NAME)
    public Job buildJob() {
		logger.info(String.format("Building job [%s]", JOB_NAME));
		Job job= getDefaultJobBuilder(JOB_NAME)
                .next(sendDailyDeltaProfileSMSStepBuilder.buildStep())
                .build();
		
      	logger.info(String.format("[%s] job build successfully", JOB_NAME));
        return job;
    }
	
	@Bean("SmsJMSConfig")
	@Lazy
	@Override
	public JMSConfig createJMSConfig(@Qualifier("DailyDeltaProfileSMSJobConfigProperties") BaseJobConfigProperties jobConfigProperties, @Qualifier("SmsJMSConfigProperties") JMSConfigProperties jmsConfigProperties) throws NamingException {
		DailyDeltaProfileSMSJobConfigProperties properties = (DailyDeltaProfileSMSJobConfigProperties) jobConfigProperties;
		return JMSUtils.setupJMS(null, properties.getJmsQueue(), jmsConfigProperties);
	}
}
