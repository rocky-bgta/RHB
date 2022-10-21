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
public class UpdateApprovalDowntimeAdhoc implements BoData {
	
	private Integer id;
	
	private String name;
	
	private String startTime;
	
	private String endTime;
	
	@JsonProperty
	private Boolean isPushNotification;
	
	private String pushDate;
	
	private String type;
	
	private String adhocType;
	
	private String bankId;
	
	private String bankName;
	
	private String adhocCategory;

}
