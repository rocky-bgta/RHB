package com.rhbgroup.dcp.bo.batch.job.model;

import java.io.Serializable;
import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class InterestRate implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	String code;
	String tenure;
	BigDecimal interestRateOnMca;
}
