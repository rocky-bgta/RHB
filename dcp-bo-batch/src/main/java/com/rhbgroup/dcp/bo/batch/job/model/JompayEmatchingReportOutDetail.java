package com.rhbgroup.dcp.bo.batch.job.model;

import java.util.Date;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JompayEmatchingReportOutDetail {
	String channelRecordType="1";
	String channelId;
	String channelStatus;
	String applicationId;
	String acctControlOne;
	String acctControlTwo;
	String acctControlThree;
	String accountNo;
	String debitCreditInd;
	String userTransCode;
	String transactionAmount;
	String fromAcctControlOne="";
	String fromAcctControlTwo="";
	String fromAcctControlThree="";
	String fromAccountNo="";
	String transactionBranch;
	String transactionDate;
	String transactionTime;
	String transactionSeqNo="";
	String transactionTerminal="";
	String filler="";
}
