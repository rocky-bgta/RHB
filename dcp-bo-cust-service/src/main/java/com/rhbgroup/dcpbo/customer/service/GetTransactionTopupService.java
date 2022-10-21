package com.rhbgroup.dcpbo.customer.service;

import com.rhbgroup.dcpbo.customer.dto.TransactionTopup;

public interface GetTransactionTopupService {
    public TransactionTopup retrieveTransactionTopup(String refId);
}
