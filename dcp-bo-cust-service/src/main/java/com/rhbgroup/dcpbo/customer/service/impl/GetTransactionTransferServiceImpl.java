package com.rhbgroup.dcpbo.customer.service.impl;

import com.rhbgroup.dcpbo.customer.enums.FundTransferMainFunctionType;
import com.rhbgroup.dcpbo.customer.exception.CommonException;
import com.rhbgroup.dcpbo.customer.model.*;
import com.rhbgroup.dcpbo.customer.dto.TransactionTransfer;
import com.rhbgroup.dcpbo.customer.dto.TransactionTransferTransfer;
import com.rhbgroup.dcpbo.customer.repository.*;
import com.rhbgroup.dcpbo.customer.service.GetTransactionTransferService;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


/**
 * Service class for managing users.
 */
@Service
@Transactional
public class GetTransactionTransferServiceImpl implements GetTransactionTransferService {

    @Autowired
    TransferTxnRepository transferTxnRepository;
    @Autowired
    DcpAuditFundTransferRepository dcpAuditFundTransferRepository;
    @Autowired
    AuditRepository auditRepository;
    @Autowired
    DcpAuditEventConfigRepository dcpAuditEventConfigRepository;
    @Autowired
    BankRepository bankRepository;


    //Retrieve Transfer transaction details to Activity view
    public TransactionTransfer retrieveTransactionTransfer(String refId)  {
        //- 1 -get transfer details from transfer transaction table using refID
        TransferTxn transferTxn = transferTxnRepository.findByRefId(refId);
        if (transferTxn == null)
            throw new CommonException(CommonException.GENERIC_ERROR_CODE,"No transfer transaction found for refId " + refId);



        //- 2 -get bill transfer audit id from audit transfer table using refId
        AuditFundTransfer auditFundTransfer= dcpAuditFundTransferRepository.findAuditFundTransferByRefId(refId);
        if(auditFundTransfer==null) {
            throw new CommonException("50002","No audit fund transfer found for refId " + refId);
        }

        //- 3 -get event code, event name, channel, status_code, and status description
        Integer auditId = auditFundTransfer.getAuditId();
        Audit audit = auditRepository.findOne(auditId);
        if (audit == null)
            throw new CommonException(CommonException.GENERIC_ERROR_CODE,"No audit found for audit id " + auditId);
        String eventCode = audit.getEventCode();
        String channel = audit.getChannel();
        String statusCode = audit.getStatusCode();
        String statusDescription = audit.getStatusDescription();

        AuditEventConfig auditEventConfig = dcpAuditEventConfigRepository.findByEventCode(eventCode);
        if (auditEventConfig == null)
            throw new CommonException(CommonException.GENERIC_ERROR_CODE, "No audit event config found for event code of " + eventCode);

        String eventName = auditEventConfig.getEventName();

        //- 4 -populate transfer in response object
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        TransactionTransfer transactionTransfer = new TransactionTransfer();
        TransactionTransferTransfer transactionTransferTransfer;
        transactionTransferTransfer = modelMapper.map(transferTxn,TransactionTransferTransfer.class);
        transactionTransferTransfer.setEventName(eventName);
        transactionTransferTransfer.setOtherDetails(transferTxn.getOtherPaymentDetails());
        transactionTransferTransfer.setChannel(channel);
        transactionTransferTransfer.setStatusCode(statusCode);
        transactionTransferTransfer.setStatusDescription(statusDescription);

        //set duitnow country code
        String mainFunction = transactionTransferTransfer.getMainFunction();
        if (mainFunction.equals(FundTransferMainFunctionType.DUITNOW.name())){
            transactionTransferTransfer.setDuitnowCountryCode(transferTxn.getDuitnowCountryCode());
        }

        //set bank initials
        if (mainFunction.equals(FundTransferMainFunctionType.IBG.name())
                || mainFunction.equals(FundTransferMainFunctionType.INSTANT.name())
                || mainFunction.equals(FundTransferMainFunctionType.IBFT.name())
                || mainFunction.equals(FundTransferMainFunctionType.OTHER_RHB.name()) ||
                mainFunction.equals(FundTransferMainFunctionType.OWN.name())){
            Integer bankId = transferTxn.getToBankId();
            Bank bank = bankRepository.getOne(bankId);
            if (bank == null)
                throw new CommonException(CommonException.GENERIC_ERROR_CODE, "No bank found for bank id " + bankId );
            String bankInitials = bank.getBankInitials();
            transactionTransferTransfer.setBank(bankInitials);
        }

        //set subfunction & resident status
        if (mainFunction.equals(FundTransferMainFunctionType.IBG.name())
                || mainFunction.equals(FundTransferMainFunctionType.INSTANT.name())||
                mainFunction.equals(FundTransferMainFunctionType.IBFT.name())){
            transactionTransferTransfer.setSubFunction(transferTxn.getSubFunction());
            transactionTransferTransfer.setResidentStatus(String.valueOf(transferTxn.getToResidentStatus()  ));
        }

        //set id type
        if (mainFunction.equals(FundTransferMainFunctionType.IBG.name())
                || mainFunction.equals(FundTransferMainFunctionType.DUITNOW.name())){
            transactionTransferTransfer.setIdType(transferTxn.getToIdType());
        }

        //set id no
        if (mainFunction.equals(FundTransferMainFunctionType.IBG.name()) ){
            transactionTransferTransfer.setIdNo(transferTxn.getToIdNo());
        }

        transactionTransfer.setTransfer(transactionTransferTransfer);
        return transactionTransfer;
    }
}
