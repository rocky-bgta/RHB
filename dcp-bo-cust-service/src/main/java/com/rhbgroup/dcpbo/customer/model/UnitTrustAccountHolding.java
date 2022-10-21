package com.rhbgroup.dcpbo.customer.model;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
@Entity
@Table(name = "VW_UT_ACCOUNT_HOLDING")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class UnitTrustAccountHolding implements Serializable {
	private static final long serialVersionUID = -6364270271777579002L;

	@Id
	@JsonIgnore
	private int id;

	@Column(name = "account_no")
	private String accountNo;
	
	@Column(name = "fund_id")
	private String fundId;
	
	@Column(name = "holding_unit", columnDefinition="decimal")
    @JsonSerialize(using = MoneyValueSerializer.class)
	private BigDecimal fundHoldingUnit;
	
	@JsonIgnore
	@Column(name = "fund_myr_market_value", columnDefinition="decimal")
	private BigDecimal fundMyrMarketValue;
	
	@JsonIgnore
	@Column(name = "fund_myr_unrealised_gain_loss", columnDefinition="decimal")
	private BigDecimal fundMyrUnrealisedGainLoss;
	
	@JsonIgnore
	@Column(name = "fund_myr_unrealised_gain_loss_percentage", columnDefinition="decimal")
	private BigDecimal fundMyrUnrealisedGainLossPercentage;
	
	@JsonIgnore
	@Column(name = "fund_myr_investment_amount", columnDefinition="decimal")
	private BigDecimal fundMyrInvestmentAmount;
	
	@JsonIgnore
	@Column(name = "fund_currency_market_value", columnDefinition="decimal")
	private BigDecimal fundCurrencyMarketValue;
	
	@JsonIgnore
	@Column(name = "fund_currency_unrealised_gain_loss", columnDefinition="decimal")
	private BigDecimal fundCurrencyUnrealisedGainLoss;
	
	@JsonIgnore
	@Column(name = "fund_currency_unrealised_gain_loss_percentage", columnDefinition="decimal")
	private BigDecimal fundCurrencyUnrealisedGainLossPercentage;
	
	@JsonIgnore
	@Column(name = "fund_currency_investment_amount", columnDefinition="decimal")
	private BigDecimal fundCurrencyInvestmentAmount;
	
	@JsonIgnore
	@Column(name = "fund_currency_average_unit_price", columnDefinition="decimal")
	private BigDecimal fundCurrencyAverageUnitPrice;
	
	@Transient
	private String fundName;
	
	@Transient
	private String fundProductCategoryDescription;
	
	@Transient
	private String fundRiskLevelDescription;
	
	@Transient
	@JsonIgnore
	private BigDecimal fundMyrNavPrice;
	
	@Transient
	@JsonIgnore
	private BigDecimal fundCurrencyNavPrice;
	
	@Transient
	private Myr myr;
	
	@Transient
	private Currency currency;
	
	@Setter
	@Getter
	@ToString
	static class Myr {
		@JsonSerialize(using = MoneyValueSerializer.class)
		private BigDecimal myrNavPrice;

		@JsonSerialize(using = MoneyValueSerializer.class)
        private BigDecimal myrMarketValue;

		@JsonSerialize(using = MoneyValueSerializer.class)
        private BigDecimal myrUnrealisedGainLoss;

		@JsonSerialize(using = MoneyValueSerializer.class)
        private BigDecimal myrUnrealisedGainLossPercentage;
	}

	@Setter
	@Getter
	@ToString
	static class Currency {

		@JsonSerialize(using = MoneyValueSerializer.class)
		private BigDecimal currencyNavPrice;

		@JsonSerialize(using = MoneyValueSerializer.class)
        private BigDecimal currencyMarketValue;

		@JsonSerialize(using = MoneyValueSerializer.class)
        private BigDecimal currencyUnrealisedGainLoss;

		@JsonSerialize(using = MoneyValueSerializer.class)
        private BigDecimal currencyUnrealisedGainLossPercentage;

		@JsonSerialize(using = MoneyValueSerializer.class)
        private BigDecimal currencyInvestmentAmount;

		@JsonSerialize(using = MoneyValueSerializer.class)
        private BigDecimal currencyAverageUnitPrice;
	}
	
	public void setTransientFieldValues() {
		if (myr == null)
			myr = new Myr();
		myr.myrNavPrice = fundMyrNavPrice;
		myr.myrMarketValue = fundMyrMarketValue;
		myr.myrUnrealisedGainLoss = fundMyrUnrealisedGainLoss;
		myr.myrUnrealisedGainLossPercentage = fundMyrUnrealisedGainLossPercentage;

		if (currency == null)
			currency = new Currency();
		currency.currencyNavPrice = fundCurrencyNavPrice;
		currency.currencyMarketValue = fundCurrencyMarketValue;
		currency.currencyUnrealisedGainLoss = fundCurrencyUnrealisedGainLoss;
		currency.currencyUnrealisedGainLossPercentage = fundCurrencyUnrealisedGainLossPercentage;
		currency.currencyInvestmentAmount = fundCurrencyInvestmentAmount;
		currency.currencyAverageUnitPrice = fundCurrencyAverageUnitPrice;
	}
	
}
