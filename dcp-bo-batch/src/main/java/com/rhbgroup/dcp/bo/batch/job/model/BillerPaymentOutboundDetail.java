package com.rhbgroup.dcp.bo.batch.job.model;
import com.rhbgroup.dcp.bo.batch.framework.model.BaseModel;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class BillerPaymentOutboundDetail extends BaseModel {
	private String recordType="D";
	private String txnId;
	private String txnDate;
	private String txnAmount;
	private String txnType="CR";
	private String txnDesc="";
	private String billRefNo1;
	private String billRefNo2;
	private String billRefNo3;
	private String txnTime;
	private String filler;
}
