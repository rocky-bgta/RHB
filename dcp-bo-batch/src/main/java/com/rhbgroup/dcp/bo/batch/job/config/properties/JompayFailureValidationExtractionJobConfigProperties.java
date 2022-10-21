package com.rhbgroup.dcp.bo.batch.job.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Component
@ConfigurationProperties("job.jompayfailurevalidationextractionjob")
@EnableConfigurationProperties
@Getter
@Setter
public class JompayFailureValidationExtractionJobConfigProperties {
	private int chunkSize;
	private int jdbcPagingSize;
	private String ftpFolder;
	private String name;
	private String nameDateFormat;
	private String bankId;
}