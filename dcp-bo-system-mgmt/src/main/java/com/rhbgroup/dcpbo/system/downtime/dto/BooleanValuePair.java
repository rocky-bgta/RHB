package com.rhbgroup.dcpbo.system.downtime.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@JsonInclude
public class BooleanValuePair {
	private Boolean before;
	private Boolean after;
	
	public BooleanValuePair() {
	}
	
	public BooleanValuePair(Boolean before, Boolean after) {
		this.before = before;
		this.after = after;
	}
}
