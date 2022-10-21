package com.rhbgroup.dcp.bo.batch.job.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoadTermDepositDetail extends LoadTermDeposit {
	private String control1;
	private String productType;
	private String productDescription;
	private Integer tenure;
	private Double interestRate;
	private String endDate;
}
