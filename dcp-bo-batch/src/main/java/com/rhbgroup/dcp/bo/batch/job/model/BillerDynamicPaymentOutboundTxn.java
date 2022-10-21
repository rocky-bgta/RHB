package com.rhbgroup.dcp.bo.batch.job.model;

import com.rhbgroup.dcp.bo.batch.framework.model.BaseModel;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Setter
@Getter
public class BillerDynamicPaymentOutboundTxn extends BaseModel {
	private Map<String,String> fieldMap=new HashMap<>();
	private String txnId; 
	private String refId;
	private String txnDate;
	private String txnAmount;
	private String billRefNo1="";
	private String billRefNo2="";
	private String billRefNo3="";
	private String billRefNo4="";
	private String txnTime;
	private String txnYear;
	private String idNo; 
	private String userAddress1; 
	private String userAddress2; 
	private String userAddress3; 
	private String userAddress4; 
	private String userState;
	private String userCity;
	private String userPostCode;
	private String userCountry;
	private String payMethod;
	private int sequenceNumber;

}
