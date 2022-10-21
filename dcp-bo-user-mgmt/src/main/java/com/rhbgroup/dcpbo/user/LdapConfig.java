package com.rhbgroup.dcpbo.user;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "ldap")
@Setter
@Getter
@ToString
public class LdapConfig {
	private String url;
	private String basedn;
	private String userdn;
	private String password;
	private String referral;
	private String nameField;
	private String emailField;
}
