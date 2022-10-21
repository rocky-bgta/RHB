package com.rhbgroup.dcp.bo.batch.job.model;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class AsnbTxn{
	
    private long id;
    private String fundId;
    private String fundLongName;
    private String accountHolderIdType;
    private String accountHolderIdNo;
    private String seqNumber;
    private String membershipNumber;
    private String guardianIdType;
    private String guardianIdNo;
    private String guardianMembershipNo;
    private BigDecimal nav;
    private BigDecimal salesCharge;
    private BigDecimal salesTax;
    private String taxInvoiceNo;
    private Integer txnTokenId;
    private BigDecimal fundPrice;
    private BigDecimal feePercentage;
    private BigDecimal unitsAlloted;
    private BigDecimal estNumOfUnits;
    private String txnNo;
    private Date priceDate;
    private Timestamp createdTime;
    private String createdBy;
    private Timestamp updatedTime;
    private String updatedBy;
    private Boolean isReconciled;
    private String pnbErrorCode;
}