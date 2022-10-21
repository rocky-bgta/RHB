package com.rhbgroup.dcpbo.customer.service;

import com.rhbgroup.dcp.loans.bizlogic.GetMortgageTransactionHistoryLogic;
import com.rhbgroup.dcp.loans.model.NonHirePurchaseTransactionsDcpRequest;
import com.rhbgroup.dcp.loans.model.NonHirePurchaseTransactionsPaginationDcpRequest;
import com.rhbgroup.dcp.model.Capsule;
import com.rhbgroup.dcp.model.Constants;
import com.rhbgroup.dcp.model.SourceChannel;
import com.rhbgroup.dcp.util.JsonUtil;
import com.rhbgroup.dcpbo.customer.dto.MortgageTransactions;
import com.rhbgroup.dcpbo.customer.exception.CommonException;
import com.rhbgroup.dcpbo.customer.repository.LoanProfileRepository;
import lombok.Setter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.stream.Collectors;

@Service
public class GetMortgageTransactionService {

    @Autowired
    LoanProfileRepository loanProfileRepository;

    private final Logger log = LogManager.getLogger(GetMortgageTransactionService.class);


    @Setter
    @Autowired
    private GetMortgageTransactionHistoryLogic getMortgageTransactionHistoryLogic;

    public MortgageTransactions getMortgageTransactions(int customerId, String accountNo, Integer pageCounter, String firstKey, String lastKey) {
        NonHirePurchaseTransactionsDcpRequest inquiry = new NonHirePurchaseTransactionsDcpRequest();
        MortgageTransactions mortgageTransactions = new MortgageTransactions();
        NonHirePurchaseTransactionsPaginationDcpRequest pagination = new NonHirePurchaseTransactionsPaginationDcpRequest();
        Capsule capsule = new Capsule();

        inquiry.setAccountNo(accountNo);

        pagination.setFirstKey(firstKey);
        pagination.setLastKey(lastKey);
        pagination.setPageCounter(pageCounter);

        inquiry.setPagination(pagination);
        capsule.updateCurrentMessage(JsonUtil.objectToJson(inquiry));
        capsule.setUserId(customerId);
        capsule.setProperty(Constants.SOURCE_CHANNEL, SourceChannel.DCP_MBK);
        Capsule response = new Capsule();
        try {
            response = getMortgageTransactionHistoryLogic.executeBusinessLogic(capsule);
        }catch(Exception e){
            log.error(Arrays.stream(e.getStackTrace())
                    .map(StackTraceElement::toString)
                    .collect(Collectors.joining("\n")));
            throw new CommonException(CommonException.GENERIC_ERROR_CODE, "EAI method execute business logic error!");
        }
        if(response.isOperationSuccessful()){
            mortgageTransactions.convert(response);
            return mortgageTransactions;
        } else {
            throw new CommonException(CommonException.GENERIC_ERROR_CODE, "Unsuccessful EAI operation");

        }
    }
}
