package com.rhbgroup.dcp.bo.batch.job.model;

import java.util.Date;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class UnitTrustCustomerDetail extends UnitTrustFileAbs{
	private String cisNo;
	private String customerName;
}
