package com.rhbgroup.dcpbo.customer.listener;

import org.springframework.boot.autoconfigure.jms.DefaultJmsListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.stereotype.Component;

import javax.jms.ConnectionFactory;

@Component
@Profile("local")
public class LocalListener {

    @Bean(name = "testFactory")
    public JmsListenerContainerFactory<?> testFactory(ConnectionFactory connectionFactory, DefaultJmsListenerContainerFactoryConfigurer configurer) {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        configurer.configure(factory, connectionFactory);
        return factory;
    }

    @JmsListener(destination = "java:jboss/exported/jms/queue/q_dcpbo_audit", containerFactory = "testFactory")
    public void receiveMessageQueue(String message) {
        System.out.println("*** Message received = " + message + " ***");
    }

}
