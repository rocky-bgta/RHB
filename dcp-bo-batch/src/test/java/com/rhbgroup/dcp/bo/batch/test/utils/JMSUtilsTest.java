package com.rhbgroup.dcp.bo.batch.test.utils;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.util.UUID;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSContext;
import javax.jms.JMSProducer;
import javax.jms.ObjectMessage;
import javax.naming.CommunicationException;
import javax.naming.Context;
import javax.naming.NamingException;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.jms.core.JmsTemplate;

import com.rhbgroup.dcp.bo.batch.framework.config.properties.SmsJMSConfigProperties;
import com.rhbgroup.dcp.bo.batch.framework.model.JMSConfig;
import com.rhbgroup.dcp.bo.batch.framework.utils.JMSUtils;
import com.rhbgroup.dcp.bo.batch.test.common.BaseJobTest;
import com.rhbgroup.dcp.model.Capsule;

public class JMSUtilsTest extends BaseJobTest {

	private static final String DEFAULT_CONNECTION_FACTORY = "jms/RemoteConnectionFactory";
	
	/*
	 * Test setup the JMS configuration
	 */
	@Test
	public void testPositiveSetupJMS() throws NamingException {
		JMSConfig jmsConfig = new JMSConfig();
		String queue = "TEST_QUEUE";
		
		SmsJMSConfigProperties jmsConfigProperties = new SmsJMSConfigProperties();
		jmsConfigProperties.setHost("TEST_HOST");
		jmsConfigProperties.setPort(12345);
		jmsConfigProperties.setUsername("TESTER");
		jmsConfigProperties.setPassword("TESTER");
		
		// We are using mocks to get the connection
		Context mockNamingContext = Mockito.mock(Context.class);
		jmsConfig.setNamingContext(mockNamingContext);
		
		ConnectionFactory mockConnectionFactory = Mockito.mock(ConnectionFactory.class);
		Destination mockDestination = Mockito.mock(Destination.class);
		
		when(mockNamingContext.lookup(DEFAULT_CONNECTION_FACTORY)).thenReturn(mockConnectionFactory);
		when(mockNamingContext.lookup(queue)).thenReturn(mockDestination);
		
		JMSConfig result = JMSUtils.setupJMS(jmsConfig, queue, jmsConfigProperties);
		assertNotNull(result.getConnectionFactory());
		assertNotNull(result.getDestination());
		assertNotNull(result.getJmsTemplate());
	}
	
	/*
	 * Test setup the JMS configuration with wrong info
	 */
	@Test
	public void testNegativeSetupJMSWithInvalidDetails() throws NamingException {
		String queue = "TEST_QUEUE";
		
		SmsJMSConfigProperties jmsConfigProperties = new SmsJMSConfigProperties();
		jmsConfigProperties.setHost("TEST_HOST");
		jmsConfigProperties.setPort(12345);
		jmsConfigProperties.setUsername("TESTER");
		jmsConfigProperties.setPassword("TESTER");
		
		// We dont use any more and it will for sure hit connection issue
		expectedEx.expect(CommunicationException.class);
    	expectedEx.expectMessage(Matchers.containsString("Failed to connect to any server. Servers tried"));
		
		JMSUtils.setupJMS(null, queue, jmsConfigProperties);
	}
	
	/*
	 * Test sending the JMS message
	 */
	@Test
	public void testPositiveSendJMSMessage() {
		String message = "TEST JMS MESSAGE";
		JMSConfig jmsConfig = new JMSConfig();
		
		Destination mockDestination = Mockito.mock(Destination.class);
		jmsConfig.setDestination(mockDestination);
		
		JmsTemplate mockJmsTempalte = Mockito.mock(JmsTemplate.class);
		jmsConfig.setJmsTemplate(mockJmsTempalte);
		
		doNothing().when(mockJmsTempalte).convertAndSend(jmsConfig.getDestination(), message);
		
		JMSUtils.sendMessageToJMS(message, jmsConfig);
	}
	
