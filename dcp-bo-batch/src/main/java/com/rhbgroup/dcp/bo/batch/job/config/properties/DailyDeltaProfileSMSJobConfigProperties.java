package com.rhbgroup.dcp.bo.batch.job.config.properties;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import com.rhbgroup.dcp.bo.batch.framework.config.properties.BaseJobConfigProperties;

import lombok.Getter;
import lombok.Setter;

@Configuration
@Qualifier("DailyDeltaProfileSMSJobConfigProperties")
@Lazy
@ConfigurationProperties(prefix="job.dailydeltaprofilesmsjob")
@Setter
@Getter
public class DailyDeltaProfileSMSJobConfigProperties implements BaseJobConfigProperties {
	private int chunkSize;
	private int jdbcPagingSize;
	private String eventCode;
	private String jmsQueue;
}
