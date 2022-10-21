package com.rhbgroup.dcp.bo.batch.job.model;

import java.math.BigDecimal;
import java.sql.Date;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class TopupTxn {

	 	private Integer id;
	    private Integer userId;
	    private String txnId;
	    private String refId;
	    private String mainFunction;
	    private String multiFactorAuth;
	    private String fromAccountNo;
	    private String fromCardNo;
	    private Integer toBillerId;
	    private String nickname;
	    private String ref1;
	    private BigDecimal amount;
	    private Date txnTime;
	    private String txnStatus;
	    private String txnStatusCode;
	    private BigDecimal totalServiceCharge;
	    private String paymentMethod;
	    private BigDecimal gstRate;
	    private BigDecimal gstAmount;
	    private String gstTreatmentType;
	    private String gstCalculationMethod;
	    private String gstTaxCode;
	    private String gstTxnId;
	    private String gstRefNo;
	    private Integer toFavouriteId;
	    private Boolean isQuickPay;
	    private String mobilityOneTxnId;
	    private Date updatedTime;
	    private String channel;
		private String tellerId;
	    private String traceId;
	    private String eaiErrorCode;
	    private String eaiErrorMsg;
	    private String eaiErrorParam;

}
