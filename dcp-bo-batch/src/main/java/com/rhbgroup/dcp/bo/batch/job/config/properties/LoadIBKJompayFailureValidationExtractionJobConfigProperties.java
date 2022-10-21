package com.rhbgroup.dcp.bo.batch.job.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Component
@ConfigurationProperties("job.loadibkjompayfailurevalidationextractionjob")
@EnableConfigurationProperties
@Getter
@Setter
public class LoadIBKJompayFailureValidationExtractionJobConfigProperties {
	private String ftpFolder;
	private String name;
    private String nameDateFormat;
    private int chunkSize;
	private String headerPrefix;
	private String headerColumns;
	private String headerNames;
	private String detailPrefix;
	private String detailColumns;
	private String detailNames;
	private String trailerPrefix;
	private String trailerColumns;
	private String trailerNames;
	private int dayDiff;
}
