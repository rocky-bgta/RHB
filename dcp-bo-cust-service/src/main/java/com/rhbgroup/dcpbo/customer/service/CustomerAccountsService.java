package com.rhbgroup.dcpbo.customer.service;

import com.rhbgroup.dcpbo.customer.contract.BoData;

public interface CustomerAccountsService {
	public BoData getCustomerAccounts(Integer customerId);
}