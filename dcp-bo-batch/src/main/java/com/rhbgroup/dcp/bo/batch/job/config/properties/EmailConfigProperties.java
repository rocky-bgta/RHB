package com.rhbgroup.dcp.bo.batch.job.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Component
@ConfigurationProperties("job.email")
@EnableConfigurationProperties
@Getter
@Setter
public class EmailConfigProperties {
	
	private String host;
	private String port;
	private String username;
	private String password;
	private String to;
	private String from;
	private String template_01;
	private String template_02;
	private String template_03;
	private String template_04;
	private String template_05;
	private String template_06;
	private String template_07;
	private String smtpAuth;
	private String tlsEnable;
	private String tlsRequire;
	private String sslTrustAllCert;
	private String emailDebugMode;
}
