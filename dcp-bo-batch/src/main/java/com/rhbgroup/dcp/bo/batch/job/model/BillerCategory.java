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
public class BillerCategory {
	private String categoryName;
	private String status;
	private Timestamp createdTime;
	private String createdBy;
	private Timestamp updatedTime;
	private String updatedBy;
	private Integer sortOrder;

}
