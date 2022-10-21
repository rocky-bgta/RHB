package com.rhbgroup.dcp.bo.batch.job.factory;

import com.rhbgroup.dcp.bo.batch.framework.constants.BatchErrorCode;
import com.rhbgroup.dcp.bo.batch.framework.exception.BatchException;
import com.rhbgroup.dcp.bo.batch.job.enums.EPullAutoEnrollment;
import com.rhbgroup.dcp.bo.batch.job.model.EPullAutoEnrollmentDetails;
import com.rhbgroup.dcp.bo.batch.job.repository.EPullAutoEnrollmentRepositoryImpl;
import com.rhbgroup.dcp.estatement.model.EPullAutoEnrollmentAccountRequest;
import com.rhbgroup.dcp.estatement.model.EPullAutoEnrollmentCardRequest;
import com.rhbgroup.dcp.estatement.model.EPullAutoEnrollmentRequest;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Component
@Lazy
@Getter
@Setter
public class EPullAutoEnrollmentRequestFactory {

    public static final Predicate<EPullAutoEnrollmentDetails> statementType5 = e -> e.getStatementType() == 5;

    @Autowired
    EPullAutoEnrollmentRepositoryImpl ePullEnrollmentRepo;

    private int userId;

    public EPullAutoEnrollmentRequest buildRequest() throws BatchException {

        if(userId == 0) {
            throw new BatchException(BatchErrorCode.BATCH_DEPENDENCY_MISSING_ERROR, " User id is missing.");
        }

        EPullAutoEnrollmentRequest request = new EPullAutoEnrollmentRequest(
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>());

        extractDepositAccounts(request);
        extractLoanAccounts(request);
        extractCardNos(request);

        return request;
    }

    private List<EPullAutoEnrollmentAccountRequest> getAccounts(List<EPullAutoEnrollmentDetails> ePullAutoEnrollmentDetails, String accountType) {
        return ePullAutoEnrollmentDetails.stream().filter(e -> e.getAccountType().equals(accountType))
                .map(e -> new EPullAutoEnrollmentAccountRequest(e.getAccountNo()))
                .collect(Collectors.toList());
    }

    private List<EPullAutoEnrollmentCardRequest> getCardNos(List<EPullAutoEnrollmentDetails> ePullAutoEnrollmentDetails, String accountType) {
        return ePullAutoEnrollmentDetails.stream().filter(e -> e.getAccountType().equals(accountType))
                .map(e -> new EPullAutoEnrollmentCardRequest(e.getAccountNo()))
                .collect(Collectors.toList());
    }

    private void extractDepositAccounts(EPullAutoEnrollmentRequest request) throws BatchException {
        List<EPullAutoEnrollmentDetails> ePullAutoEnrollmentDetails =
                ePullEnrollmentRepo.getEPullAutoEnrollmentAccount(userId, EPullAutoEnrollment.GET_DEPOSIT.value);
        ePullAutoEnrollmentDetails.removeIf(statementType5);
        request.getSavings().addAll(getAccounts(ePullAutoEnrollmentDetails, "SAVINGS"));
        request.getCurrents().addAll(getAccounts(ePullAutoEnrollmentDetails, "CURRENT"));
        request.getMca().addAll(getAccounts(ePullAutoEnrollmentDetails, "MCA"));
        request.getTermDeposits().addAll(getAccounts(ePullAutoEnrollmentDetails, "TERM_DEPOSIT"));
    }

    private void extractLoanAccounts(EPullAutoEnrollmentRequest request) throws BatchException {
        List<EPullAutoEnrollmentDetails> ePullAutoEnrollmentDetails =
                ePullEnrollmentRepo.getEPullAutoEnrollmentAccount(userId, EPullAutoEnrollment.GET_LOAN.value);
        ePullAutoEnrollmentDetails.removeIf(statementType5);
        request.getMortgages().addAll(getAccounts(ePullAutoEnrollmentDetails, "MORTGAGE"));
        request.getPersonalFinances().addAll(getAccounts(ePullAutoEnrollmentDetails, "PERSONAL_FINANCE"));
        request.getHirePurchases().addAll(getAccounts(ePullAutoEnrollmentDetails, "HIRE_PURCHASE"));
        request.getAsb().addAll(getAccounts(ePullAutoEnrollmentDetails, "ASB"));
    }

    private void extractCardNos(EPullAutoEnrollmentRequest request) throws BatchException {
        List<EPullAutoEnrollmentDetails> ePullAutoEnrollmentDetails = ePullEnrollmentRepo.getEPullAutoEnrollmentAccount(userId, EPullAutoEnrollment.GET_CARD.value);
        ePullAutoEnrollmentDetails.removeIf(statementType5);
        request.getCreditCards().addAll(getCardNos(ePullAutoEnrollmentDetails, "CREDIT_CARD"));
        request.getPrepaidCards().addAll(getCardNos(ePullAutoEnrollmentDetails, "PREPAID_CARD"));
    }
}
