package com.rhbgroup.dcp.bo.batch.job.model;

import java.math.BigDecimal;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class BatchUpdateMcaInterestRate {
	private long id;
	private String jobExecutionId;
	private long mcaTermInterestrateId;
	private String code;
	private String tenure;
	private BigDecimal interestRate;
	private boolean isProcessed;
	private Date createdTime;
	private Date updatedTime;
}
