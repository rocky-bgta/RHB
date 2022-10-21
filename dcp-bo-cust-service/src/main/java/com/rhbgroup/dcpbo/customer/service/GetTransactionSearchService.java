package com.rhbgroup.dcpbo.customer.service;

import com.rhbgroup.dcpbo.customer.dto.TransactionSearch;

import java.util.List;

public interface GetTransactionSearchService {
    public List<TransactionSearch> retrieveTransactionSearch(String refId);
}
