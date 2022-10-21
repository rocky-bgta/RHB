package com.rhbgroup.dcp.bo.batch.job.model;

import java.sql.Timestamp;
import java.util.Date;

import com.rhbgroup.dcp.bo.batch.framework.model.BaseModel;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class StagedMsicConfigs{
	private long id;
	private long jobExecutionId;
	private String msic;
	private String description;
	private String accountType;
	private boolean islamicCompliance;
	private String status;
    private Date createdTime;
    private String createdBy;
    private Date updatedTime;
    private String updatedBy;
}
