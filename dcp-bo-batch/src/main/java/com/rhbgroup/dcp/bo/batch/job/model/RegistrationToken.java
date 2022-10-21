package com.rhbgroup.dcp.bo.batch.job.model;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class RegistrationToken{
	
	 private Integer id;
	 private String token;
	 private String deviceId;
	 private String username;	 
	 private String email;	 
	 private String cisNo;
	 private String accountNumber;
	 private Boolean isOtpVerified;
	 private String channel;
	 private String auditAdditionalData;
	 private Boolean isStaff;
	 private String userType;
	 private String challengeQuestion;
	 private String challengeAnswer;
	 private Boolean isPremier;
	 private String name;
	 private String idType;
	 private String idNo; 
	 private String mobileNo;
	 private String ipAddress;
	 private Date createdTime;
	 private String createdBy;
	 private Date updatedTime;
	 private String updatedBy; 
	 private Boolean isActive;
	 private Boolean isIslamic;
	 private Boolean isOtpRegisteredMobile;
	 private String accountType;
    
}