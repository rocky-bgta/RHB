package com.rhbgroup.dcp.bo.batch.job.config.properties;

import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import lombok.Getter;
import lombok.Setter;

@Lazy
@Configuration
@ConfigurationProperties(prefix="job.extractlogindatafirsttime")
@Setter
@Getter

public class ExtractDailyFirstTimeLoginJobConfigProperties {
    private int chunksize;
    private int jdbcpagingpagesize;
    private String ftpfolder;
    private String namedateformat;
    private Map<String, String> csv;
    private Map<String, String> txt;
    
    public String getCsvProperty(String key) {
    	return csv.get(key);
    }
    
    public String getTxtProperty(String key) {
    	return txt.get(key);
    }
}
