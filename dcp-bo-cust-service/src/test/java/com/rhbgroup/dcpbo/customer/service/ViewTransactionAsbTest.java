package com.rhbgroup.dcpbo.customer.service;

import com.rhbgroup.dcp.adaptor.BusinessAdaptor;
import com.rhbgroup.dcp.data.entity.loans.LoanProfile;
import com.rhbgroup.dcp.data.entity.profiles.UserProfile;
import com.rhbgroup.dcp.data.repository.ProfileRepository;
import com.rhbgroup.dcp.loans.model.NonHirePurchaseTransactionsDcpRequest;
import com.rhbgroup.dcp.loans.model.NonHirePurchaseTransactionsPaginationDcpRequest;
import com.rhbgroup.dcp.model.Capsule;
import com.rhbgroup.dcp.util.JsonUtil;
import com.rhbgroup.dcpbo.customer.contract.ViewTransaction;
import com.rhbgroup.dcpbo.customer.dto.AsbTransactions;
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

@RunWith(SpringRunner.class)
public class ViewTransactionAsbTest {

    @TestConfiguration
    static class testConfiguration {
        @Bean
        public ViewTransaction getSut() {
            return new ViewTransactionAsb();
        }
    }

    @Autowired
    private ViewTransaction sut;
                                          
    @MockBean(name = "profileRepository")
    private ProfileRepository profileRepository;

    @MockBean(name = "boLoanProfileRepository")
    private BoLoanProfileRepository boLoanProfileRepository;

    @MockBean(name = "asbLoanTransactionHistoryLogic")
    private BusinessAdaptor asbLoanTransactionHistoryLogic;

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
        UserProfile userProfile = new UserProfile();
        userProfile.setId(13);
        BDDMockito.given(profileRepository.getUserProfileByUserId(12)).willReturn(userProfile);

        LoanProfile loanProfile = new LoanProfile();
        loanProfile.setAccountNo("24");
        loanProfile.setLoanProductId(22);
        BDDMockito.given(boLoanProfileRepository.findByAccountId(21)).willReturn(loanProfile);

        BDDMockito.given(asbLoanTransactionHistoryLogic.executeBusinessLogic(BDDMockito.any())).willReturn(responseCapsule);

        AsbTransactions asbTransactions = (AsbTransactions)sut.listing(12, "21", 4, "7", "10");

        Assert.assertEquals("2", asbTransactions.getPagination().getLastKey());
        Assert.assertEquals("1", asbTransactions.getPagination().getFirstKey());
        Assert.assertEquals(2, asbTransactions.getTransactionHistory().size());
        Assert.assertEquals("2018-06-19T00:00:00.000+08:00", asbTransactions.getTransactionHistory().get(1).getTxnDate());
        Assert.assertEquals("Ref3 Ref4", asbTransactions.getTransactionHistory().get(1).getDescription());
        Assert.assertThat(asbTransactions.getTransactionHistory().get(1).getAmount(), Matchers.closeTo(12345.68, 0.05));
    }

    @Test
    public void padWith0() {
        ViewTransactionAsb viewTransactionAsb = new ViewTransactionAsb();

        String input = " 2";
        String actual = viewTransactionAsb.padWith0WhenNeeded(input);
        Assert.assertEquals( "02" ,actual);
    }

    @Test
    public void notPadWith0() {
        ViewTransactionAsb viewTransactionAsb = new ViewTransactionAsb();

        String input = "01";
        String actual = viewTransactionAsb.padWith0WhenNeeded(input);
        Assert.assertEquals( "01" ,actual);
    }
}