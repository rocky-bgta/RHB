package com.rhbgroup.dcpbo.customer.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AdditionalDataDescription {
	private Step1 step1;
    private Step2 step2;
    private Step3 step3;
    private String regCardLoanNo;
    private String idNo;
    private String mobileNo;
}