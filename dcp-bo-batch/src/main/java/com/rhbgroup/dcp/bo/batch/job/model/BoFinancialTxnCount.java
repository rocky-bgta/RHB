package com.rhbgroup.dcp.bo.batch.job.model;

import java.util.Date;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class BoFinancialTxnCount{
	 private int id;
	 private int userId;
	 private Date txnDate;
	 private String ownAcctVolume;
	 private String OwnAcctAmt;
	 private String ownAcctMortgageVolume;
	 private String OwnAcctMortgageAmt;
	 private String ownAcctCreditCardVolume;
	 private String OwnAcctCreditCardAmt;
	 private String ownAcctAsbVolume;
	 private String OwnAcctAsbAmt;
	 private String ownAcctPersonalFinVolume;
	 private String OwnAcctPersonalFinAmt;
	 private String ownAcctHpVolume;
	 private String OwnAcctHpAmt;
	 private String ownAcctPrepaidVolume;
	 private String OwnAcctPrepaidAmt;
	 private String duitNowVolume;
	 private String duitNowAmt;
	 private String duitNowQrVolume;
	 private String duitNowQrAmt;
	 private String instantTransferVolume;
	 private String instantTransferAmt;
	 private String ibgVolume;
	 private String ibgAmt;
	 private String intrabankOpenVolume;
	 private String intrabankOpenAmt;	 
	 private String intrabankFavVolume;
	 private String intrabankFavAmt;
	 private String jompayOpenVolume;
	 private String jompayOpenAmt;
	 private String jompayFavVolume;
	 private String jompayFavAmt;
	 private String rhbBillerOpenVolume;
	 private String rhbBillerOpenAmt;
	 private String rhbBillerFavVolume;
	 private String rhbBillerFavAmt;
	 private String prepaidTopupVolume;
	 private String prepaidTopupAmt;
	 private String prepaidTopupFavVolume;
	 private String prepaidTopupFavAmt;
	 private String asnbVolume;
	 private String asnbAmt;
	 private String mcaBuyCurrencyVolume;
	 private String mcaBuyCurrencyAmt;
	 private String mcaSellCurrencyVolume;
	 private String mcaSellCurrencyAmt;
	 private String mcaBuyMetalVolume;
	 private String mcaBuyMetalAmt;
	 private String mcaSellMetalVolume;
	 private String mcaSellMetalAmt;
	 private String mcaTdPlacementVolume;
	 private String mcaTdPlacementAmt; 
	 private String mcaTdWithdrawVolume;
	 private String mcaTdWithdrawAmt; 
	 private String tdPlacementVolume;
	 private String tdPlacementAmt; 
	 private String tdWithdrawVolume;
	 private String tdWithdrawAmt;
	 private String inwardFpxVolume;
	 private String inwardFpxAmt;
	 private String outwardFpxVolume;
	 private String outwardFpxAmt;
	 private Date createdTime;
}