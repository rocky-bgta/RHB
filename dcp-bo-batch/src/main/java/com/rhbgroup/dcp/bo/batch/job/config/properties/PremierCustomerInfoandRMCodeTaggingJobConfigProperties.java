package com.rhbgroup.dcp.bo.batch.job.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import lombok.Getter;
import lombok.Setter;

@Lazy
@Configuration
@ConfigurationProperties(prefix="job.premiercustomerinfoandrmcodetaggingjob")
@Setter
@Getter

public class PremierCustomerInfoandRMCodeTaggingJobConfigProperties {
	private int chunkSize;
	private String headerIndicator;
	private String footerIndicator;
	private String detailIndicator;
	private String detailColumns;
	private String detailNames;
	private String ftpFolder;
	private String name;
	private String nameDateFormat;
}
