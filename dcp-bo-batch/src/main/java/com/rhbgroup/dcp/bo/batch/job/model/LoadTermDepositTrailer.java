package com.rhbgroup.dcp.bo.batch.job.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoadTermDepositTrailer  extends LoadTermDeposit{
	private String totalRecordCount;
	private double totalAmount;
	private String endIndicator;
}
