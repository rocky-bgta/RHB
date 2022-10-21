package com.rhbgroup.dcpbo.customer.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "TBL_REGISTRATION_TOKEN")
public class RegistrationToken implements Serializable{
	
	 @Id
	 @GeneratedValue(strategy = GenerationType.IDENTITY)
	 @Column(name = "id", nullable = false, unique = true)
	 private Integer id;

	 @Column(name = "TOKEN", nullable = false)
	 private String token;
	 
	 @Column(name = "DEVICE_ID", nullable = false)
	 private String deviceId;
	
	 @Column(name = "USERNAME", nullable = true)
	 private String username;
	 
	 @Column(name = "EMAIL", nullable = true)
	 private String email;
	 
	 @Column(name = "CIS_NO", nullable = false)
	 private String cisNo;
	 
	 @Column(name = "ACCOUNT_NUMBER", nullable = true)
	 private String accountNumber;
	 
	 @Column(name = "IS_OTP_VERIFIED", nullable = false)
	 private Boolean iSOtpVerified;
	 
	 @Column(name = "CHANNEL", nullable = false)
	 private String channel;
	 
	 @Column(name = "AUDIT_ADDITIONALDATA", nullable = true)
	 private String auditAdditionalData;
	 
	 @Column(name = "IS_STAFF", nullable = true)
	 private Boolean isStaff;
	 
	 @Column(name = "USER_TYPE", nullable = false)
	 private String userType;
	 
	 @Column(name = "CHALLENGE_QUESTION", nullable = true)
	 private String challengeQuestion;
	 
	 @Column(name = "CHALLENGE_ANSWER", nullable = true)
	 private String challengeAnswer;
	 
	 @Column(name = "IS_PREMIER", nullable = true)
	 private Boolean isPremier;
	 
	 @Column(name = "NAME", nullable = false)
	 private String name;
	 
	 @Column(name = "ID_TYPE", nullable = false)
	 private String idType;
	 
	 @Column(name = "ID_NO", nullable = false)
	 private String idNo;
	 
	 @Column(name = "MOBILE_NO", nullable = true)
	 private String mobileNo;
	 
	 @Column(name = "IP_ADDRESS", nullable = false)
	 private String ipAddress;
	 
	 @Column(name = "CREATED_TIME", nullable = false)
	 private Date createdTime;
	 
	 @Column(name = "CREATED_BY", nullable = false)
	 private String createdBy;
	 
	 @Column(name = "UPDATED_TIME", nullable = true)
	 private Date updatedTime;
	 
	 @Column(name = "UPDATED_BY", nullable = true)
	 private String updatedBy;
	 
	 @Column(name = "IS_ACTIVE", nullable = false)
	 private Boolean isActive;
	 
	 @Column(name = "IS_ISLAMIC", nullable = false)
	 private Boolean isIslamic;
	 
	 @Column(name = "IS_OTP_REGISTERED_MOBILE", nullable = false)
	 private Boolean isOtpRegisteredMobile;
	 
	 @Column(name = "ACCOUNT_TYPE", nullable = false)
	 private String accountType;

}
