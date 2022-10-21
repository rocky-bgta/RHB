package com.rhbgroup.dcpbo.customer.service;

import com.rhbgroup.dcp.adaptor.BusinessAdaptor;
import com.rhbgroup.dcp.data.entity.loans.LoanProduct;
import com.rhbgroup.dcp.data.entity.loans.LoanProfile;
import com.rhbgroup.dcp.data.entity.profiles.UserProfile;
import com.rhbgroup.dcp.data.repository.ProfileRepository;
import com.rhbgroup.dcp.loans.model.NonHirePurchaseTransactionsDcpRequest;
import com.rhbgroup.dcp.loans.model.NonHirePurchaseTransactionsPaginationDcpRequest;
import com.rhbgroup.dcp.model.Capsule;
import com.rhbgroup.dcp.model.Constants;
import com.rhbgroup.dcp.model.SourceChannel;
import com.rhbgroup.dcp.util.JsonUtil;
import com.rhbgroup.dcpbo.customer.contract.BoData;
import com.rhbgroup.dcpbo.customer.contract.ViewTransaction;
import com.rhbgroup.dcpbo.customer.controller.LoanController;
import com.rhbgroup.dcpbo.customer.dto.AsbTransactions;
import com.rhbgroup.dcpbo.customer.repository.BoLoanProfileRepository;
import com.rhbgroup.dcpbo.customer.service.impl.CustomerAccountsServiceImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.UUID;

/**
 * DCPBL-9810
 */
public class ViewTransactionAsb implements ViewTransaction {

    private Logger logger = LogManager.getLogger(ViewTransactionAsb.class);

    @Autowired
    @Qualifier(value = "profileRepository")
    private ProfileRepository profileRepository;

    @Autowired
    @Qualifier("boLoanProfileRepository")
    private BoLoanProfileRepository boLoanProfileRepository;

    @Autowired
    @Qualifier("asbLoanTransactionHistoryLogic")
    private BusinessAdaptor asbLoanTransactionHistoryLogic;

    private AsbTransactions asbTransactions;

    @Override
    public BoData listing(int customerId, String accountNo, Integer pageCounter, String firstKey, String lastKey) {
//        String paddedWith0 = padWith0WhenNeeded(pageCounter);
        asbTransactions = new AsbTransactions();

        //TODO customerId equal to userProfile id, why we need to query at the 1st place?
        UserProfile userProfile = profileRepository.getUserProfileByUserId(customerId);

        //TODO loanProduct not use anywhere but in spec it say otherwise
        //LoanProduct loanProduct = boLoanProfileRepository.findByLoanId(loanProfile.getLoanProductId());
        Capsule requestCapsule = generateAsbLoanTransactionHistoryRequest(accountNo, firstKey, lastKey,
                pageCounter, userProfile);

        logger.debug("listing requestCapsule {} ",JsonUtil.objectToJson(requestCapsule));

        Capsule responseCapsule = asbLoanTransactionHistoryLogic.executeBusinessLogic(requestCapsule);

        logger.debug("listing responseCapsule {} ",JsonUtil.objectToJson(responseCapsule));

        this.asbTransactions.convert(responseCapsule);

        return asbTransactions;
    }

    public String padWith0WhenNeeded(String pageCounter) {
        if(pageCounter.indexOf("0") != 0) { //TODO magic number
            pageCounter = String.format("%02d", Integer.parseInt(pageCounter.trim()));
        }
        return pageCounter;
    }

    public Capsule generateAsbLoanTransactionHistoryRequest(String accountNo, String firstKey, String lastKey,
                                                            Integer pageCounter, UserProfile userProfile) {
        Capsule capsule = new Capsule();
        capsule.setOperationSuccess(true);

        NonHirePurchaseTransactionsDcpRequest inquiry = new NonHirePurchaseTransactionsDcpRequest();
        NonHirePurchaseTransactionsPaginationDcpRequest pagination = new NonHirePurchaseTransactionsPaginationDcpRequest();
        inquiry.setAccountNo(accountNo);

        pagination.setFirstKey(firstKey);
        pagination.setLastKey(lastKey);
        //TODO discrepancy between api in inquiry service and ad requirement
        //pagination.setPageCounter(paddedPageCounter);
        if (pageCounter != null ){
            pagination.setPageCounter(pageCounter);
        }
        inquiry.setPagination(pagination);

        capsule.updateCurrentMessage(JsonUtil.objectToJson(inquiry));
        capsule.setUserId(userProfile.getId());
        capsule.setCisNumber(userProfile.getCisNo());
        capsule.setProperty(Constants.SOURCE_CHANNEL, SourceChannel.DCP_MBK);
        capsule.setMessageId(UUID.randomUUID().toString());
        capsule.setProperty(Constants.OPERATION_NAME, asbLoanTransactionHistoryLogic.getClass().getSimpleName());
        return capsule;

    }
}
