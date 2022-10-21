package com.rhbgroup.dcp.bo.batch.job.model;


import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class UnitTrustAccountDetail extends UnitTrustFileAbs{
	private String accountNo;
	private String signatoryCode;
	private String signatoryDescription;
	private String accountType;
	private String accountStatusCode;
	private String accountStatusDesc;
	private String accountInvestProduct;
	private String lastPerformedTxnDate;
}
