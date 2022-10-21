package com.rhbgroup.dcpbo.customer.service.impl;

import com.rhbgroup.dcp.adaptor.BusinessAdaptor;
import com.rhbgroup.dcp.data.entity.loans.LoanProfile;
import com.rhbgroup.dcp.loans.model.NonHirePurchaseTransactionsDcpRequest;
import com.rhbgroup.dcp.loans.model.NonHirePurchaseTransactionsPaginationDcpRequest;
import com.rhbgroup.dcp.model.Capsule;
import com.rhbgroup.dcp.util.JsonUtil;
import com.rhbgroup.dcpbo.customer.contract.BoData;
import com.rhbgroup.dcpbo.customer.contract.ViewTransaction;
import com.rhbgroup.dcpbo.customer.dto.LoanPersonalTransactions;
import com.rhbgroup.dcpbo.customer.repository.BoLoanProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class ViewTransactionPersonalLoanServiceImpl implements ViewTransaction {

    @Autowired @Qualifier("boLoanProfileRepository")
    private BoLoanProfileRepository boLoanProfileRepository;

    @Autowired @Qualifier("personalFinanceTransactionHistoryLogic")
    private BusinessAdaptor personalFinanceTransactionHistoryLogic;

    public ViewTransactionPersonalLoanServiceImpl(BoLoanProfileRepository boLoanProfileRepository,
                                                  BusinessAdaptor personalFinanceTransactionHistoryLogic) {
        this.boLoanProfileRepository = boLoanProfileRepository;
        this.personalFinanceTransactionHistoryLogic = personalFinanceTransactionHistoryLogic;
    }

    @Override
    public BoData listing(int customerId, String accountNo, Integer pageCounter, String firstKey, String lastKey) {
//        String paddedWith0 = padWith0WhenNeeded(pageCounter);

        Capsule requestCapsule = generatePersonalFinanceTransactionHistoryRequest(accountNo,
                firstKey, lastKey, pageCounter, customerId);
        Capsule responseCapsule = personalFinanceTransactionHistoryLogic.executeBusinessLogic(requestCapsule);
        LoanPersonalTransactions loanPersonalTransactions = new LoanPersonalTransactions();
        loanPersonalTransactions.convert(responseCapsule);
        return loanPersonalTransactions;
    }

    private String padWith0WhenNeeded(String pageCounter) {
        if(pageCounter.indexOf("0") != 0) { //TODO magic number
            pageCounter = String.format("%02d", Integer.parseInt(pageCounter.trim()));
        }
        return pageCounter;
    }

    private Capsule generatePersonalFinanceTransactionHistoryRequest(String accountNo, String firstKey, String lastKey,
                                                                     Integer pageCounter, int userId) {
        Capsule capsule = new Capsule();
        capsule.setOperationSuccess(true);

        NonHirePurchaseTransactionsDcpRequest inquiry = new NonHirePurchaseTransactionsDcpRequest();
        inquiry.setAccountNo(accountNo);

        NonHirePurchaseTransactionsPaginationDcpRequest pagination = new NonHirePurchaseTransactionsPaginationDcpRequest();
        pagination.setFirstKey(firstKey);
        pagination.setLastKey(lastKey);
        //TODO discrepancy between api in inquiry service and ad requirement
        //pagination.setPageCounter(1);

        if (pageCounter != null ){
            pagination.setPageCounter(pageCounter);
        }
        inquiry.setPagination(pagination);

        capsule.updateCurrentMessage(JsonUtil.objectToJson(inquiry));
        capsule.setUserId(userId);
        return capsule;

    }
}
