package com.rhbgroup.dcpbo.customer.vo;

import java.util.List;

import com.rhbgroup.dcpbo.customer.contract.BoData;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetMcaTermLogicRequestVo implements BoData {

	private String accountNo;
	private List<String> currencyCode;
}
