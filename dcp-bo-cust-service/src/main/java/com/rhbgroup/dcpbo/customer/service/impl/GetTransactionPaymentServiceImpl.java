package com.rhbgroup.dcpbo.customer.service.impl;

import com.rhbgroup.dcpbo.customer.exception.CommonException;
import com.rhbgroup.dcpbo.customer.model.*;
import com.rhbgroup.dcpbo.customer.dto.TransactionPaymentPayment;
import com.rhbgroup.dcpbo.customer.dto.TransactionPayment;
import com.rhbgroup.dcpbo.customer.repository.*;
import com.rhbgroup.dcpbo.customer.service.GetTransactionPaymentService;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service class for retrieving payment transactions
 */
@Service
@Transactional
public class GetTransactionPaymentServiceImpl implements GetTransactionPaymentService {

    @Autowired
    PaymentTxnRepository paymentTxnRepository;
    @Autowired
    DcpAuditBillPaymentRepository dcpAuditBillPaymentRepository;
    @Autowired
    AuditRepository auditRepository;
    @Autowired
    BillerRepository billerRepository;

    private static final String OTHER_BILLER = "OTHER_BILLER";

    //Retrieve transaction of type payment with ref id
    public TransactionPayment retrieveTransactionPayment(String refId) {
        //- 1 -get payment details from transaction table using refID
        PaymentTxn paymentTxn = paymentTxnRepository.findByRefId(refId);
        if (paymentTxn==null)
            throw new CommonException(CommonException.GENERIC_ERROR_CODE,"Payment not found for ref Id " + refId);
        Integer billerId = paymentTxn.getToBillerId();


        //- 2 -get bill payment audit id from audit payment table using refId
        // get audit details using auditId
        Integer auditId = dcpAuditBillPaymentRepository.findAuditIdByRefId(refId);
        if (auditId == null)
            throw new CommonException("50002","No audit record found for ref id " + auditId);
        Audit audit = auditRepository.findOne(auditId);
        if (audit == null)
            throw new CommonException(CommonException.GENERIC_ERROR_CODE,"No record found for audit id " + auditId);
        String channel = audit.getChannel();
        String statusCode = audit.getStatusCode();
        String statusDescription = audit.getStatusDescription();


        //- 3 -populate payment in response object
        TransactionPayment transactionPayment = new TransactionPayment();
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        TransactionPaymentPayment transactionPaymentPayment = modelMapper.map(paymentTxn,TransactionPaymentPayment.class);
        transactionPaymentPayment.setCustId(String.valueOf(paymentTxn.getUserId()));
        transactionPaymentPayment.setServiceCharge(String.valueOf(paymentTxn.getTotalServiceCharge()));
        transactionPaymentPayment.setBillerId(String.valueOf(paymentTxn.getToBillerId()));
        transactionPaymentPayment.setChannel(channel);
        transactionPaymentPayment.setStatusCode(statusCode);
        transactionPaymentPayment.setStatusDescription(statusDescription);
        //Setting biller name based on main function type
        String billerName;
        String mainFunction = paymentTxn.getMainFunction();
        //Assumption of either OTHER_BILLER then get billername from biller table, if not, then JOM_PAY name from txn table
        if (mainFunction.equals(OTHER_BILLER)){
            Biller biller = billerRepository.findOne(billerId);
            if (biller == null)
                throw new CommonException(CommonException.GENERIC_ERROR_CODE,"No biller found for biller id " + billerId);
            billerName = biller.getBillerName();
        }else{
            billerName = paymentTxn.getToBillerAccountName();
        }
        transactionPaymentPayment.setBillerName(billerName);

        transactionPayment.setPayment(transactionPaymentPayment);
        return transactionPayment;
    }
}
