package com.rhbgroup.dcp.bo.batch.job.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Component
@ConfigurationProperties("job.emaildmbud089")
@EnableConfigurationProperties
@Getter
@Setter
public class OlaCasaEmailConfigProperties {
	
	private String host;
	private String port;
	private String username;
	private String password;
	private String to;
	private String from;
	private String subject;
	private String body;
	private String smtpAuth;
	private String tlsEnable;
	private String tlsRequire;
	private String sslTrustAllCert;
	private String emailDebugMode;
}
