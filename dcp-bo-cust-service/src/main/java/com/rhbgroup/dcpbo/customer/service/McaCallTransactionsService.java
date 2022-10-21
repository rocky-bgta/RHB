package com.rhbgroup.dcpbo.customer.service;

import com.rhbgroup.dcpbo.customer.contract.BoData;

public interface McaCallTransactionsService {

	BoData getMcaCallTransactions(Integer customerId, String accountNo, String foreignCurrency, Integer pageCounter,
			String firstKey, String lastKey);

}
