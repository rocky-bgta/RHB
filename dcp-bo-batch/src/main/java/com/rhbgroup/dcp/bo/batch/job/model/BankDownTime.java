package com.rhbgroup.dcp.bo.batch.job.model;

import java.util.Date;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BankDownTime {
	private int id;
	private long jobExecutionId;
	private int bankId;
	private Date startDatetime;
	private Date endDatetime;
	private String mainFunction;
	private Date createdTime;
	private String createdBy;
	private Date updatedTime;
	private String updatedBy;
}
