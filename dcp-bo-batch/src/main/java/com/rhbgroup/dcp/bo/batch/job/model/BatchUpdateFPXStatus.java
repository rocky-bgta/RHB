package com.rhbgroup.dcp.bo.batch.job.model;

import java.math.BigDecimal;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class BatchUpdateFPXStatus {
	private long id;
	private long jobExecutionId;
	private long txnTokenId;
	private String mainFunction;
	private String subFunction;
	private String bankId;
	private String buyerName;
	private String buyerEmail;
	private String sellerBankCode;
	private String sellerExId;
	private String sellerExOrderNo;
	private String sellerId;
	private String sellerOrderNo;
	private Date sellerTxnTime;
	private BigDecimal txnAmount;
	private String txnStatus;
	private String debitAuthCode;
	private String debitAuthNo;
	private String creditAuthCode;
	private String creditAuthNo;
	private String txnDescription;
	private String productDescription;
	private String txnId;
	private Date txnTime;
	private boolean isProcessed;
	private Date createdTime;
	private Date updatedTime;
}
