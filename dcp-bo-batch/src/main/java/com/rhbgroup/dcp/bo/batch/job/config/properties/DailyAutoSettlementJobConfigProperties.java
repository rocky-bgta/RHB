package com.rhbgroup.dcp.bo.batch.job.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Component
@ConfigurationProperties("job.dailyautosettlementjob")
@EnableConfigurationProperties
@Getter
@Setter
@Primary
public class DailyAutoSettlementJobConfigProperties {
	
	//header
	private String ftpType;
	private String jobName;
	private String jobNumber;
	private String procStep;
	private String progId;
	private String userId;
	//body
	
	private String ftpTypeSummary;
	private String stlmtCtl2Code;
	private String stlmtCtl3Code;
	private String fAccNum;
	private String drApplID;
	private String crApplID;
	private String drTranCode;
	private String crTranCode;
	private String drCrRecipRefPrefix;
	private String channelCode;
	private String subChannel;
	private String pgmID;

	//footer
	private String ftpTypeFooter;
}
