package com.rhbgroup.dcp.bo.batch.job.model;

import java.math.BigDecimal;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class MCATermInterestRateConfig {

	private Integer id;
	private String currencyCode;
	private BigDecimal interestRate;
	private String tenure;
	private String tenureDescription;
	private Date createdTime;
	private Date updatedTime;
	private String createdBy;
	private String updatedBy;
	
	
}
