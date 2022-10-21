package com.rhbgroup.dcp.bo.batch.job.model;

import java.sql.Timestamp;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class BatchStagedMsicConfig {
	private long id;
	private long jobExecutionId;
	private String msicId;
	private String msic;
	private String description;
	private String accountType;
	private boolean isIslamicCompliance;
	private String status;
	private boolean isProcessed;
	private String fileName;
	private Timestamp processDate;
	private Timestamp createdTime;
}
