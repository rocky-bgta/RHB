package com.rhbgroup.dcpbo.customer.service.impl;

import com.rhbgroup.dcp.adaptor.BusinessAdaptor;
import com.rhbgroup.dcp.loans.model.HirePurchaseTransactionsDcpRequest;
import com.rhbgroup.dcp.loans.model.HirePurchaseTransactionsPaginationDcpRequest;
import com.rhbgroup.dcp.model.Capsule;
import com.rhbgroup.dcp.util.JsonUtil;
import com.rhbgroup.dcpbo.customer.contract.BoData;
import com.rhbgroup.dcpbo.customer.contract.ViewTransaction;
import com.rhbgroup.dcpbo.customer.dto.HirePurchaseTransactions;
import com.rhbgroup.dcpbo.customer.repository.BoLoanProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class ViewHirePurchaseTransactionsLoanServiceImpl implements ViewTransaction {

    @Autowired @Qualifier("boLoanProfileRepository")
    private BoLoanProfileRepository boLoanProfileRepository;

    @Autowired @Qualifier("hirePurchaseTransactionsLogic")
    private BusinessAdaptor hirePurchaseTransactionsLogic;

    public ViewHirePurchaseTransactionsLoanServiceImpl(BoLoanProfileRepository boLoanProfileRepository,
                                                       BusinessAdaptor hirePurchaseTransactionsLogic) {
        this.boLoanProfileRepository = boLoanProfileRepository;
        this.hirePurchaseTransactionsLogic = hirePurchaseTransactionsLogic;
    }

    @Override
    public BoData listing(int customerId, String accountNo, Integer pageCounter, String firstKey, String lastKey) {

        Capsule requestCapsule = generateHirePurchaseTransactionHistoryRequest(accountNo,
                firstKey, lastKey, pageCounter, customerId);
        Capsule responseCapsule = hirePurchaseTransactionsLogic.executeBusinessLogic(requestCapsule);
        HirePurchaseTransactions hirePurchaseTransactions = new HirePurchaseTransactions();
        hirePurchaseTransactions.convert(responseCapsule);
        return hirePurchaseTransactions;
    }

    private Capsule generateHirePurchaseTransactionHistoryRequest(String accountNo, String firstKey,
                                                                  String lastKey, Integer pageCounter, Integer userId) {
        Capsule capsule = new Capsule();
        HirePurchaseTransactionsDcpRequest inquiry = new HirePurchaseTransactionsDcpRequest();
        inquiry.setAccountNo(accountNo);

        HirePurchaseTransactionsPaginationDcpRequest pagination = new HirePurchaseTransactionsPaginationDcpRequest();
        pagination.setFirstKey(firstKey);
        pagination.setLastKey(lastKey);
        //TODO all loan request would need to change page counter since AD have confirmed this is int
        //pagination.setPageCounter(s);
        if (pageCounter != null ){
            pagination.setPageCounter(pageCounter);
        }
        inquiry.setPagination(pagination);

        capsule.updateCurrentMessage(JsonUtil.objectToJson(inquiry));
        capsule.setUserId(userId);
        return capsule;
    }
}
