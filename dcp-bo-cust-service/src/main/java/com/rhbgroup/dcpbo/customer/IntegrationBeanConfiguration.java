package com.rhbgroup.dcpbo.customer;

import com.rhbgroup.dcpbo.customer.audit.collector.BoAuditLogQueue;
import com.rhbgroup.dcpbo.customer.audit.collector.BoAuditLogger;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;

import javax.jms.ConnectionFactory;

/**
 * This system will talk to other subsystem to make it fully functional, park the bean here.
 * Example: jms, eai
 * Each bean need to have identifier and must be associate to profile.
 * @author faisal
 */

@Configuration
public class IntegrationBeanConfiguration {

    @Profile({"default", "dev","sit", "sit2", "uat", "prod", "trn", "drl1_02", "drl1_05", "drl2_02", "drl2_05", "prod_02", "prod_05", "preprod", "sit_pilot", "uat_pilot", "dev_pilot" , "uat2"})
    @Bean("boAuditLogger")
    public BoAuditLogQueue getBoAuditLogger(JmsTemplate jmsTemplate,
                                            @Value("${auditLogger.audit.queue.name}") String auditQueueName) {
        return new BoAuditLogger(jmsTemplate ,auditQueueName);
    }

    @Profile({"local", "test"})
    @Bean("boAuditLogger")
    public BoAuditLogQueue getMockBoAuditLogger() {
        return new BoAuditLogQueue() {
            @Autowired
            JmsTemplate jmsTemplate;

            @Value("${auditLogger.audit.queue.name}")
            String destinationName;

            @Override
            public void send(String jsonStr) {

                jmsTemplate.convertAndSend(destinationName, jsonStr);
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
