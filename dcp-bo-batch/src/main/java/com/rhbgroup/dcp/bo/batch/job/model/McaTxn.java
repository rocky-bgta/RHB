package com.rhbgroup.dcp.bo.batch.job.model;

import java.math.BigDecimal;
import java.sql.Timestamp;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class McaTxn{
	
    private Integer id;
    private Integer txnTokenId;
    private String toCcy;
    private BigDecimal toAmount;
    private BigDecimal conversionRate;
    private String rateType;
    private String purposeCode;
    private String purposeText;
    private Boolean isStaff;
    private BigDecimal feaUtilization;
    private String tenureCode;
    private Integer tenureDays;
    private Timestamp valueDate;
    private Timestamp maturityDate;
    private String productCode;
    private Timestamp createdTime;
    private String createdBy;
    private Timestamp updatedTime;
    private String updatedBy;
    private BigDecimal interestRate;
    private BigDecimal interestAmount;
    private String shortName;
    private String limitCcy;
    private BigDecimal limitAmt;
    private BigDecimal rateUsd;
    private BigDecimal rateMyr;
    private BigDecimal amtMyr;
    private String termRefNo;
    private BigDecimal payoutAmount;
    private BigDecimal amtUsd;

}