package com.rhbgroup.dcpbo.customer.vo;

import java.math.BigDecimal;
import java.util.List;

import com.rhbgroup.dcpbo.customer.contract.BoData;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class McaTermsVo implements BoData {

	private String accountNo;
	private BigDecimal totalBalance;
	private List<McaTermDepositsVo> termDeposits;
	
}
