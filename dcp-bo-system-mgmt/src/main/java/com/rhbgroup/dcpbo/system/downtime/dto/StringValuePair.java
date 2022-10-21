package com.rhbgroup.dcpbo.system.downtime.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@JsonInclude
public class StringValuePair {
	private String before;
	private String after;
	
	public StringValuePair() {
	}
	
	public StringValuePair(String before, String after) {
		this.before = before;
		this.after = after;
	}
}
