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
public class BatchStagedOlaCasaNotification {
	 private long id;
	 private String jobExecutionId;
	 private String activitiesName;
	 private int userId;
	 private String eventCode;
	 private Date createdTime;
	 private String createdBy;
	 private Date updatedTime;
	 private String updatedBy;
}