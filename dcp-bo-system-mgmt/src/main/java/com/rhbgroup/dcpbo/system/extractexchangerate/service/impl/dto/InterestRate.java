package com.rhbgroup.dcpbo.system.extractexchangerate.service.impl.dto;

import java.io.Serializable;
import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InterestRate implements Serializable {
	String code="";
	String tenure="";
	String principalAmount="";
	String valueDate="";
}
