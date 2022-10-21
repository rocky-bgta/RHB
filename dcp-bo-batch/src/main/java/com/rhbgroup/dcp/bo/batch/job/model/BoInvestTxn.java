package com.rhbgroup.dcp.bo.batch.job.model;

import java.math.BigDecimal;
import java.sql.Timestamp;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class BoInvestTxn{
	
    private Integer id;
    private String txnId;
    private Integer userId;
    private String refId;
    private String subFunction;
    private String mainFunction;
    private String fromAccountNo;
    private String toAccountNo;
    private String fromAccountName;
    private String toAccountName;
    private String recipientRef;
    private BigDecimal amount;
    private String multiFactorAuth;
    private Timestamp txnTime;
    private String txnStatus;
    private BigDecimal serviceCharge;
    private BigDecimal gstAmount;
    private BigDecimal gstRate;
    private String gstTreatmentType;
    private String gstTaxCode;
    private String gstCalculationMethod;
    private Integer gstTxnId;
    private String fromIpAddress;
    private String gstRefNo;
    private Boolean isQuickPay;
    private Integer toFavouriteId;
    private String txnStatusCode;
    private String fromAccountConnectorCode;
    private String channel;
    private String accessMethod;
    private String paymentMethod;
    private String deviceId;
    private Integer txnTokenId;
    private String subChannel;
    private String curfId;
    private String rejectCode;
    private String rejectDescription;
    private Boolean isSetupFavourite;
    private Boolean isSetupQuickLink;  
    private Boolean isSetupQuickPay;
    private String txnCcy;
    private Timestamp createdTime;
    private String createdBy;
    private Timestamp updatedTime;
    private String updatedBy;
    
}