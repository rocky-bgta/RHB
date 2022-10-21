package com.rhbgroup.dcpbo.customer.vo;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomerTrxLimitVo {

	private String txnType;
	private List<MainFunctionLimitsVo> mainFunctionLimits;

}
