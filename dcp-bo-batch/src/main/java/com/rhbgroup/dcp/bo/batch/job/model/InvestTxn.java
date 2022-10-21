package com.rhbgroup.dcp.bo.batch.job.model;

import java.math.BigDecimal;
import java.sql.Timestamp;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class InvestTxn{
	
    private Integer id;
    private Integer userId;
    private String txnId;
    private String refId;
    private String mainFunction;
    private String subFunction;
    private String fromAccountNo;
    private String fromAccountName;
    private String toAccountNo;
    private String toAccountName;
    private BigDecimal amount;
    private String recipientRef;
    private String multiFactorAuth;
    private String txnStatus;
    private Timestamp txnTime;
    private BigDecimal serviceCharge;
    private BigDecimal gstRate;
    private BigDecimal gstAmount;
    private String gstTreatmentType;
    private String gstCalculationMethod;
    private String gstTaxCode;
    private Integer gstTxnId;
    private String gstRefNo;
    private Boolean isQuickPay;
    private String fromIpAddress;
    private String txnStatusCode;
    private String fromAccountConnectorCode;
    private Integer toFavouriteId;
    private String channel;
    private String paymentMethod;
    private String accessMethod;
    private String deviceId;
    private String subChannel;
    private Integer txnTokenId;
    private String curfId;
    private String rejectDescription;
    private String rejectCode;
    private Boolean isSetupFavourite;
    private Boolean isSetupQuickLink;  
    private Boolean isSetupQuickPay;
    private String txnCcy;
    private Timestamp createdTime;
    private String createdBy;
    private Timestamp updatedTime;
    private String updatedBy;
    
}