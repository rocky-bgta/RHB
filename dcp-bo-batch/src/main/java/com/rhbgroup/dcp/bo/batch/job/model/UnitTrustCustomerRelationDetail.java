package com.rhbgroup.dcp.bo.batch.job.model;


import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class UnitTrustCustomerRelationDetail extends UnitTrustFileAbs{
	private String cisNo;
	private String accountNo;
	private String joinType;

}
