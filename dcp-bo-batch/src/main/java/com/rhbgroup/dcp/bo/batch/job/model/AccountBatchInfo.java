package com.rhbgroup.dcp.bo.batch.job.model;

import java.util.Date;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
@ToString
@Getter
@Setter
public class AccountBatchInfo {
	private int id;
	private String accountType;
	private Date startTime;
	private Date endTime;
	private String targetDataset;
	private String updatedBy;
	private Date updatedTime;
	
}
