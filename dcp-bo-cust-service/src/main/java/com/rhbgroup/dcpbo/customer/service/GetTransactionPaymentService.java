package com.rhbgroup.dcpbo.customer.service;

import com.rhbgroup.dcpbo.customer.dto.TransactionPayment;

public interface GetTransactionPaymentService {
    public TransactionPayment retrieveTransactionPayment(String refId);
}
