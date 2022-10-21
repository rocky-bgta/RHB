package com.rhbgroup.dcp.bo.batch.job.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import com.rhbgroup.dcp.bo.batch.framework.config.properties.BaseJobConfigProperties;

import lombok.Getter;
import lombok.Setter;

@Lazy
@Configuration
@ConfigurationProperties(prefix = "job.extractsystemadhocjob")
@Getter
@Setter
public class ExtractAdhocDowntimeJobConfigProperties implements BaseJobConfigProperties {
	private int chunkSize;
    private int jdbcPagingSize;
	private String batchCode;
	private String eventCode;
	private String message;
	private int dayDiff;
}
