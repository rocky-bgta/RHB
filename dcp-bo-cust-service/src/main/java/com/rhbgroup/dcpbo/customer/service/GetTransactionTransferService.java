package com.rhbgroup.dcpbo.customer.service;

import com.rhbgroup.dcpbo.customer.dto.TransactionTransfer;

public interface GetTransactionTransferService {
    public TransactionTransfer retrieveTransactionTransfer(String refId);
}
