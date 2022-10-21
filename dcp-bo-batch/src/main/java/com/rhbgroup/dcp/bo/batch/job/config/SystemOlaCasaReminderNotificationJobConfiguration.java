package com.rhbgroup.dcp.bo.batch.job.config;

import javax.naming.NamingException;

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
import com.rhbgroup.dcp.bo.batch.job.config.properties.SystemOlaCasaReminderNotificationJobConfigProperties;
import com.rhbgroup.dcp.bo.batch.job.step.InsertDcpTransactionDataStepBuilder;
import com.rhbgroup.dcp.bo.batch.job.step.OlaCasaNotificationDataStepBuilder;

@Configuration
@Lazy
public class SystemOlaCasaReminderNotificationJobConfiguration extends BaseJobConfiguration implements JmsConfiguration {
    private static final String JOB_NAME = "SystemOlaCasaReminderNotificationJob";

    @Autowired
    OlaCasaNotificationDataStepBuilder olaCasaNotificationDataStepBuilder;

    @Bean(JOB_NAME)
    public Job buildJob() {
       return getDefaultJobBuilder(JOB_NAME)
                .next(this.olaCasaNotificationDataStepBuilder.buildStep())
                .build();
    }
    
    @Bean("SmsJMSConfig")
	@Lazy
	@Override
	public JMSConfig createJMSConfig(@Qualifier("SystemOlaCasaReminderNotificationJobConfigProperties") BaseJobConfigProperties jobConfigProperties, @Qualifier("SmsJMSConfigProperties") JMSConfigProperties jmsConfigProperties) throws NamingException {
    	SystemOlaCasaReminderNotificationJobConfigProperties properties = (SystemOlaCasaReminderNotificationJobConfigProperties) jobConfigProperties;
		return JMSUtils.setupJMS(null, properties.getJmsQueue(), jmsConfigProperties);
	}
}
