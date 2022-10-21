package com.rhbgroup.dcpbo.system.downtime.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.rhbgroup.dcpbo.system.common.BoData;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@JsonInclude
public class WorkflowDowntimeApproval implements BoData {
	private Integer approvalId;
	private StringValuePair name;
	private StringValuePair startTime;
	private StringValuePair endTime;
	private BooleanValuePair isPushNotification;
	private StringValuePair pushDate;
	private StringValuePair type;
	private StringValuePair adhocType;
	private StringValuePair adhocCategory;
	private StringValuePair bankName;
	private StringValuePair bankId;
	private String actionType;
	private String reason;
	private String createdBy;
	private String createdTime;
	private String updatedBy;
	private String updatedTime;
	private String creatorName;
	private String isCreator;
	private String approvalStatus;
}
