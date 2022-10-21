package com.rhbgroup.dcpbo.customer.service;

import com.rhbgroup.dcpbo.customer.contract.BoData;


public interface CardDetailsService {
	public BoData getCardDetails(
			Integer customerId,
			String cardNo,
			String channelFlag,
			String connectorCode,
			String blockCode,
			String accountBlockCode);
}
