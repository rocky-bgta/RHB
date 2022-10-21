package com.rhbgroup.dcp.bo.batch.job.config.properties;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.rhbgroup.dcp.bo.batch.framework.config.properties.BaseJobConfigProperties;
import com.rhbgroup.dcp.bo.batch.framework.config.properties.BaseRestAPIConfigProperties;

import lombok.Getter;
import lombok.Setter;

@Component
@Qualifier("EpullEnrollmentJobConfigProperties")
@Lazy
@ConfigurationProperties("job.epullenrollmentjob")
@Getter
@Setter
public class EpullEnrollmentJobConfigProperties extends BaseRestAPIConfigProperties implements BaseJobConfigProperties {
	private int chunkSize;
	private int jdbcPagingPageSize;
	private int maxAttempt;
}