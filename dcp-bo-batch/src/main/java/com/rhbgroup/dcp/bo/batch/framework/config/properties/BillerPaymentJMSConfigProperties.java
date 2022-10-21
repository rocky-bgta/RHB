package com.rhbgroup.dcp.bo.batch.framework.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Qualifier("BillerPaymentJMSConfigProperties")
@ConfigurationProperties("jmsbiller")
@EnableConfigurationProperties
@Getter
@Setter
public class BillerPaymentJMSConfigProperties extends JMSConfigProperties {
}
