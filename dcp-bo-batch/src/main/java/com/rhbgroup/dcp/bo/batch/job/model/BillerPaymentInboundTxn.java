package com.rhbgroup.dcp.bo.batch.job.model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class BillerPaymentInboundTxn {

	private String txnId; 
	private String txnDate;
	private String txnAmount;
	private String txnType;
	private String txnDesc;
	private String billRefNo1="";
	private String billRefNo2="";
	private String billRefNo3="";
	private String txnTime;
	
}
