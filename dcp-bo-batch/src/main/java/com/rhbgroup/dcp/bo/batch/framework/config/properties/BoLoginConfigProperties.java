package com.rhbgroup.dcp.bo.batch.framework.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Component
@ConfigurationProperties("bologin")
@EnableConfigurationProperties
@Primary
@Getter
@Setter
public class BoLoginConfigProperties {
	private String api;
	private String username;
	private String password;
}