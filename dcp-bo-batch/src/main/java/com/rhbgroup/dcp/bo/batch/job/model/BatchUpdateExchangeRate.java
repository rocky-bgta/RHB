package com.rhbgroup.dcp.bo.batch.job.model;

import java.util.Date;

import org.apache.commons.lang3.builder.ToStringBuilder;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class BatchUpdateExchangeRate {

	private long id;
	private String jobExecutionId;
	private long currencyRateConfigId;
	private String code;
	private String description;
	private Double buyTt;
	private Double sellTt;
	private int unit;
	private boolean isProcessed;
	private Date createdTime;
	private Date updatedTime;

}