package com.rhbgroup.dcp.bo.batch.job.model;

import java.math.BigDecimal;
import java.sql.Date;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class TransferTxn{
	
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
    private Integer toBankId;
    private BigDecimal amount;
    private String currencyCode;
    private String recipientRef;
    private String otherPaymentDetails;
    private String multiFactorAuth;
    private String mobileNo;
    private String txnStatus;
    private Date txnTime;
    private BigDecimal serviceCharge;
    private BigDecimal gstRate;
    private BigDecimal gstAmount;
    private String gstTreatmentType;
    private String gstCalculationMethod;
    private String gstTaxCode;
    private String gstTxnId;
    private String gstRefNo;
    private Boolean isQuickPay;
    private String toIdType;
    private String toIdNo;
    private Boolean toResidentStatus;
    private String duitnowCountryCode;
    private String fromIpAddress;
    private String duitnowToRegistrationId;
    private String duitnowToBic;       
    private String txnStatusCode;
    private Date updatedTime;
    private String fromAccountConnectorCode;
    private String tellerId;
    private String traceId;
    private Integer toFavouriteId;
    private String channel;
    private Boolean isPreLogin;
    private String fromCardNo;
    private String paymentMethod;
    private String fromCardHolder;
    private String accessMethod;
    private String deviceId;
    private String subChannel;
    private Integer txnTokenId;
    private String curfId;
    private String firstInstalmentDate;
    private Boolean iseFestive;

}