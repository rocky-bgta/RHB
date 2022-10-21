package com.rhbgroup.dcp.bo.batch.job.model;

import java.sql.Date;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class BoRegistration{
	
	 private Integer id;
	 private String deviceId;
	 private String token;
	 private String username;	 
	 private String cisNo;
	 private String email;	 
	 private String accountNumber;
	 private String channel;
	 private Boolean isOtpVerified;
	 private String auditAdditionalData;
	 private String userType;
	 private Boolean isStaff;
	 private String challengeQuestion;
	 private Boolean isPremier;
	 private String challengeAnswer;
	 private String name;
	 private String idNo; 
	 private String idType;
	 private String mobileNo;
	 private Date createdTime;
	 private String ipAddress;
	 private String createdBy;
	 private String updatedBy; 
	 private Date updatedTime;
	 private Boolean isIslamic;
	 private Boolean isOtpRegisteredMobile;
	 private Boolean isActive;
	 private String txnStatus;
	 private String residentialState;
	 private String accountType;
	 private String branchIncentiveCode;

}