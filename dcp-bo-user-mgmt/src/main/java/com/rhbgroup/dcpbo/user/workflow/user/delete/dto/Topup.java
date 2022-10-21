package com.rhbgroup.dcpbo.user.workflow.user.delete.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@JsonInclude
@Getter
@Setter
public class Topup {
	
	private Integer id;

    private String name;
    
    private boolean enabled;
	
	

}
