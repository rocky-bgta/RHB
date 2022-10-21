package com.rhbgroup.dcp.bo.batch.job.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import java.util.Map;

@Lazy
@Configuration
@ConfigurationProperties(prefix="job.epullautoenrollment")
@Setter
@Getter
public class EPullAutoEnrollmentJobProperties {
    private int chunksize;
    private int jdbcpagingpagesize;
    private String ftpfolder;
    private String namedateformat;
    private Map<String, String> txt;

    public String getTxtProperty(String key) {
        return txt.get(key);
    }
}
