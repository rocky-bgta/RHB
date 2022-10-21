package com.rhbgroup.dcp.bo.batch.framework.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Component
@ConfigurationProperties("ftp")
@EnableConfigurationProperties
@Primary
@Getter
@Setter
public class FTPConfigProperties {
	
	private String host;
	private int port;
	private String username;
	private String password;
	private boolean issecureftp;
	private int timeout;
}
