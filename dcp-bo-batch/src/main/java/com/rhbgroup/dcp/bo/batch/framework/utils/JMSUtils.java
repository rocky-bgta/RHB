package com.rhbgroup.dcp.bo.batch.framework.utils;

import java.lang.IllegalStateException;
import java.util.Date;
import java.util.Properties;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSContext;
import javax.jms.JMSProducer;
import javax.jms.ObjectMessage;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.log4j.Logger;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageType;

import com.rhbgroup.dcp.bo.batch.framework.config.properties.JMSConfigProperties;
import com.rhbgroup.dcp.bo.batch.framework.model.JMSConfig;
import com.rhbgroup.dcp.bo.batch.job.step.tasklet.OlaCasaNotificationDataTasklet;
import com.rhbgroup.dcp.model.Capsule;
import com.rhbgroup.dcp.model.Constants;
import com.rhbgroup.dcp.util.DateTimeUtil;

public class JMSUtils {
    private static final Logger logger = Logger.getLogger(JMSUtils.class);

    private static final String DEFAULT_CONNECTION_FACTORY = "jms/RemoteConnectionFactory";
    private static final String INITIAL_CONTEXT_FACTORY = "org.jboss.naming.remote.client.InitialContextFactory";
    private static final String PROVIDER_URL_TEMPLATE = "http-remoting://%s:%s";

    private JMSUtils() {
    	throw new IllegalStateException("Utility Class");
    }
    
    public static JMSConfig setupJMS(JMSConfig jmsConfig, String queue, JMSConfigProperties jmsConfigProperties) throws NamingException {
    	if(jmsConfig == null) {
    		jmsConfig = new JMSConfig();
    	}
    	
    	// Create NamingContext
    	if(jmsConfig.getNamingContext() == null) {
    		jmsConfig.setNamingContext(JMSUtils.getInitialContext(jmsConfigProperties));
		}
    	// Create ConnectionFactory
    	if(jmsConfig.getNamingContext() != null && jmsConfig.getConnectionFactory() == null) {
			jmsConfig.setConnectionFactory(getJMSConnectionFactory(jmsConfig));
		}
    	// Set Destination
		if(jmsConfig.getNamingContext() != null && jmsConfig.getDestination() == null) {
			jmsConfig.setDestination(JMSUtils.getDestination(queue, jmsConfig));
		}
		// Set JmsTemplate
		if(jmsConfig.getConnectionFactory() != null && jmsConfig.getJmsTemplate() == null) {
			jmsConfig.setJmsTemplate(JMSUtils.getJmsTemplate(jmsConfig));
		}
		// Set JmsContext
        if(jmsConfig.getNamingContext() != null && jmsConfig.getJmsContext() == null) {
            jmsConfig.setJmsContext(JMSUtils.getJMSContext(jmsConfig.getNamingContext(), jmsConfigProperties.getUsername(), jmsConfigProperties.getPassword()));
        }

        // Set JmsProducer
        if(jmsConfig.getJmsContext() != null && jmsConfig.getJmsProducer() == null) {
            jmsConfig.setJmsProducer(JMSUtils.getJMSProducer(jmsConfig.getJmsContext()));
        }
		
		return jmsConfig;
    }
    
    public static void sendMessageToJMS(String message, JMSConfig jmsConfig) {
    	jmsConfig.getJmsTemplate().convertAndSend(jmsConfig.getDestination(), message);
    }

    public static void sendCapsuleMessageToJMS(Capsule capsule, JMSConfig jmsConfig) {
		logger.info(String.format("jmsConfig  = %s", jmsConfig));
        JMSContext jmsContext = jmsConfig.getJmsContext();
        String currentDatetime = DateTimeUtil.convertDateToFormattedDate(new Date());
        capsule.setProperty(Constants.QUEUE_PUBLISHED_TIME, currentDatetime);

        ObjectMessage objectMessage = jmsContext.createObjectMessage(capsule);
        jmsConfig.getJmsProducer().send(jmsConfig.getDestination(), objectMessage);
        capsule.setOperationSuccess(true);
    }

    private static Context getInitialContext(JMSConfigProperties jmsConfigProperties) throws NamingException {
    	Properties env = new Properties();
        env.put(Context.INITIAL_CONTEXT_FACTORY, INITIAL_CONTEXT_FACTORY);
        env.put(Context.PROVIDER_URL, String.format(PROVIDER_URL_TEMPLATE, jmsConfigProperties.getHost(), jmsConfigProperties.getPort()));
        env.put(Context.SECURITY_PRINCIPAL, jmsConfigProperties.getUsername());
        env.put(Context.SECURITY_CREDENTIALS, jmsConfigProperties.getPassword());
        return new InitialContext(env);
    }
    
    private static ConnectionFactory getJMSConnectionFactory(JMSConfig jmsConfig) throws NamingException {
    	return (ConnectionFactory) jmsConfig.getNamingContext().lookup(DEFAULT_CONNECTION_FACTORY);
    }
    
    private static Destination getDestination(String queue, JMSConfig jmsConfig) throws NamingException {
    	return (Destination) jmsConfig.getNamingContext().lookup(queue);
    }
    
    private static JmsTemplate getJmsTemplate(JMSConfig jmsConfig) {
    	MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setTargetType(MessageType.TEXT);
        converter.setTypeIdPropertyName("_type");

    	JmsTemplate jmsTemplate = new JmsTemplate(jmsConfig.getConnectionFactory());
    	jmsTemplate.setMessageConverter(converter);

    	return jmsTemplate;
    }

    private static JMSContext getJMSContext(Context namingContext, String username, String password) throws NamingException {
        ConnectionFactory connectionFactory = (ConnectionFactory) namingContext.lookup(DEFAULT_CONNECTION_FACTORY);
        return connectionFactory.createContext(username, password);
    }

    private static JMSProducer getJMSProducer(JMSContext jmsContext) {
        return jmsContext.createProducer();
    }
}
