package com.rhbgroup.dcp.bo.batch.job.config.properties;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import lombok.Getter;
import lombok.Setter;

@Lazy
@Configuration
@ConfigurationProperties(prefix="job.loademunittrustjob")
@Setter
@Getter
public class LoadEMUnitTrustJobConfigProperties {
	private String batchCode;
	private int chunkSize;
	private int jdbcPagingSize;
	private String delimiter;
	private String ftpFolder;
	private String maxTargetSetKey;
	private String utCustomerFile;
	private String utCustomerRelFile;
	private String utAccountFile;
	private String utAccountHldFile;
	private String utFundFile;
	private String utBatchAccountInfoKey;
	private List<UTFile> utFiles = new ArrayList<>();
	
	@Setter
	@Getter
	public static class UTFile {
		private String name;
		private String nameDateFormat;
		private String headerPrefix;
		private String headerNames;
		private String detailPrefix;
		private String detailNames;
		private String trailerPrefix;
		private String trailerNames;
		private String downloadFilePath;
		private int dayDiff;
		private int status;
	}
}
