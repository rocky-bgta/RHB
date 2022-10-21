package com.rhbgroup.dcp.bo.batch.job.model;

import java.sql.Timestamp;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class MsicConfig {
	private long id;	
	private String msic;
	private String description;
	private String accountType;
	private boolean isIslamicCompliance;
	private String status;
	private Timestamp createdTime;
	private String createdBy;
	private Timestamp updatedTime;
	private String updatedBy;
}
