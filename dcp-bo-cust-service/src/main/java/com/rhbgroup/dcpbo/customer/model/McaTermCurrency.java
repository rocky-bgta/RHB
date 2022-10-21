package com.rhbgroup.dcpbo.customer.model;

import java.math.BigDecimal;

import com.rhbgroup.dcpbo.customer.contract.BoData;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class McaTermCurrency implements BoData {
	private String code;
	private BigDecimal balance;
}
