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
public class BatchStagedNotifMass {
	private long id;
	private long jobExecutionId;
	private String fileName;
	private String eventCode;
	private String content;
	private long userId;
	private boolean isProcessed;
	private Date createdTime;
	private String createdBy;
	private Date updatedTime;
	private String updatedBy;
}
