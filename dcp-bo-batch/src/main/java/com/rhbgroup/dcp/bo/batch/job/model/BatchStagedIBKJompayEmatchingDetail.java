package com.rhbgroup.dcp.bo.batch.job.model;

import java.util.Date;

import com.rhbgroup.dcp.bo.batch.framework.model.BaseModel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class BatchStagedIBKJompayEmatchingDetail extends BatchStagedIBKJompayEmatching {
	private String channelId;
	private String channelStatus;
	private String applicationId;
	private String acctCtrl1;
	private String acctCtrl2;
	private String acctCtrl3;
	private String accountNo;
	private String debitCreditInd;
	private String userTranCode;
	private double amount;
	private String txnBranch;
	private String txnDate;
	private String txnTime;
	private String fileName;
	private Date createdTime;
	private String filler;
	private String jobExecutionId;
}
