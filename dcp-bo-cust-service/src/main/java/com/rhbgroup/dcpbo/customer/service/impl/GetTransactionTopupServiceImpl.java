package com.rhbgroup.dcpbo.customer.service.impl;

import com.rhbgroup.dcpbo.customer.exception.CommonException;
import com.rhbgroup.dcpbo.customer.model.*;
import com.rhbgroup.dcpbo.customer.dto.TransactionTopup;
import com.rhbgroup.dcpbo.customer.dto.TransactionTopupTopup;
import com.rhbgroup.dcpbo.customer.repository.*;
import com.rhbgroup.dcpbo.customer.service.GetTransactionTopupService;
import io.ebean.annotation.Transactional;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.modelmapper.ModelMapper;


@Service
@Transactional
public class GetTransactionTopupServiceImpl implements GetTransactionTopupService{
    @Autowired
    TopupTxnRepository topupTxnRepository;
    @Autowired
    DcpAuditTopupRepository dcpAuditTopupRepository;
    @Autowired
    AuditRepository auditRepository;
    @Autowired
    TopupBillerRepository topupBillerRepository;

    //Retrieve Topup transition details
    public TransactionTopup retrieveTransactionTopup(String refId) {
        //- 1 -get topup details from topup transaction table using refID
        TopupTxn topupTxn = topupTxnRepository.findByRefId(refId);
        if (topupTxn==null)
            throw new CommonException(CommonException.GENERIC_ERROR_CODE, "No topup transaction record found for ref id " + refId);
        Integer billerId = topupTxn.getToBillerId();
        //Setting biller name based on main function type
        TopupBiller topupBiller = topupBillerRepository.findOne(billerId);
        if (topupBiller == null)
            throw new CommonException(CommonException.GENERIC_ERROR_CODE, "No topup biller record found for biller id " + billerId);


        //- 2 -get bill topup audit id from audit topup table using refId
        AuditTopup auditTopup= dcpAuditTopupRepository.findByRefId(refId);
        if(auditTopup == null) {
            throw new CommonException("50002", "No audit topup record found for ref id " + refId);
        }

        //- 3 -get audit details
        Integer auditId = auditTopup.getAuditId();
        Audit audit = auditRepository.findOne(auditId);
        if (audit == null)
            throw new CommonException(CommonException.GENERIC_ERROR_CODE, "No audit record found for audit id " + auditId);
        String channel = audit.getChannel();
        String statusCode = audit.getStatusCode();
        String statusDescription = audit.getStatusDescription();


        //- 4 -populate topup in response object
        // process the payload and populate response object for audit field
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        TransactionTopup transactionTopup = new TransactionTopup();
        TransactionTopupTopup transactionTopupTopup = new TransactionTopupTopup();
        transactionTopupTopup = modelMapper.map(topupTxn,TransactionTopupTopup.class);
        String topupBillerName = topupBiller.getBillerName();
        transactionTopupTopup.setCustId(String.valueOf(topupTxn.getUserId()));
        transactionTopupTopup.setServiceCharge(String.valueOf(topupTxn.getTotalServiceCharge()));
        transactionTopupTopup.setBillerName(topupBillerName);
        transactionTopupTopup.setChannel(channel);
        transactionTopupTopup.setStatusCode(statusCode);
        transactionTopupTopup.setStatusDescription(statusDescription);

        //- 6 -construct response
        transactionTopup.setTopup(transactionTopupTopup);
        return transactionTopup;
    }
}
