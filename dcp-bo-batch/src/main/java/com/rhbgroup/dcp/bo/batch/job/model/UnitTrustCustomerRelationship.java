package com.rhbgroup.dcp.bo.batch.job.model;


import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class UnitTrustCustomerRelationship extends UnitTrust {
	private String cisNo;
	private String acctNo;
	private String joinType;
}
