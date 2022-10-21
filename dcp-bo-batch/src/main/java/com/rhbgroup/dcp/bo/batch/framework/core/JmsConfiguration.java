package com.rhbgroup.dcp.bo.batch.framework.core;

import javax.naming.NamingException;

import com.rhbgroup.dcp.bo.batch.framework.config.properties.BaseJobConfigProperties;
import com.rhbgroup.dcp.bo.batch.framework.config.properties.JMSConfigProperties;
import com.rhbgroup.dcp.bo.batch.framework.model.JMSConfig;

public interface JmsConfiguration {
	public JMSConfig createJMSConfig(BaseJobConfigProperties jobConfigProperties, JMSConfigProperties jmsConfigProperties) throws NamingException;
}
