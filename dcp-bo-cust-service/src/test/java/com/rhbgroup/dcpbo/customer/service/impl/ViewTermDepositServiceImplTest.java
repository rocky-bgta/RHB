package com.rhbgroup.dcpbo.customer.service.impl;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.rhbgroup.dcp.data.entity.deposits.DepositProfile;
import com.rhbgroup.dcp.data.entity.profiles.UserProfile;
import com.rhbgroup.dcp.data.repository.ProfileRepository;
import com.rhbgroup.dcp.model.Capsule;
import com.rhbgroup.dcp.transformer.ruledriven.util.GSONUtil;
import com.rhbgroup.dcp.uber.deposits.casa.bizlogic.GetTermDepositDetailsLogic;
import com.rhbgroup.dcpbo.customer.dto.TermDepositDetails;
import com.rhbgroup.dcpbo.customer.repository.BoDepositRepository;
import com.rhbgroup.dcpbo.customer.service.ViewDepositService;
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
public class ViewTermDepositServiceImplTest {

    @TestConfiguration
    static class ViewTermDepositServiceImplTestConfig {
        @Bean
        public ViewDepositService sut(BoDepositRepository boDepositRepository,
                                      GetTermDepositDetailsLogic getTermDepositDetailsLogic,
                                      ProfileRepository profileRepository) {
            return new ViewTermDepositServiceImpl(boDepositRepository, getTermDepositDetailsLogic, profileRepository);
        }
    }

    @Autowired
    private ViewDepositService sut;

    @MockBean
    private BoDepositRepository boDepositRepository;

    @MockBean
    private GetTermDepositDetailsLogic getTermDepositDetailsLogic;

    @MockBean
    private ProfileRepository profileRepository;

    private Capsule responseCapsule;

    @Before
    public void setup() {
        responseCapsule = new Capsule();
        String jsonString = "{" +
                "  \"tenure\": \"120\"," +
                "  \"typeOfTenure\": \"M\"," +
                "  \"accountNo\": \"123456789010\"," +
                "  \"interestRate\": \"123\"," +
                "  \"visualPercentage\": \"3\"," +
                "  \"effectiveDate\": \"2018-06-19T00:00:00.000+08:00\"," +
                "  \"lastRenewalDate\": \"2018-01-19T00:00:00.000+08:00\"," +
                "  \"ownershipType\": \"XYZ\"," +
                "  \"maturityDate\": \"2019-06-19T00:00:00.000+08:00\"," +
                "  \"accruedInterest\": \"12345.67\"," +
                "  \"currentBalance\": \"12345.69\"," +
                "  \"projectedValue\": \"2345.67\"," +
                "  \"accountHolder\": [ " +
                "       { " +
                "           \"name\": \"John\"" +
                "       }," +
                "       {" +
                "           \"name\": \"Paul\"" +
                "       }" +
                "   ]" +
                "}";
        responseCapsule.setOperationSuccess(true);
        responseCapsule.updateCurrentMessage(jsonString);
    }

    @Test
    public void getDetail() {
        DepositProfile depositProfile = new DepositProfile();
        depositProfile.setAccountNo("1234556789");
        depositProfile.setUserId(30);
        BDDMockito.given(boDepositRepository.getDepositProfileById(20)).willReturn(depositProfile);

        UserProfile userProfile = new UserProfile();
        userProfile.setCisNo("123");
        userProfile.setId(30);
        BDDMockito.given(profileRepository.getUserProfileByUserId(30)).willReturn(userProfile);

        BDDMockito.given(getTermDepositDetailsLogic.executeBusinessLogic(BDDMockito.any())).willReturn(responseCapsule);

        TermDepositDetails termDepositDetails = (TermDepositDetails)sut.detail(30, "20");
        Assert.assertThat(termDepositDetails.getAccountNo(), Matchers.is("123456789010"));
        Assert.assertThat(termDepositDetails.getEffectiveDate(), Matchers.is("2018-06-19T00:00:00.000+08:00"));
        Assert.assertThat(termDepositDetails.getAccountHolder().get(0).getName(), Matchers.is("John"));
        Assert.assertThat(termDepositDetails.getAccountHolder().get(1).getName(), Matchers.is("Paul"));
    }
}