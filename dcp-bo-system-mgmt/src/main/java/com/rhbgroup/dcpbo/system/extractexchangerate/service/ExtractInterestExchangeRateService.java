package com.rhbgroup.dcpbo.system.extractexchangerate.service;

import com.rhbgroup.dcpbo.system.common.BoData;
import com.rhbgroup.dcpbo.system.extractexchangerate.service.impl.dto.ExtractInterestExchangeRateRequest;

public interface ExtractInterestExchangeRateService {
	public BoData getInterestExchangeRate(ExtractInterestExchangeRateRequest request);
}
