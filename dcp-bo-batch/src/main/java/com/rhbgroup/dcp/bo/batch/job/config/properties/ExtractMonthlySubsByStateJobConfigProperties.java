package com.rhbgroup.dcp.bo.batch.job.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import java.util.Map;

@Lazy
@Configuration
@ConfigurationProperties(prefix="job")
@Setter
@Getter
public class ExtractMonthlySubsByStateJobConfigProperties {

    private Map<String, ReportConfig> bnmsubs;

    @Setter
    @Getter
    public static class ReportConfig{
        private int chunksize;
        private int jdbcpagingpagesize;
        private String ftpfolder;
        private String namedateformat;
        private String sqlview;
        private Map<String, String> csv;

        public String getCsvProperty(String key) {
            return csv.get(key);
        }
    }


}