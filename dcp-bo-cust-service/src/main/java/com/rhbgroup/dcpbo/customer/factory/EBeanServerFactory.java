package com.rhbgroup.dcpbo.customer.factory;

import io.ebean.EbeanServer;
import io.ebean.config.ServerConfig;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

public class EBeanServerFactory implements InitializingBean, FactoryBean<EbeanServer> {

    private EbeanServer ebeanServer;

    private ServerConfig serverConfig;

    public EBeanServerFactory(ServerConfig serverConfig) {
        this.serverConfig = serverConfig;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (serverConfig == null) {
            throw new Exception("Please define eBean config before someone going to hit u");
        }
        this.ebeanServer = io.ebean.EbeanServerFactory.create(serverConfig);
    }

    @Override
    public EbeanServer getObject() throws Exception {
        return ebeanServer;
    }

    @Override
    public Class<? extends EbeanServer> getObjectType() {
        return EbeanServer.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}
