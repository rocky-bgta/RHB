package com.rhbgroup.dcpbo.customer.service;

import com.rhbgroup.dcpbo.customer.contract.BoData;

public interface CardTransactionsService {
	public BoData getCardTransactions (Integer customerId, String accountNo, String firstKey, String lastKey, String pageCounter);
}