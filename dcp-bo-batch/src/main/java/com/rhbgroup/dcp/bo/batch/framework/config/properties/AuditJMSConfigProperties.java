package com.rhbgroup.dcp.bo.batch.framework.config.properties;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Component
@Qualifier("AuditJMSConfigProperties")
@ConfigurationProperties("jmsaudit")
@EnableConfigurationProperties
@Getter
@Setter
public class AuditJMSConfigProperties extends JMSConfigProperties {
}
