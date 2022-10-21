package com.rhbgroup.dcpbo.system.downtime.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
@JsonInclude
public class BankDetails {
	
	private int id;
	private String name;
	private String shortName;
	private String isIbg;
	private String isInstant;
	

}
