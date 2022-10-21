package com.rhbgroup.dcpbo.customer.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.rhbgroup.dcp.creditcards.model.CreditCardDcpResponse;
import com.rhbgroup.dcp.deposits.casa.model.AccountsSummary;
import com.rhbgroup.dcp.deposits.casa.model.TermDepositsDcpResponse;
import com.rhbgroup.dcp.deposits.mca.model.bizlogic.response.McaAccountsDcpResponse;
import com.rhbgroup.dcp.deposits.mca.model.bizlogic.response.McaTermAccounts;
import com.rhbgroup.dcp.investments.model.DcpGetUnitTrustResponse;
import com.rhbgroup.dcp.loans.model.LoansDcpResponse;
import com.rhbgroup.dcp.profiles.model.DcpGetAccountsProfileResponse;
import com.rhbgroup.dcpbo.customer.contract.BoData;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Setter
@Getter
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CustomerAccounts implements BoData {
	DcpGetAccountsProfileResponse accounts;
	AccountsSummary casa;
	CreditCardDcpResponse cards;
	TermDepositsDcpResponse termDeposits;
	LoansDcpResponse loans;
	List<McaTermAccounts> mca;
	DcpGetUnitTrustResponse unitTrust;
	List <AsnbAccounts> asnb;
}
