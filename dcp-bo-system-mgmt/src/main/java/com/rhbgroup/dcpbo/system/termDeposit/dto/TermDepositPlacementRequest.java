package com.rhbgroup.dcpbo.system.termDeposit.dto;

import com.rhbgroup.dcpbo.system.common.BoData;
import com.rhbgroup.dcp.model.webfraud.RequestPayload;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
public class TermDepositPlacementRequest implements BoData{

	private Integer txnTokenId;
	private String mainFunction;
	private String subFunction;
	private String cisNo;
	private String frCtrl3;
	private String categoryName;
	private String fromAccountNo;
	private String fdAcctNo;
	private String productCode;
	private Integer tenure;
	private BigDecimal amount;
	private String entity;
	private String productName;
	private Boolean isCreditToPrinciple;
	private String refId;
	private String interestDistCode;
	private String collCtrl3;
	private String affiliateAcctCtrl3;
	private String affiliateAcctNo;
	private String autoRenewal;
	private String userCd1;
	private String connectorCode;
	private BigDecimal serviceCharge;
	private BigDecimal gstRate;
	private BigDecimal gstAmount;
	private String gstTreatmentType;
	private String gstCalculationMethod;
	private String gstTaxCode;
	private String gstTxnId;
	private String gstRefNo;
	private String fromBank;
	private String stpIndicator;
	private String ref1;
	private String ref2;
	private String senderName;
	private String recipientRef;
	private String otherPaymentDetails;
	private String beneficiaryName;
	private String bankCode;
	private String statementType;
	private RequestPayload aaop;
	private String channel;
	private String ipAddress;
	private BigDecimal principalAmount;
	
	//added nibk2009 specific members
	private String productType;
	private String fdAccountNo;
	private String specialRate;
	private String profitRatio;
	private String withHoldingTax;
	
}
