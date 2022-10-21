package com.rhbgroup.dcp.bo.batch.job.model;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class BoTopupTxn {

    private Integer id;
    private Integer userId;
    private String txnId;
    private String refId;
    private String tellerId;
    private String traceId;
    private String mainFunction;
    private String multiFactorAuth;
    private String fromAccountNo;
    private String fromCardNo;
    private Integer toBillerId;
    private String nickname;
    private String ref1;
    private BigDecimal amount;
    private Timestamp txnTime;
    private String txnStatus;
    private String txnStatusCode;
    private BigDecimal totalServiceCharge;
    private BigDecimal billerServiceCharge;
    private String paymentMethod;
    private BigDecimal gstRate;
    private BigDecimal gstAmount;
    private String gstTreatmentType;
    private String gstCalculationMethod;
    private String gstTxnId;
    private String gstTaxCode;
    private String gstRefNo;
    private Boolean isQuickPay;
    private Integer toFavouriteId;
    private String mobilityOneTxnId;
    private String channel;
    private Date updatedTime;
    private String status;
    private String eaiErrorMsg;
    private String eaiErrorCode;
    private String eaiErrorParam;

}
