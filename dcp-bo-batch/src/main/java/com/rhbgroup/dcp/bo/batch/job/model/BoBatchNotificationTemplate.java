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
public class BoBatchNotificationTemplate {
	 private long id;
	 private String eventCode;
	 private Boolean emailFlag;
	 private String emailSubjectTemplate;
	 private String emailBodyTemplate;
	 private Boolean pushFlag;
	 private String pushTitleTemplate;
	 private String pushBodyTemplate;
	 private Boolean inboxFlag;
	 private String inboxSubjectTemplate;
	 private String inboxBodyTemplate;
	 private Date createdTime;
	 private String createdBy;
	 private Date updatedTime;
	 private String updatedBy;
}