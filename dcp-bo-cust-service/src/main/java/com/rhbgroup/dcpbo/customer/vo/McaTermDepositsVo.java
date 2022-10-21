package com.rhbgroup.dcpbo.customer.vo;

import com.rhbgroup.dcpbo.customer.contract.BoData;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class McaTermDepositsVo implements BoData {

	private String referenceNo;
	private McaTermForeignLocalCurrency foreignCurrency;
	private McaTermForeignLocalCurrency localCurrency;

}
