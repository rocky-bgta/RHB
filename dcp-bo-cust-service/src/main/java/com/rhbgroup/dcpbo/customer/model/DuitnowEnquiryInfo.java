package com.rhbgroup.dcpbo.customer.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.rhbgroup.dcpbo.customer.contract.BoData;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DuitnowEnquiryInfo implements BoData {
	private String registrationId;
	private String displayName;
	private String idType;
	private String idVal;
	private String countryCode;
	private String countryName;
	private String status;
	private String bic;
	private String bankName;
	private String bankAccount;
	private String bankAcctType;
	private String bankAcctName;
	private Boolean otpCheckSumFlag;
}
