package com.rhbgroup.dcpbo.customer.dto;

import java.math.BigDecimal;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.rhbgroup.dcp.asnb.model.DcpAsnbDashboardFundDetails;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AsnbAccounts {
	private boolean isMinor;
	private String membershipNumber;
	private BigDecimal grandtotalHoldings;
	private String identificationNumber;
	private String guardianIdNumber;
	private String memberIdentificationNumber;
	private String memberIdType;
	private String accountHolderName;
	private String nickName;
	private List <DcpAsnbDashboardFundDetails> funds;

}
