package com.rhbgroup.dcpbo.customer.controller;

import com.rhbgroup.dcpbo.customer.dto.AccountHolder;
import com.rhbgroup.dcpbo.customer.dto.TermDepositDetails;
import com.rhbgroup.dcpbo.customer.service.ViewDepositService;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.util.ArrayList;
import java.util.List;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest(classes = { TermDepositControllerTest.class, 
		TermDepositController.class })
@EnableWebMvc
public class TermDepositControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean(name = "viewTermDepositService")
    private ViewDepositService viewTermViewDepositService;

    private TermDepositDetails termDepositDetails;

    @Before
    public void setup() {
        AccountHolder accountHolder1 = new AccountHolder();
        AccountHolder accountHolder2 = new AccountHolder();
        accountHolder1.setName("John");
        accountHolder2.setName("Paul");
        List<AccountHolder> accountHolderList = new ArrayList<>();
        accountHolderList.add(accountHolder1);
        accountHolderList.add(accountHolder2);

        termDepositDetails = new TermDepositDetails();
        termDepositDetails.setAccountNo("1234556789");
        termDepositDetails.setVisualPercentage(25);
        termDepositDetails.setEffectiveDate("20170702");
        termDepositDetails.setMaturityDate("20171002");
        termDepositDetails.setCurrentBalance("15000.0");
        termDepositDetails.setProjectedValue("15240.0");
        termDepositDetails.setInterestRate(3.2);
        termDepositDetails.setOwnershipType("IND");
        termDepositDetails.setTypeOfTenure("M");
        termDepositDetails.setTenure(3);
        termDepositDetails.setLastRenewalDate("20170402");
        termDepositDetails.setAccruedInterest("140.0");
        termDepositDetails.setAccountHolder(accountHolderList);
    }

    @Test
    public void getTermDepositDetails() throws Exception {
        BDDMockito.given(viewTermViewDepositService.detail(1,"20")).willReturn(termDepositDetails);

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
                .andExpect(MockMvcResultMatchers.jsonPath("tenure", Matchers.is(3)))
                .andExpect(MockMvcResultMatchers.jsonPath("lastRenewalDate", Matchers.is("20170402")))
                .andExpect(MockMvcResultMatchers.jsonPath("accruedInterest", Matchers.is(140.0)))
                .andExpect(MockMvcResultMatchers.jsonPath("accountHolder[0].name", Matchers.is("John")))
                .andExpect(MockMvcResultMatchers.jsonPath("accountHolder[1].name", Matchers.is("Paul")));

//        BDDMockito.verify(viewTermViewDepositService).detail(1, "20");
    }
}