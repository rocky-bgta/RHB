package com.rhbgroup.dcp.bo.batch.job.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

@Lazy
@Configuration
@ConfigurationProperties(prefix="job.extractdebitcarddeliveryjob")
@Setter
@Getter

public class ExtractDebitCardDeliveryJobConfigProperties {
    private int chunksize;
    private int jdbcpagingpagesize;
    private String detailcolumns;
    private String detailnames;
    private String ftpfolder;
    private String name;
    private String namedateformat;
    private String batchtime;
}
