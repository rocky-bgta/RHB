package com.rhbgroup.dcpbo.system.downtime.whitelist.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.rhbgroup.dcpbo.system.common.BoData;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
@JsonInclude
public class ApprovalDowntimeAdhocWhitelistPayload implements BoData {
	
	private Integer id;
	
	private String type;
	
	private Integer userId;
	
	private String name;
	
	private String mobileNo;
	
	private String username;
	
	private String idNo;
	
	private String idType;
	
	private String cisNo;

}
