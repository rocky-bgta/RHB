package com.rhbgroup.dcp.bo.batch.job.model;


import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class BoRegistrationMode{
	
	 private String channel;
	 private String accountType;
	 private Boolean isIslamic;
	 private String txnStatus;
	 private int count;
}