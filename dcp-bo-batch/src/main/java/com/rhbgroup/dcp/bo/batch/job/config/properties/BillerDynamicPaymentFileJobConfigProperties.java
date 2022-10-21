package com.rhbgroup.dcp.bo.batch.job.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import lombok.Getter;
import lombok.Setter;

@Configuration
@Lazy
@ConfigurationProperties(prefix="job.billerdynamicpaymentfilejob")
@Setter
@Getter
public class BillerDynamicPaymentFileJobConfigProperties {
	private int chunkSize;
	private String detailColumns;
	private String detailNames;
	private String name;
	private String nameDateFormat;
	private String templateNamel;
	private String masterFtpFolder;
	private String middleName;

}
