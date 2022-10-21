package com.rhbgroup.dcpbo.customer.service.impl;

import com.rhbgroup.dcpbo.customer.dto.TransactionSearch;
import com.rhbgroup.dcpbo.customer.dto.TransactionSearchCustomer;
import com.rhbgroup.dcpbo.customer.dto.TransactionSearchTransaction;
import com.rhbgroup.dcpbo.customer.enums.TransactionType;
import com.rhbgroup.dcpbo.customer.exception.CommonException;
import com.rhbgroup.dcpbo.customer.model.*;
import com.rhbgroup.dcpbo.customer.repository.PaymentTxnRepository;
import com.rhbgroup.dcpbo.customer.repository.TopupTxnRepository;
import com.rhbgroup.dcpbo.customer.repository.TransferTxnRepository;
import com.rhbgroup.dcpbo.customer.repository.UserProfileRepository;
import com.rhbgroup.dcpbo.customer.service.GetTransactionSearchService;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.modelmapper.ModelMapper;

import java.util.ArrayList;
import java.util.List;


/**
 * Service class for managing users.
 */
@Service
@Transactional
public class GetTransactionSearchServiceImpl implements GetTransactionSearchService {

    @Autowired
    PaymentTxnRepository paymentTxnRepository;
    @Autowired
    TransferTxnRepository transferTxnRepository;
    @Autowired
    TopupTxnRepository topupTxnRepository;
    @Autowired
    UserProfileRepository userProfileRepository;

    //Retrieve transaction and customer details based on ref id
    public List<TransactionSearch> retrieveTransactionSearch(String refId) {

        //- 1.1 - check in payment txn table for if txn exist
        //- 1.2 - check in transfer txn table for if txn exist
        //- 1.3 - check in topup txn table for if txn exist
        Integer userId;
        String txnType;
        PaymentTxn paymentTxn;
        TransferTxn transferTxn;
        TopupTxn topupTxn;
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        TransactionSearchTransaction transactionSearchTransaction;

        paymentTxn = paymentTxnRepository.findByRefId(refId);
        if (paymentTxn != null){
            txnType = TransactionType.PAYMENT.name();
            userId = paymentTxn.getUserId();
            transactionSearchTransaction = modelMapper.map(paymentTxn,TransactionSearchTransaction.class);
        }else{
            transferTxn = transferTxnRepository.findByRefId(refId);
            if (transferTxn != null){
                txnType = TransactionType.TRANSFER.name();
                userId = transferTxn.getUserId();
                transactionSearchTransaction = modelMapper.map(transferTxn,TransactionSearchTransaction.class);
            }
            else{
                topupTxn = topupTxnRepository.findByRefId(refId);
                if (topupTxn != null) {
                    txnType = TransactionType.TOPUP.name();
                    userId = topupTxn.getUserId();
                    transactionSearchTransaction = modelMapper.map(topupTxn,TransactionSearchTransaction.class);
                }else
                    throw new CommonException(CommonException.TRANSACTION_NOT_FOUND, "No transaction found for ref id " + refId, HttpStatus.NOT_FOUND);
            }
        }
        transactionSearchTransaction.setTxnType(txnType);

        //- 2 - retrieve customer details
        UserProfile userProfile = userProfileRepository.findOne(userId);
        if(userProfile == null)
            throw new CommonException(CommonException.GENERIC_ERROR_CODE);

        //- 3 - Mapping response object
        TransactionSearchCustomer transactionSearchCustomer =modelMapper.map(userProfile,TransactionSearchCustomer.class);
        transactionSearchCustomer.setCustId(userProfile.getId());
        transactionSearchCustomer.setStatus(userProfile.getUserStatus());
        TransactionSearch transactionSearch = new TransactionSearch();

        transactionSearch.setTransaction(transactionSearchTransaction);
        transactionSearch.setCustomer(transactionSearchCustomer);
        List<TransactionSearch> transactionSearchList = new ArrayList<>();

        transactionSearchList.add(transactionSearch);
        return transactionSearchList;
    }
}
