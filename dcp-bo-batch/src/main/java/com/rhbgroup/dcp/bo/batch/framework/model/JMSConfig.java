package com.rhbgroup.dcp.bo.batch.framework.model;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSContext;
import javax.jms.JMSProducer;
import javax.naming.Context;

import org.springframework.jms.core.JmsTemplate;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@EqualsAndHashCode
@ToString
public class JMSConfig {
	private ConnectionFactory connectionFactory;
	private Context namingContext;
	private Destination destination;
	private JmsTemplate jmsTemplate;
	private JMSContext jmsContext;
	private JMSProducer jmsProducer;
}
