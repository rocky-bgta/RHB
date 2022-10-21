package com.rhbgroup.dcp.bo.batch.job.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

@Lazy
@Configuration
@ConfigurationProperties(prefix="job.loadibkprepaidreloadjob")
@Setter
@Getter

public class PrepaidReloadFileFromIBKJobConfigProperties {
    private int chunksize;
    private int jdbcpagingpagesize;
    private String delimiter;
    private String detailnames;
    private String ftpfolder;
    private String namecasa;
    private String namecc;
    private String namedateformat;
}