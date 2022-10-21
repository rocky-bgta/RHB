package com.rhbgroup.dcp.bo.batch.job.model;

import java.util.Date;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@EqualsAndHashCode
@ToString
public class UnitTrustFundMasterDetail extends UnitTrustFileAbs {
	private String fundId;
	private String fundName;
	private String fundCurr;
	private String fundCurrNavPrice;
	private String navDate;
	private String prodCategoryCode;
	private String prodCategoryDesc;
	private String riskLevelCode;
	private String riskLevelDesc;
	private String myrNavPrice;

}