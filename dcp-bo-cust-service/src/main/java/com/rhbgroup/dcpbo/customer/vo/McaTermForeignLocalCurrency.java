package com.rhbgroup.dcpbo.customer.vo;

import java.math.BigDecimal;

import com.rhbgroup.dcpbo.customer.contract.BoData;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class McaTermForeignLocalCurrency implements BoData {

	private String code;
	private BigDecimal balance;
	private String description;
}
