package com.rhbgroup.dcp.bo.batch.job.model;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class BoPaymentTxn {

    private Integer id;
    private String txnId;
    private Integer userId;
    private String refId;
    private String mainFunction;
    private String multiFactorAuth;
    private String subFunction;
    private String toAccountNo;
    private String fromAccountNo;
    private Integer toBillerId;
    private String toBillerAccountCodeName;
    private String toBillerAccountName;
    private String validateSig;
    private String ref1;
    private String nickname;
    private String ref2;
    private String ref4;
    private String ref3;
    private String recipientRef;
    private BigDecimal amount;
    private String otherPaymentDetail;
    private String bankCodeIbg;
    private Boolean realTimeNotification;
    private BigDecimal billerServiceCharge;
    private String txnStatus;
    private BigDecimal totalServiceCharge;
    private BigDecimal gstRate;
    private BigDecimal gstAmount;
    private Timestamp txnTime;
    private String gstCalculationMethod;
    private String gstTreatmentType;
    private String gstTaxCode;
    private String gstRefNo;
    private String gstTxnId;
    private Boolean isQuickPay;
    private String txnStatusCode;
    private String rrnInfo;
    private Date updatedTime;
    private String channel;
    private String fromCardNo;
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
