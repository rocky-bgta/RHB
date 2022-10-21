package com.rhbgroup.dcp.bo.batch.job.model;


import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@EqualsAndHashCode
@ToString
public class UnitTrustAccountHolding extends UnitTrust {
	private String acctNo;
	private String fundId;
	private double holdingUnit;
	private double fundCurrMarketVal;
	private double fundCurrUnrealisedGainLoss;
	private double fundCurrUnrealisedGainLossPercent;
	private double fundCurrInvestAmnt;
	private double fundCurrAvgUnitPrice;
	private double fundMyrMarketVal;
	private double fundMyrUnrealisedGainLoss;
	private double fundMyrUnrealisedGainLossPercent;
	private double fundMyrInvestAmnt;
	private double fundMyrAvgUnitPrice;
}
