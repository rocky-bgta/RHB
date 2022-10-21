package com.rhbgroup.dcpbo.customer.service;

import com.rhbgroup.dcpbo.customer.contract.BoData;

public interface UnitTrustDetailsService {

	public BoData getUnitTrustDetails(String accountNo, Integer customerId);

}
