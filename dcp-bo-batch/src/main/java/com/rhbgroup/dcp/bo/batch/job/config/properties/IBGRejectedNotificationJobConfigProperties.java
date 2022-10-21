package com.rhbgroup.dcp.bo.batch.job.config.properties;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.rhbgroup.dcp.bo.batch.framework.config.properties.BaseJobConfigProperties;

import lombok.Getter;
import lombok.Setter;

@Component
@Qualifier("IBGRejectedNotificationJobConfigProperties")
@Lazy
@ConfigurationProperties("job.ibgrejectednotificationjob")
@Getter
@Setter
public class IBGRejectedNotificationJobConfigProperties implements BaseJobConfigProperties {
	private int chunkSize;
	private int jdbcPagingSize;
	private String eventCode;
	private String jmsQueue;
}