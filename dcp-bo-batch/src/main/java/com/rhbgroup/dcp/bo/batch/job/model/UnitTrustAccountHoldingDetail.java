package com.rhbgroup.dcp.bo.batch.job.model;


import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@EqualsAndHashCode
@ToString
public class UnitTrustAccountHoldingDetail extends UnitTrustFileAbs {
	private String acctNo;
	private String fundId;
	private String holdingUnit;
	private String fundCurrMarketVal;
	private String fundCurrUnrealisedGainLoss;
	private String fundCurrUnrealisedGainLossPercent;
	private String fundCurrInvestAmnt;
	private String fundCurrAvgUnitPrice;
	private String fundMyrMarketVal;
	private String fundMyrUnrealisedGainLoss;
	private String fundMyrUnrealisedGainLossPercent;
	private String fundMyrInvestAmnt;
	private String fundMyrAvgUnitPrice;
}
