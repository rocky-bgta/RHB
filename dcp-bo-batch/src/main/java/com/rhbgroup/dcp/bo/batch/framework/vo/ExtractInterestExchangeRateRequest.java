package com.rhbgroup.dcp.bo.batch.framework.vo;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ExtractInterestExchangeRateRequest  {
	List<InterestRate> rate;
}
