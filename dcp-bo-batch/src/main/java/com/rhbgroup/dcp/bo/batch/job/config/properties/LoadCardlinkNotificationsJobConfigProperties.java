package com.rhbgroup.dcp.bo.batch.job.config.properties;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

@Lazy
@Configuration
@ConfigurationProperties(prefix="job.loadcardlinknotificationsjob")
@Setter
@Getter
public class LoadCardlinkNotificationsJobConfigProperties {
	private int chunkSize;
	private String headerPrefix;
	private String headerColumns;
	private String headerNames;
	private String detailPrefix;
	private String detailDelimiter;
	private String detailNames;
	private String trailerPrefix;
	private String trailerColumns;
	private String trailerNames;
	private String name;
	private String nameDateFormat;
	private String ftpFolder;
	private String eventCode;
	private String batchCode;
	private int dayDiff;
}
