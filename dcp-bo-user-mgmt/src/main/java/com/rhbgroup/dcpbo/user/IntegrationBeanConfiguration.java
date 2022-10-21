package com.rhbgroup.dcpbo.user;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;

import com.rhbgroup.dcpbo.common.audit.BoAuditLogQueue;
import com.rhbgroup.dcpbo.common.audit.BoAuditLogger;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;

/**
 * This system will talk to other subsystem to make it fully functional, park the bean here.
 * Example: jms, eai
 * Each bean need to have identifier and must be associate to profile.
 * @author faisal
 */

@Configuration
public class IntegrationBeanConfiguration {

	
    @Profile({"local", "dev", "sit", "sit2","uat","uat2", "drl1_02", "drl1_05", "drl2_02", "drl2_05", "trn", "preprod", "dev_pilot", "sit_pilot", "uat_pilot", "prod_02", "prod_05"})
    @Bean("boAuditLogger")
    public BoAuditLogQueue getBoAuditLogger(JmsTemplate jmsTemplate,
                                            @Value("${auditLogger.audit.queue.name}") String auditQueueName) {
        return new BoAuditLogger(jmsTemplate ,auditQueueName);
    }

    @Profile("test")
    @Bean("boAuditLogger")
    public BoAuditLogQueue getMockBoAuditLogger() {
        return new BoAuditLogQueue() {
            @Override
            public void send(String jsonStr) {

            }
        };
    }

    @Bean
    public MessageConverter jacksonJmsMessageConverter() {
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setTargetType(MessageType.TEXT);
        converter.setTypeIdPropertyName("_type");
        return converter;
    }
    
}
