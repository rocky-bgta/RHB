package com.rhbgroup.dcpbo.customer.service.impl;

import com.rhbgroup.dcp.adaptor.BusinessAdaptor;
import com.rhbgroup.dcp.data.entity.loans.LoanProfile;
import com.rhbgroup.dcp.data.entity.profiles.UserProfile;
import com.rhbgroup.dcp.model.Capsule;
import com.rhbgroup.dcpbo.customer.contract.ViewTransaction;
import com.rhbgroup.dcpbo.customer.dto.AsbTransactions;
import com.rhbgroup.dcpbo.customer.dto.LoanPersonalTransactions;
import com.rhbgroup.dcpbo.customer.repository.BoLoanProfileRepository;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
public class ViewTransactionPersonalLoanServiceImplTest {

    @TestConfiguration
    static class testConfiguration {
        @Bean
        public ViewTransaction getSut(BoLoanProfileRepository boLoanProfileRepository,
                                                              BusinessAdaptor personalFinanceTransactionHistoryLogic) {
            return new ViewTransactionPersonalLoanServiceImpl(boLoanProfileRepository, personalFinanceTransactionHistoryLogic);
        }
    }

    @Autowired
    private ViewTransaction sut;

    @MockBean(name = "boLoanProfileRepository")
    private BoLoanProfileRepository boLoanProfileRepository;

    @MockBean(name = "personalFinanceTransactionHistoryLogic")
    private BusinessAdaptor personalFinanceTransactionHistoryLogic;

    private Capsule responseCapsule;

    @Before
    public void setup() {
        responseCapsule = new Capsule();
        responseCapsule.setOperationSuccess(true);
        //Based on GetAsbLoanTransactionHistoryTest
        String responseJsonString = "{\"pagination\":{\"firstKey\":\"1\",\"lastKey\":\"2\",\"isLastPage\":false,\"pageCounter\":\"1\"},\n" +
                "\"transactionHistory\":\n" +
                "[{\"txnDate\":\"2018-06-19T00:00:00.000+08:00\",\"description\":\"Ref1 Ref2\",\"amount\":12345.67},\n" +
                "{\"txnDate\":\"2018-06-19T00:00:00.000+08:00\",\"description\":\"Ref3 Ref4\",\"amount\":12345.68}]}";

        responseCapsule.updateCurrentMessage(responseJsonString);
        responseCapsule.setUserId(2);
    }

    @Test
    public void listingWithResult() {
        LoanProfile loanProfile = new LoanProfile();
        loanProfile.setAccountNo("24");
        loanProfile.setUserId(66);
        BDDMockito.given(boLoanProfileRepository.findByAccountId(21)).willReturn(loanProfile);

        BDDMockito.given(personalFinanceTransactionHistoryLogic.executeBusinessLogic(BDDMockito.any())).willReturn(responseCapsule);

        LoanPersonalTransactions loanPersonalTransactions = (LoanPersonalTransactions)sut.listing(12, "21", 4, "7", "10");

        Assert.assertEquals("2", loanPersonalTransactions.getPagination().getLastKey());
        Assert.assertEquals("1", loanPersonalTransactions.getPagination().getFirstKey());
        Assert.assertEquals(2, loanPersonalTransactions.getTransactionHistory().size());
        Assert.assertEquals("2018-06-19T00:00:00.000+08:00", loanPersonalTransactions.getTransactionHistory().get(1).getTxnDate());
        Assert.assertEquals("Ref3 Ref4", loanPersonalTransactions.getTransactionHistory().get(1).getDescription());
        Assert.assertThat(loanPersonalTransactions.getTransactionHistory().get(1).getAmount(), Matchers.closeTo(12345.68, 0.05));
    }
}