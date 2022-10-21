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
public class WorkflowDeleteDowntimeApproval implements BoData {
	private Integer id;
	private String name;
	private String startTime;
	private String endTime;
	private Boolean isPushNotification;
	private String pushDate;
	private String type;
	private String adhocType;
	private String adhocCategory;
	private String bankName;
	private String bankId;
}
