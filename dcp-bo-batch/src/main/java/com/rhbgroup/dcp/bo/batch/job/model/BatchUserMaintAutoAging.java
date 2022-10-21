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
public class BatchUserMaintAutoAging {
	private long jobExecutionId;
	private int userId;
	private String userName;
	private String name;
	private String email;
	private int userDepartmentId;
	private String department;
	private String currentUserStatus;
	private String newUserStatus;
	private Date lastLoginTime;
	private int lastLoginTimeDayDiff;
	private boolean isProcessed;
	private Date createdTime;
	private Date updatedTime;
}
