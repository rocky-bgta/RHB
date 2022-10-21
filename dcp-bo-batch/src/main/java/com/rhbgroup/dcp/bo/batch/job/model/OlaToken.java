package com.rhbgroup.dcp.bo.batch.job.model;

import java.sql.Timestamp;
import java.sql.Date;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class OlaToken{
	
	private Integer id;
    private String token;
    private String deviceId;
    private String name;
    private String productType;
    private String productCode;
    private String mobileNo;
    private String email;
    private String idType;
    private String idNo;
    private Date dateOfBirth;
    private String nationality;
    private String secretPhrase;
    private String keyedPassword1;
    private String keyedPassword2;
    private String gender;
    private String race;
	private String maritalStatus;
	private String prStatus;
	private String prCountry;
	private String prIdNo;
	private Integer citizenship;
	private String addressLine1;
	private String addressLine2;
	private String addressLine3;
	private String city;
	private String state;
	private String postcode;
	private String country;
	private String purposeOfAcctOpening;
	private String employerInstitutionalRefId;
	private Integer sourceOfWealth;
	private String sourceOfFund;
	private Integer employmentStatus;
	private String occupation;
	private String sector;
	private String natureOfBusiness;
	private String companyName;
	private String workAddressLine1;
	private String workAddressLine2;
	private String workAddressLine3;
	private String workCity;
	private String workState;
	private String workPostCode;
	private String workCountry;
	private Integer monthlyIncome;
	private String accountState;
	private String accountBranch;
	private Boolean isMsaHomeDeliveryAllowed;
	private String isMsaOfficeDeliveryAllowed;
	private String activationOption;
	private String msaActivationMode;
	private Boolean isEkycUser;
	private Boolean isActive;
	private String accountNo;
	private String status;
	private String assessmentRiskLevel;
	private String assessmentRiskScore;
	private String auditAdditionalData;
	private Boolean isTncAccepted;
	private Boolean isConsentRhbGroupAccepted;
	private Boolean isEtbCustomer;
	private String addressType;
	private String divisionCode;
	private String debitCardNo;
	private Timestamp createdTime;
	private String createdBy;
	private Timestamp updatedTime;
	private String updatedBy;
	private String secretPhase;
	private Boolean isEtbCasaUser;
	private String txnStatusCode;
	private String txnStatus;
	private String refId;
	private String channel;
	private Integer segmentCode;
	private String amlScreeningResult;
	private String apiStatusCode;
	private Boolean isIslamic; 
	private String apiStatusDesc;
	private String username;
	private String ipAddress;
}