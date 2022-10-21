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
public class BatchStagedNotification {
	private long id;
	private long jobExecutionId;
	private String fileName;
	private String eventCode;
	private String keyType;
	private long userId;
	private String data1;
	private String data2;
	private String data3;
	private String data4;
	private String data5;
	private String data6;
	private String data7;
	private String data8;
	private String data9;
	private String data10;
	private boolean isProcessed;
	private Date createdTime;
	private String createdBy;
	private Date updatedTime;
	private String updateBy;
}
