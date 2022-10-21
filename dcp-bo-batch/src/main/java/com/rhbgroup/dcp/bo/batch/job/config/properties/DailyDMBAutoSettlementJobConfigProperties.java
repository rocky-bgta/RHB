package com.rhbgroup.dcp.bo.batch.job.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Component
@ConfigurationProperties("job.dailyautosettlementjobdmb")
@EnableConfigurationProperties
@Getter
@Setter
public class DailyDMBAutoSettlementJobConfigProperties extends DailyAutoSettlementJobConfigProperties {

}
