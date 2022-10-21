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
public abstract class UnitTrust {
	private long jobExecutionId;
	private String processDate;
	private Date batchExtractionTime;
	private Date createdTime;
	private String createdBy;
	private Date updatedTime;
	private String updatedBy;
	private int status;
	private String fileName;
}
