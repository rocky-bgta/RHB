package com.rhbgroup.dcp.bo.batch.job.model;

import java.sql.Timestamp;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class DepositProduct {
	private String depositType;
	private String productCode;
	private String productName;
	private Timestamp createdTime;
	private String createdBy;
	private Timestamp updatedTime;
	private String updatedBy;
	private boolean isIslamic;
	private Integer tdCategoryId;
	private Double interestRate;
	private Integer tenure;
	private Timestamp promoStartDate;
	private Timestamp promoEndDate;
	private Double minAmount;
	private Double maxAmount;
	private Double maxFpxAmount;
	private Integer minAge;
	private String uspContent;
	private String sourceOfFundAllowed;
	private String rsaCustomFactValue;

}
