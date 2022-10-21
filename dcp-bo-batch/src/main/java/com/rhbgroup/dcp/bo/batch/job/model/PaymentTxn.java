package com.rhbgroup.dcp.bo.batch.job.model;

import java.math.BigDecimal;
import java.sql.Date;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PaymentTxn {

    private Integer id;
    private Integer userId;
    private String txnId;
    private String refId;
    private String multiFactorAuth;
    private String mainFunction;
    private String subFunction;
    private String fromAccountNo;
    private String toAccountNo;
    private Integer toBillerId;
    private String toBillerAccountName;
    private String toBillerAccountCodeName;
    private String validateSig;
    private String nickname;
    private String ref1;
    private String ref2;
    private String ref3;
    private String ref4;
    private String recipientRef;
    private String otherPaymentDetail;
    private BigDecimal amount;
    private Boolean realTimeNotification;
    private String bankCodeIbg;
    private BigDecimal totalServiceCharge;
    private String txnStatus;
    private Date txnTime;
    private BigDecimal gstRate;
    private BigDecimal gstAmount;
    private String gstTreatmentType;
    private String gstCalculationMethod;
    private String gstTaxCode;
    private String gstTxnId;
    private String gstRefNo;
    private Boolean isQuickPay;
    private String rrnInfo;
    private String txnStatusCode;
    private Date updatedTime;
    private String fromCardNo;
    private String channel;
    private String paymentMethod;
    private Integer toFavouriteId;
    private String tellerId;
    private String traceId;
    private Boolean isJompayQr;
    private String eaiErrorCode;
    private String eaiErrorMsg;
    private String eaiErrorParam;
    private String rejectedCode;
    private String rejectedDesc;
}
