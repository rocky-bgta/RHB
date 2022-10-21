package com.rhbgroup.dcp.bo.batch.job.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@EqualsAndHashCode
@ToString
public class BlacklistedMsicDetail extends BlacklistedMsic {
	private String id;
	private String msicCode;
	private String msic;
	private String description;
	private String accountType;
	private String islamicIndicator;
	private String status;
	private String createApprovedId;
	private String updateApprovedId;
	private String createApprovedDate;
	private String updateApprovedDate;
}
