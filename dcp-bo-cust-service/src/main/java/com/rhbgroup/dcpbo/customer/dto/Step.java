package com.rhbgroup.dcpbo.customer.dto;

import com.rhbgroup.dcpbo.customer.contract.BoData;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Step implements BoData {
	
	private Data step1;
	private Data step1b;
	private Data step2;
	private Data step3;
	private String cardNo;
	private String idNo;
	private String cisMobileNo;
	private String regCardLoanNo;
	private String idType;
	private String mobileNo;
	private String email;
	private String isPremier;
	private String isSanctioned;
	private String nationalityDesc;
	private String termsAndConditionsAccepted;
	private String termsAndConditionsAcceptedDate;
	private String tnccategory;
	private String pdpaversion;
	private String tncversion;
	private String crossSellingAcceptedDate;
	private String crossSellingRHBGroupAccepted;
	private String crossSellingRHBPartnersAccepted;
	private String otpMobileNo;
	private String otpRegistrationDate;
}
