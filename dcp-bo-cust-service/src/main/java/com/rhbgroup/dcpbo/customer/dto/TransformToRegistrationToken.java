package com.rhbgroup.dcpbo.customer.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class TransformToRegistrationToken {

	    private String accountNumber;
	    private String name;
	    private String mobileNo;
	    private String cisNo;
	    private String idType;
	    private String idNo;
	    private Boolean isPremier;
}
