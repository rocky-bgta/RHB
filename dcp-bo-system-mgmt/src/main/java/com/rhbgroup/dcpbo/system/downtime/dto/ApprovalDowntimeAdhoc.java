package com.rhbgroup.dcpbo.system.downtime.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.rhbgroup.dcpbo.system.common.BoData;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
@JsonInclude
public class ApprovalDowntimeAdhoc implements BoData {
	
	private String name;
	
	private String startTime;
	
	private String endTime;
	
	@JsonProperty
	private Boolean isPushNotification;
	
	private String pushDate;
	
	private String type;
	
	private String adhocType;

	private String adhocCategory;
	
	private String bankName;
	
	private String bankId;
}
