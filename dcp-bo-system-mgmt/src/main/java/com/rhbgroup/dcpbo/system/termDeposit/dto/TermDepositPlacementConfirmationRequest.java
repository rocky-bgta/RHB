package com.rhbgroup.dcpbo.system.termDeposit.dto;

import java.math.BigDecimal;

import com.rhbgroup.dcpbo.system.common.BoData;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TermDepositPlacementConfirmationRequest implements BoData{
	String txnStatus;
	String txnToken;
	String txnCurrency;
	String sellerExId;
	String sellerExOrderNo;
	String sellerTxnTime;
	String sellerOrderNo;
	String sellerId;
	String sellerBankCode;
	String txnAmount;
	String buyerEmail;
	String buyerName;
	String bankId;
	String productDescription;
	String fpxVersion;
}
