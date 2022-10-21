package com.rhbgroup.dcp.bo.batch.job.model;

import java.sql.Timestamp;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@EqualsAndHashCode
@ToString
public class BatchStagedDowntimeNotification {
	private long id;
	private long jobExecutionId;
	private String type;
	private String adhocType;
	private String eventCode;
	private String content;
	private long userId;
	private boolean isProcessed;
	private Timestamp startTime;
	private Timestamp endTime;
	private Timestamp createdTime;
	private String createdBy;
	private Timestamp updatedTime;
	private String updatedBy;
	private String adhocTypeCategory;
}
