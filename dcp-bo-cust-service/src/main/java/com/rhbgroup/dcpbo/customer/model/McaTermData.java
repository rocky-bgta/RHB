package com.rhbgroup.dcpbo.customer.model;

import java.math.BigDecimal;

import com.rhbgroup.dcpbo.customer.contract.BoData;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class McaTermData implements BoData {
	private String accountNo;
	private String referenceNo;
	private McaTermCurrency foreignCurrency;
	private McaTermCurrency localCurrency;
	private BigDecimal visualPercentage;
	private String placementDate;
	private String maturityDate;
	private BigDecimal interestRate;
	private Integer tenure;
	private String lastRenewalDate;
	private BigDecimal accruedInterest;

}