	/*
	 * Test sending empty JMS message
	 */
	@Test
	public void testPositiveSendEmptyMessage() {
		String message = "";
		JMSConfig jmsConfig = new JMSConfig();
		
		Destination mockDestination = Mockito.mock(Destination.class);
		jmsConfig.setDestination(mockDestination);
		
		JmsTemplate mockJmsTempalte = Mockito.mock(JmsTemplate.class);
		jmsConfig.setJmsTemplate(mockJmsTempalte);
		
		doNothing().when(mockJmsTempalte).convertAndSend(jmsConfig.getDestination(), message);
		
		JMSUtils.sendMessageToJMS(message, jmsConfig);
	}
	
	/*
	 * Test sending NULL message
	 */
	@Test
	public void testNegativeSendNullMessage() {
		String message = null;
		JMSConfig jmsConfig = new JMSConfig();
		
		Destination mockDestination = Mockito.mock(Destination.class);
		jmsConfig.setDestination(mockDestination);
		
		JmsTemplate mockJmsTempalte = Mockito.mock(JmsTemplate.class);
		jmsConfig.setJmsTemplate(mockJmsTempalte);
		
		doThrow(new NullPointerException()).when(mockJmsTempalte).convertAndSend(jmsConfig.getDestination(), message);
		
		expectedEx.expect(Exception.class);
		
		JMSUtils.sendMessageToJMS(message, jmsConfig);
	}
	
	/*
	 * Test while sending JMS something wrong with the JMS server site
	 */
	@Test
	public void testNegativeSendJMS() throws NamingException {
		JMSConfig jmsConfig = new JMSConfig();
		String queue = "TEST_QUEUE";
		String message = "TEST JMS MESSAGE";
		
		SmsJMSConfigProperties jmsConfigProperties = new SmsJMSConfigProperties();
		jmsConfigProperties.setHost("TEST_HOST");
		jmsConfigProperties.setPort(12345);
		jmsConfigProperties.setUsername("TESTER");
		jmsConfigProperties.setPassword("TESTER");
		
		Context mockNamingContext = Mockito.mock(Context.class);
		jmsConfig.setNamingContext(mockNamingContext);
		
		ConnectionFactory mockConnectionFactory = Mockito.mock(ConnectionFactory.class);
		Destination mockDestination = Mockito.mock(Destination.class);
		
		when(mockNamingContext.lookup(DEFAULT_CONNECTION_FACTORY)).thenReturn(mockConnectionFactory);
		when(mockNamingContext.lookup(queue)).thenReturn(mockDestination);
		
		JMSConfig result = JMSUtils.setupJMS(jmsConfig, queue, jmsConfigProperties);
		assertNotNull(result.getConnectionFactory());
		assertNotNull(result.getDestination());
		assertNotNull(result.getJmsTemplate());
		
		expectedEx.expect(Exception.class);
		
		JMSUtils.sendMessageToJMS(message, jmsConfig);
	}
	
	/*
	 * Test sending the JMS capsule message
	 */
	@Test
	public void testPositiveCapsuleSendJMSMessage() throws NamingException {
		JMSConfig jmsConfig = new JMSConfig();
		
		Destination mockDestination = Mockito.mock(Destination.class);
		jmsConfig.setDestination(mockDestination);
		
		JMSContext mockJmsContext = Mockito.mock(JMSContext.class);
		jmsConfig.setJmsContext(mockJmsContext);
		
		ObjectMessage mockObjectMessage = Mockito.mock(ObjectMessage.class);
		when(mockJmsContext.createObjectMessage(Mockito.any())).thenReturn(mockObjectMessage);
		
		JMSProducer mockJmsProducer = Mockito.mock(JMSProducer.class);
		jmsConfig.setJmsProducer(mockJmsProducer);
		when(mockJmsProducer.send(Mockito.any(Destination.class), Mockito.any(ObjectMessage.class))).thenReturn(mockJmsProducer);
		
		Capsule capsule = new Capsule();
		capsule.setUserId(9991);
		capsule.setMessageId(UUID.randomUUID().toString());
		
		JMSUtils.sendCapsuleMessageToJMS(capsule, jmsConfig);
	}
	
