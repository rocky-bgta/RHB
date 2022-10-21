package com.rhbgroup.dcp.bo.batch.job.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
public class AsnbBatchDetails {

	private String bodyInd;
	private String channelType;
	private String requestIdentificaiton;
	private String deviceOwner;
	private String unitHolderId;
	private String uhName;
	private String identificationType;
	private String identificationNumber;
	private String fundId;
	private String amountApplied;
	private String transactionDate;
	private String transactionTime;
	private String bnkTxnRefNumber;
	private String bnkCustomerPhnNumber;
	private String bankAccountNumber;
	private String transactionStatus;
	private String unitsAlloted;
	private String transactionNumber;
	private String transactionCode;
	private String endOfRecord;
}
