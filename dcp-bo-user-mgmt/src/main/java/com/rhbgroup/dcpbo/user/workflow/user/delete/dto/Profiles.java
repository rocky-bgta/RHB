package com.rhbgroup.dcpbo.user.workflow.user.delete.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@JsonInclude
@Getter
@Setter
public class Profiles {

	private int profileId;
	
	private String operationHoursFromTime;
	
    private String operationHoursToTime;
	    
	private String type;
		
	private boolean isActive;
	private List<String> operationDateTime;
}
