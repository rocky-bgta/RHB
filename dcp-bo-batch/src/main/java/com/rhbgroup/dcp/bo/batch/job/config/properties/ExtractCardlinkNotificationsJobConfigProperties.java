package com.rhbgroup.dcp.bo.batch.job.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import lombok.Getter;
import lombok.Setter;

@Lazy
@Configuration
@ConfigurationProperties(prefix="job.extractcardlinknotificationsprocessorjob")
@Setter
@Getter
public class ExtractCardlinkNotificationsJobConfigProperties {
    private int chunkSize;
    private int jdbcPagingPageSize;
    private String batchCode;
}
