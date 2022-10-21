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
public class BatchSuspense {
	private long id;
	private long jobExecutionId;
	private String batchJobName;
	private Date createdTime;
	private String suspenseColumn;
	private String suspenseType;
	private String suspenseMessage;
	private String suspenseRecord;
}