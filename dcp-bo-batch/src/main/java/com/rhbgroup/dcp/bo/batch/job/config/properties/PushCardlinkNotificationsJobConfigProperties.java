package com.rhbgroup.dcp.bo.batch.job.config.properties;

import com.rhbgroup.dcp.bo.batch.framework.config.properties.BaseJobConfigProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import lombok.Getter;
import lombok.Setter;

@Configuration
@Qualifier("CardlinkNotificationsJobConfigProperties")
@Lazy
@ConfigurationProperties(prefix="job.pushcardlinknotificationsjob")
@Setter
@Getter
public class PushCardlinkNotificationsJobConfigProperties implements BaseJobConfigProperties {
	private int chunkSize;
	private int jdbcPagingSize;
	private String eventCode;
	private String jmsQueue;
	private String batchCode;
	private Integer throttleSize;
}
