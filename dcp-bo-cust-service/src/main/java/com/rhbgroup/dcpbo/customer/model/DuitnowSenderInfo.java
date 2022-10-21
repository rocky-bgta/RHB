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
public class DuitnowSenderInfo implements BoData {
	private String otpMobileNumber;
	private String idType;
	private String idNumber;
	private String idRegStatus;
	private String nadEnquiryStatus;
}