	/*
	 * Test sending empty JMS capsule message
	 */
	@Test
	public void testPositiveCapsuleSendEmptyMessage() {
		JMSConfig jmsConfig = new JMSConfig();
		
		Destination mockDestination = Mockito.mock(Destination.class);
		jmsConfig.setDestination(mockDestination);
		
		JMSContext mockJmsContext = Mockito.mock(JMSContext.class);
		jmsConfig.setJmsContext(mockJmsContext);
		
		ObjectMessage mockObjectMessage = Mockito.mock(ObjectMessage.class);
		when(mockJmsContext.createObjectMessage(Mockito.any())).thenReturn(mockObjectMessage);
		
		JMSProducer mockJmsProducer = Mockito.mock(JMSProducer.class);
		jmsConfig.setJmsProducer(mockJmsProducer);
		when(mockJmsProducer.send(Mockito.any(Destination.class), Mockito.any(ObjectMessage.class))).thenReturn(mockJmsProducer);
		
		Capsule capsule = new Capsule();
		
		JMSUtils.sendCapsuleMessageToJMS(capsule, jmsConfig);
	}
	
	/*
	 * Test sending NULL capsule message
	 */
	@Test
	public void testNegativeCapsuleSendNullMessage() {
		JMSConfig jmsConfig = new JMSConfig();
		
		Destination mockDestination = Mockito.mock(Destination.class);
		jmsConfig.setDestination(mockDestination);
		
		JMSContext mockJmsContext = Mockito.mock(JMSContext.class);
		jmsConfig.setJmsContext(mockJmsContext);
		
		ObjectMessage mockObjectMessage = Mockito.mock(ObjectMessage.class);
		when(mockJmsContext.createObjectMessage(Mockito.any())).thenReturn(mockObjectMessage);
		
		JMSProducer mockJmsProducer = Mockito.mock(JMSProducer.class);
		jmsConfig.setJmsProducer(mockJmsProducer);
		when(mockJmsProducer.send(Mockito.any(Destination.class), Mockito.any(ObjectMessage.class))).thenReturn(mockJmsProducer);
		
		expectedEx.expect(Exception.class);
		
		Capsule capsule = null;
		
		JMSUtils.sendCapsuleMessageToJMS(capsule, jmsConfig);
	}
	
	/*
	 * Test while sending JMS capsule something wrong with the JMS server site
	 */
	@Test
	public void testNegativeCapsuleSendJMS() throws NamingException {
		JMSConfig jmsConfig = new JMSConfig();
		String queue = "TEST_QUEUE";
		
		Capsule capsule = new Capsule();
		capsule.setUserId(9991);
		capsule.setMessageId(UUID.randomUUID().toString());
		
		SmsJMSConfigProperties jmsConfigProperties = new SmsJMSConfigProperties();
		jmsConfigProperties.setHost("TEST_HOST");
		jmsConfigProperties.setPort(12345);
		jmsConfigProperties.setUsername("TESTER");
		jmsConfigProperties.setPassword("TESTER");
		
		Context mockNamingContext = Mockito.mock(Context.class);
		jmsConfig.setNamingContext(mockNamingContext);
		
		ConnectionFactory mockConnectionFactory = Mockito.mock(ConnectionFactory.class);
		Destination mockDestination = Mockito.mock(Destination.class);
		
		when(mockNamingContext.lookup(DEFAULT_CONNECTION_FACTORY)).thenReturn(mockConnectionFactory);
		when(mockNamingContext.lookup(queue)).thenReturn(mockDestination);
		
		JMSConfig result = JMSUtils.setupJMS(jmsConfig, queue, jmsConfigProperties);
		assertNotNull(result.getConnectionFactory());
		assertNotNull(result.getDestination());
		assertNotNull(result.getJmsTemplate());
		
		expectedEx.expect(Exception.class);
		
		JMSUtils.sendCapsuleMessageToJMS(capsule, jmsConfig);
	}
	
}
