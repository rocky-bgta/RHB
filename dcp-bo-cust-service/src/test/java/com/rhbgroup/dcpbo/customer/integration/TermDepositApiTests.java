package com.rhbgroup.dcpbo.customer.integration;

import com.rhbgroup.dcp.data.entity.deposits.DepositProfile;
import com.rhbgroup.dcp.data.entity.profiles.UserProfile;
import com.rhbgroup.dcp.data.repository.ProfileRepository;
import com.rhbgroup.dcp.model.Capsule;
import com.rhbgroup.dcp.uber.deposits.casa.bizlogic.GetTermDepositDetailsLogic;
import com.rhbgroup.dcpbo.customer.controller.TermDepositController;
import com.rhbgroup.dcpbo.customer.dto.AccountHolder;
import com.rhbgroup.dcpbo.customer.dto.TermDepositDetails;
import com.rhbgroup.dcpbo.customer.repository.BoDepositRepository;
import com.rhbgroup.dcpbo.customer.service.ViewDepositService;
import com.rhbgroup.dcpbo.customer.service.impl.ViewTermDepositServiceImpl;
import org.codehaus.jackson.map.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest(classes = {TermDepositApiTests.class, TermDepositController.class, ViewDepositService.class})
@EnableWebMvc
public class TermDepositApiTests {
    @Autowired
    MockMvc mockMvc;

    @TestConfiguration
    static class TermDepositApiTestsConfig {
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
    public void setup() throws IOException {

        AccountHolder accountHolder1 = new AccountHolder();
        AccountHolder accountHolder2 = new AccountHolder();
        accountHolder1.setName("John");
        accountHolder2.setName("Paul");
        List<AccountHolder> accountHolderList = new ArrayList<>();
        accountHolderList.add(accountHolder1);
        accountHolderList.add(accountHolder2);

        TermDepositDetails termDepositDetails = new TermDepositDetails();
        termDepositDetails.setAccountNo("1234556789");
        termDepositDetails.setVisualPercentage(25);
        termDepositDetails.setEffectiveDate("20170702");
        termDepositDetails.setMaturityDate("20171002");
        termDepositDetails.setCurrentBalance("15000.0");
        termDepositDetails.setProjectedValue("15240.0");
        termDepositDetails.setInterestRate(3.2);
        termDepositDetails.setOwnershipType("IND");
        termDepositDetails.setTypeOfTenure("M");
        termDepositDetails.setTenure(120);
        termDepositDetails.setLastRenewalDate("20170402");
        termDepositDetails.setAccruedInterest("140.0");
        termDepositDetails.setAccountHolder(accountHolderList);

        responseCapsule = new Capsule();
        responseCapsule.setOperationSuccess(true);
        responseCapsule.updateCurrentMessage(new ObjectMapper().writeValueAsString(termDepositDetails));

    }

    @Test
    public void getTermDepositDetails() throws Exception {
        DepositProfile depositProfile = new DepositProfile();
        depositProfile.setAccountNo("1234556789");
        depositProfile.setUserId(30);
        when(boDepositRepository.getDepositProfileById(anyInt())).thenReturn(depositProfile);

        UserProfile userProfile = new UserProfile();
        userProfile.setCisNo("123");
        userProfile.setId(30);
        when(profileRepository.getUserProfileByUserId(anyInt())).thenReturn(userProfile);

        when(getTermDepositDetailsLogic.executeBusinessLogic(any())).thenReturn(responseCapsule);

        this.mockMvc.perform(MockMvcRequestBuilders.get("/bo/cs/customer/termdeposit/20/details")
                .header("customerId", 1))
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("accountNo", Matchers.is("1234556789")))
                .andExpect(MockMvcResultMatchers.jsonPath("visualPercentage", Matchers.is(25)))
                .andExpect(MockMvcResultMatchers.jsonPath("effectiveDate", Matchers.is("20170702")))
                .andExpect(MockMvcResultMatchers.jsonPath("maturityDate", Matchers.is("20171002")))
                .andExpect(MockMvcResultMatchers.jsonPath("currentBalance", Matchers.is(15000.0)))
                .andExpect(MockMvcResultMatchers.jsonPath("projectedValue", Matchers.is(15240.0)))
                .andExpect(MockMvcResultMatchers.jsonPath("interestRate", Matchers.is(3.2)))
                .andExpect(MockMvcResultMatchers.jsonPath("ownershipType", Matchers.is("IND")))
                .andExpect(MockMvcResultMatchers.jsonPath("typeOfTenure", Matchers.is("M")))
                .andExpect(MockMvcResultMatchers.jsonPath("tenure", Matchers.is(120)))
                .andExpect(MockMvcResultMatchers.jsonPath("lastRenewalDate", Matchers.is("20170402")))
                .andExpect(MockMvcResultMatchers.jsonPath("accruedInterest", Matchers.is(140.0)))
                .andExpect(MockMvcResultMatchers.jsonPath("accountHolder[0].name", Matchers.is("John")))
                .andExpect(MockMvcResultMatchers.jsonPath("accountHolder[1].name", Matchers.is("Paul")));
    }
}
