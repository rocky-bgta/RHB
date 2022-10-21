package com.rhbgroup.dcp.bo.batch.job.model;

import java.sql.Date;
import java.sql.Timestamp;

import com.rhbgroup.dcp.bo.batch.framework.model.BaseModel;

import lombok.Getter;
import lombok.Setter;
@Setter
@Getter

public class JompayBiller extends BaseModel {
	private int id;
	private String billerCode;
	private String billerName;
	private String status;
    private Timestamp createdTime;
    private String createdBy;
    private Timestamp updatedTime;
    private String updatedBy;
}
