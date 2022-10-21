package com.rhbgroup.dcp.bo.batch.job.config.properties;

import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import lombok.Getter;
import lombok.Setter;

@Lazy
@Configuration
@ConfigurationProperties(prefix="job.extractsmsotpnotificationjob")
@Setter
@Getter

public class ExtractSmsOtpNotificationJobConfigProperties {
    private int chunksize;
    private int jdbcpagingpagesize;
    private String ftpfolder;
    private String namedateformat;
    private Map<String, String> csv;
    
    public String getCsvProperty(String key) {
    	return csv.get(key);
    }
}
