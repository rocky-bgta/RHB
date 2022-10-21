package com.rhbgroup.dcp.bo.batch.job.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import lombok.Getter;
import lombok.Setter;

@Lazy
@Configuration
@ConfigurationProperties(prefix = "job.loadmassnotificationsjob")
@Getter
@Setter
public class LoadMassNotificationsJobConfigProperties {
	private int chunkSize;
	private String headerPrefix;
	private String headerColumns;
	private String headerNames;
	private String detailPrefix;
	private String detailDelimiter;
	private String detailNames;
	private String name;
	private String nameDateFormat;
	private String batchCode;
}
