package com.rhbgroup.dcp.bo.batch.job.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JompayEmatchingReportPaymentTxn {
	private String channelId;
	private String channelStatus;
	private String applicationId;
	private String acctCtrl1;
	private String acctCtrl2;
	private String acctCtrl3;
	private String accountNo;
	private String debitCreditInd;
	private String userTranCode;
    private Double amount;
    private String txnBranch;
    private String txnDate; //YYYYMMDD
    private String txnTime; //HH24MISS
}
