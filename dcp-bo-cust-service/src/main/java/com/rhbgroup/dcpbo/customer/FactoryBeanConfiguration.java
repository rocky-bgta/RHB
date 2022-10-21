package com.rhbgroup.dcpbo.customer;

import javax.sql.DataSource;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ServiceLocatorFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.rhbgroup.dcpbo.customer.audit.collector.AuditAdditionalDataFactory;
import com.rhbgroup.dcpbo.customer.factory.EBeanServerFactory;

import io.ebean.EbeanServer;
import io.ebean.EbeanServerFactory;
import io.ebean.config.ServerConfig;

@Configuration
public class FactoryBeanConfiguration {
	
	@Autowired
	DataSource dataSource;

    @Profile({"default", "dev", "sit", "sit2", "uat", "trn", "drl1_02", "drl1_05", "drl2_02", "drl2_05", "prod_02", "prod_05", "preprod" , "sit_pilot", "uat_pilot", "dev_pilot" , "uat2"})
    @Bean("eBeanServerFactory")
        public EBeanServerFactory getEBeanServerFactory(@Qualifier("eBeanServerConfig") ServerConfig serverConfig) {
            return new EBeanServerFactory(serverConfig);
        }

    @Profile({"default", "dev", "sit", "sit2", "uat", "trn", "drl1_02", "drl1_05", "drl2_02", "drl2_05", "prod_02", "prod_05", "preprod", "sit_pilot", "uat_pilot", "dev_pilot" , "uat2"})
    @Bean("eBeanServer")
    public EbeanServer getEBeanServer(EBeanServerFactory eBeanServerFactory) throws Exception {
        return eBeanServerFactory.getObject();
    }
    
    @Profile({"local","test"})
	@Bean("eBeanServer")
	public EbeanServer getLocalEBeanServer() throws Exception {
		ServerConfig config = new ServerConfig();
		config.setName("db");
		config.setDataSource(dataSource);
		config.setDefaultServer(true);
		return EbeanServerFactory.create(config);
	}

    @Bean("auditAdditionalDataFactory")
    public FactoryBean getAuditAdditionalDataFactory() {
        ServiceLocatorFactoryBean factoryBean = new ServiceLocatorFactoryBean();
        factoryBean.setServiceLocatorInterface(AuditAdditionalDataFactory.class);
        return factoryBean;
    }
}
