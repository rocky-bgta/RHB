package com.rhbgroup.dcp.bo.batch.job.model;

import java.util.Date;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class UnitTrustAccount extends UnitTrust {
	private String acctNo;
	private String signatoryCode;
	private String signatoryDesc;
	private String acctType;
	private String acctStatusCode;
	private String acctStatusDesc;
	private String investProd;
	private Date lastPerformedTxnDate;
}
