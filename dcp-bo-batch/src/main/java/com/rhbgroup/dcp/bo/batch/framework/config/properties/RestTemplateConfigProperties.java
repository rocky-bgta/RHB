package com.rhbgroup.dcp.bo.batch.framework.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Component
@ConfigurationProperties(prefix="job.common.resttemplate")
@EnableConfigurationProperties
@Primary
@Getter
@Setter
public class RestTemplateConfigProperties {
	private int connectionRequestTimeout;
	private int connectTimeout;
	private int readTimeout;
}
