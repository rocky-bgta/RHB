package com.rhbgroup.dcpbo.system.extractexchangerate.service.impl.dto;

import java.util.List;

import com.rhbgroup.dcpbo.system.common.BoData;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExtractInterestExchangeRateRequest implements BoData {
	List<InterestRate> rate;
}
