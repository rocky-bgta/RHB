package com.rhbgroup.dcp.bo.batch.job.model;

import java.math.BigDecimal;
import java.sql.Timestamp;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class BoMcaTxn{
	
    private Integer id;
    private String toCcy;
    private Integer txnTokenId;
    private BigDecimal toAmount;
    private String rateType;
    private BigDecimal conversionRate;
    private String purposeCode;
    private Boolean isStaff;
    private String purposeText;
    private BigDecimal feaUtilization;
    private Integer tenureDays;
    private String tenureCode;
    private Timestamp valueDate;
    private String productCode;
    private Timestamp maturityDate;
    private Timestamp createdTime;
    private Timestamp updatedTime;
    private String createdBy;
    private String updatedBy;
    private BigDecimal interestAmount;
    private BigDecimal interestRate;
    private String shortName;
    private BigDecimal limitAmt;
    private String limitCcy;
    private BigDecimal rateMyr;
    private BigDecimal amtMyr;
    private BigDecimal rateUsd;
    private BigDecimal payoutAmount;
    private BigDecimal amtUsd;
    private String termRefNo;

}