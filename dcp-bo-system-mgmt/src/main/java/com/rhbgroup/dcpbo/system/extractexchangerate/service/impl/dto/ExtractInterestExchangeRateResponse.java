package com.rhbgroup.dcpbo.system.extractexchangerate.service.impl.dto;

import com.rhbgroup.dcpbo.system.common.BoData;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExtractInterestExchangeRateResponse implements BoData {
	InterestRateResponse[] rate;
}
