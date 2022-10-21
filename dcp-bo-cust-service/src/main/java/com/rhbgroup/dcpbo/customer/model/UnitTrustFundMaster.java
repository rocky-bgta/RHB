package com.rhbgroup.dcpbo.customer.model;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
@Entity
@Table(name = "VW_UT_FUND_MASTER")
public class UnitTrustFundMaster implements Serializable {

	private static final long serialVersionUID = -7078939113368085101L;

	@Id
	private int id;
	
	@Column(name = "fund_id")
	private String fundId;
	
	@Column(name = "fund_name")
	private String fundName;
	
	@Column(name = "product_category_description")
	private String productCategoryDescription;
	
	@Column(name = "risk_level_description")
	private String riskLevelDescription;
	
	@Column(name = "myr_nav_price", columnDefinition="decimal")
	private BigDecimal myrNavPrice;
	
	@Column(name = "fund_currency_nav_price", columnDefinition="decimal")
	private BigDecimal fundCurrencyNavPrice;
}
