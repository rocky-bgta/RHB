package com.rhbgroup.dcp.bo.batch.job.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Component
@ConfigurationProperties("job.asnbreconciliationrepotjob")
@EnableConfigurationProperties
@Getter
@Setter
public class ExtractASNBReconiliationReportJobConfigProperties {
	
	private String ftpFolder;
	private String outputfolder;
	private String name;
    private String nameDateFormat;
    private String channelType;
    private String hostfolder;
    private String echannelFtpFolder;

}
