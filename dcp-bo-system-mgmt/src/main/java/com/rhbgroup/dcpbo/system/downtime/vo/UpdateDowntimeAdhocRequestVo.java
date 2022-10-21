package com.rhbgroup.dcpbo.system.downtime.vo;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.rhbgroup.dcpbo.system.common.BoData;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateDowntimeAdhocRequestVo implements BoData {
	
	@NotNull
	private int functionId;
	
	@NotNull
	private String name;
	
	@NotNull
	private String adhocCategory;
	
	@NotNull
	private String adhocType;
	
	@NotNull
	private String bankId;
	
	@NotNull
	private String startTime;
	
	@NotNull
	private String endTime;
	
	@NotNull
	@JsonProperty
	private boolean isPushNotification;
	
	private String pushDate;

}
