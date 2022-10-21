package com.rhbgroup.dcpbo.customer.service;

import com.rhbgroup.dcp.loans.bizlogic.GetMortgageTransactionHistoryLogic;
import com.rhbgroup.dcp.loans.model.NonHirePurchaseTransactionsDcpRequest;
import com.rhbgroup.dcp.loans.model.NonHirePurchaseTransactionsPaginationDcpRequest;
import com.rhbgroup.dcp.model.Capsule;
import com.rhbgroup.dcp.util.JsonUtil;
import com.rhbgroup.dcpbo.customer.audit.collector.AdditionalDataHolder;
import com.rhbgroup.dcpbo.customer.dto.MortgageTransactions;
import com.rhbgroup.dcpbo.customer.exception.CommonException;
import com.rhbgroup.dcpbo.customer.model.LoanProfile;
import com.rhbgroup.dcpbo.customer.repository.LoanProfileRepository;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.junit4.SpringRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { GetMortgageTransactionService.class, GetMortgageTransactionServiceTest.Config.class })
public class GetMortgageTransactionServiceTest {

    @Autowired
    GetMortgageTransactionService getMortgageTransactionService;

    @MockBean
    LoanProfileRepository loanProfileRepositoryMock;
    
    @MockBean
    GetMortgageTransactionHistoryLogic getMortgageTransactionHistoryLogic;
    
    @TestConfiguration
   	static class Config {

   		@Bean
   		@Primary
   		public GetMortgageTransactionService getGetMortgageTransactionService() {
   			return new GetMortgageTransactionService();
   		}
   	}
    
    @Test
    public void GetMortgageTransactionsTest(){
        LoanProfile testLoanProfile = new LoanProfile();

        testLoanProfile.setAccountNo("123");
        testLoanProfile.setUserId(321);

        NonHirePurchaseTransactionsDcpRequest inquiry = new NonHirePurchaseTransactionsDcpRequest();
        NonHirePurchaseTransactionsPaginationDcpRequest pagination = new NonHirePurchaseTransactionsPaginationDcpRequest();

        Capsule testCapsule = new Capsule();

        inquiry.setAccountNo("123");

        pagination.setFirstKey("1");
        pagination.setLastKey("2");
        pagination.setPageCounter(1);

        inquiry.setPagination(pagination);
        testCapsule.updateCurrentMessage(JsonUtil.objectToJson(inquiry));
        testCapsule.setUserId(321);

        Capsule resultCapsule = new Capsule();

        String jsonString = "{" +
                "  \"pagination\": {" +
                "    \"firstKey\": \"1\"," +
                "    \"lastKey\": \"2\"," +
                "    \"isLastPage\": false," +
                "    \"pageCounter\": 1" +
                "  }," +
                "  \"transactionHistory\": [" +
                "    {" +
                "      \"txnDate\": \"2018-06-19T00:00:00.000+08:00\"," +
                "      \"description\": \"Ref1 Ref2\"," +
                "      \"amount\": 12345.67" +
                "    }," +
                "    {" +
                "      \"txnDate\": \"2018-06-19T00:00:00.000+08:00\"," +
                "      \"description\": \"Ref3 Ref4\"," +
                "      \"amount\": 12345.68" +
                "    }" +
                "  ]" +
                "}";

        resultCapsule.setOperationSuccess(true);
        resultCapsule.updateCurrentMessage(jsonString);

        GetMortgageTransactionHistoryLogic getMortgageTransactionHistoryLogicMock = mock(GetMortgageTransactionHistoryLogic.class);

        when(loanProfileRepositoryMock.findByAccountId(321)).thenReturn(testLoanProfile);
        when(getMortgageTransactionHistoryLogicMock.executeBusinessLogic(any(Capsule.class))).thenReturn(resultCapsule);

        getMortgageTransactionService.setGetMortgageTransactionHistoryLogic(getMortgageTransactionHistoryLogicMock);

        MortgageTransactions testResult = getMortgageTransactionService.getMortgageTransactions(1, "321", 1,"1", "2");

        Assert.assertThat(testResult.getPagination().getFirstKey(), Matchers.is("1"));
        Assert.assertThat(testResult.getPagination().getLastKey(), Matchers.is("2"));
        Assert.assertThat(testResult.getPagination().getIsLastPage(), Matchers.is(false));
        Assert.assertThat(testResult.getPagination().getPageCounter(), Matchers.is(1));
        Assert.assertThat(testResult.getTransactionHistory().get(0).getTxnDate(), Matchers.is("2018-06-19T00:00:00.000+08:00"));
        Assert.assertThat(testResult.getTransactionHistory().get(0).getDescription(), Matchers.is("Ref1 Ref2"));
        Assert.assertThat(testResult.getTransactionHistory().get(0).getAmount(), Matchers.is(12345.67));
        Assert.assertThat(testResult.getTransactionHistory().get(1).getTxnDate(), Matchers.is("2018-06-19T00:00:00.000+08:00"));
        Assert.assertThat(testResult.getTransactionHistory().get(1).getDescription(), Matchers.is("Ref3 Ref4"));
        Assert.assertThat(testResult.getTransactionHistory().get(1).getAmount(), Matchers.is(12345.68));
    }

    @Test(expected = CommonException.class)
    public void GetMortgageTransactionsFailTest(){
        LoanProfile testLoanProfile = new LoanProfile();

        testLoanProfile.setAccountNo("123");
        testLoanProfile.setUserId(321);

        NonHirePurchaseTransactionsDcpRequest inquiry = new NonHirePurchaseTransactionsDcpRequest();
        NonHirePurchaseTransactionsPaginationDcpRequest pagination = new NonHirePurchaseTransactionsPaginationDcpRequest();

        Capsule testCapsule = new Capsule();

        inquiry.setAccountNo("123");

        pagination.setFirstKey("1");
        pagination.setLastKey("2");
        pagination.setPageCounter(1);

        inquiry.setPagination(pagination);
        testCapsule.updateCurrentMessage(JsonUtil.objectToJson(inquiry));
        testCapsule.setUserId(321);

        Capsule resultCapsule = new Capsule();

        resultCapsule.setOperationSuccess(false);

        GetMortgageTransactionHistoryLogic getMortgageTransactionHistoryLogicMock = mock(GetMortgageTransactionHistoryLogic.class);

        when(loanProfileRepositoryMock.findByAccountId(321)).thenReturn(testLoanProfile);
        when(getMortgageTransactionHistoryLogicMock.executeBusinessLogic(any(Capsule.class))).thenReturn(resultCapsule);

        getMortgageTransactionService.setGetMortgageTransactionHistoryLogic(getMortgageTransactionHistoryLogicMock);

        getMortgageTransactionService.getMortgageTransactions(1,"321", 1,"1", "2");

    }
}
