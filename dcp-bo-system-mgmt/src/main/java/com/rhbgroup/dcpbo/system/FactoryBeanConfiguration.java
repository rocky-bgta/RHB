package com.rhbgroup.dcpbo.system;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.config.ServiceLocatorFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.rhbgroup.dcpbo.common.audit.AuditAdditionalDataFactory;

/**
 * Bean which are complex to create and need a factory need to be declare here.
 * Example:Ebean Each bean need to have identifier and must be associate to
 * profile.
 * 
 * @author faisal
 */
@Configuration
public class FactoryBeanConfiguration {

	@Bean("auditAdditionalDataFactory")
	public FactoryBean getAuditAdditionalDataFactory() {
		ServiceLocatorFactoryBean factoryBean = new ServiceLocatorFactoryBean();
		factoryBean.setServiceLocatorInterface(AuditAdditionalDataFactory.class);
		return factoryBean;
	}

}
