package com.rhbgroup.dcp.bo.batch.job.model;

import java.sql.Timestamp;

import com.rhbgroup.dcp.bo.batch.framework.model.BaseModel;

import lombok.Getter;
import lombok.Setter;
@Setter
@Getter
public class StagedDepositProducts extends BaseModel{

	private String productCode;
	private double interestRate;
	private Timestamp promoEndDate;
	private String productName;
}
