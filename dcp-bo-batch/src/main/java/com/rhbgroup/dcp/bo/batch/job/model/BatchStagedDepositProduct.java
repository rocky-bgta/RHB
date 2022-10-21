package com.rhbgroup.dcp.bo.batch.job.model;

import java.sql.Timestamp;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class BatchStagedDepositProduct {
	private long id;
	private long jobExecutionId;
	private String depositType;
	private String productCode;
	private String productName;
	private int tenure;
	private double interestRate;
	private String islamic;
	private Timestamp promoEndDate;
	private String processed;
	private String fileName;
	private Timestamp processDate;
	private Timestamp createdTime;
}
