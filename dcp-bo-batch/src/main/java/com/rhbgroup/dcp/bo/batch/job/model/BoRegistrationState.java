package com.rhbgroup.dcp.bo.batch.job.model;


import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class BoRegistrationState{
	
	 private String channel;
	 private String residentialState;
	 private int count;
}