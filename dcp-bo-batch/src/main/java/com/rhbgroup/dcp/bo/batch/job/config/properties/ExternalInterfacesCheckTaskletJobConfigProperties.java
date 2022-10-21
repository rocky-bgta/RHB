package com.rhbgroup.dcp.bo.batch.job.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import lombok.Getter;
import lombok.Setter;

@Lazy
@Configuration
@ConfigurationProperties(prefix = "job.externalinterfacescheckjob")
@Setter
@Getter
public class ExternalInterfacesCheckTaskletJobConfigProperties {

	private boolean enableDBCheckConnectivity;
	private boolean enableJasperCheckConnectivity;
	private boolean enableFileSendToFtpCheck;
	private boolean enableJMSCheck;
	private boolean enableIBKSftpCheckConnectivity;
	private boolean enableIBKFtpCheckConnectivity;
	private boolean enableIBKSftpDownloadCheck;
	private boolean enableIBKFtpDownloadCheck;

	// jms
	private String jmsSmsQueue;

	// ftp send
	private String ftpDcpTargetFolderPath;	

	// non-secure ftp download
	private String ftpIBKFoldertolist;
	
	// secure ftp download
	private String ftpIBKPrepaidFoldertolist;
}
