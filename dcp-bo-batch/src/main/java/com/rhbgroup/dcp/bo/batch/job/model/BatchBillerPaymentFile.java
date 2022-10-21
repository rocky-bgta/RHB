package com.rhbgroup.dcp.bo.batch.job.model;

import java.sql.Date;
import java.sql.Timestamp;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@EqualsAndHashCode
@ToString
public class BatchBillerPaymentFile {
	private Integer id;
	private Integer billerPaymentConfigId;
	private String billerCode;
	private String billerName;
	private String billerCategory;
	private String fileGeneratedPath;
	private boolean isFileGenerated;
	private boolean isFileDelivered;
	private Timestamp fileDeliveredDate;
	private boolean isError;
	private String errorMessage;
	private Date fileDate;
	private Timestamp createdTime;
	private String createdBy;
	private Timestamp updatedTime;
	private String updatedBy;
	
}
