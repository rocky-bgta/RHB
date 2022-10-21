package com.rhbgroup.dcp.bo.batch.job.config.properties;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

@Lazy
@Configuration
@ConfigurationProperties(prefix="job.blacklistedmsicjob")
@Setter
@Getter
public class BlacklistedMsicJobConfigProperties {
	private int chunkSize;
	private String headerprefix;
	private String headerDelimiter;
	private String headercolumns;
	private String headernames;
	private String detailprefix;
	private String detailDelimiter;
	private String detailcolumns;
	private String detailnames;
	private String trailerprefix;
	private String trailerDelimiter;
	private String trailercolumns;
	private String trailernames;
	private String name;
	private String nameDateFormat;
	private String ftpFolder;
	private String eventCode;
	private String batchCode;
	private int dayDiff;
}