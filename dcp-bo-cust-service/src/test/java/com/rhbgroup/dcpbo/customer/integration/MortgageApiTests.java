package com.rhbgroup.dcpbo.customer.integration;

import com.rhbgroup.dcp.data.entity.profiles.UserProfile;
import com.rhbgroup.dcp.data.repository.ProfileRepository;
import com.rhbgroup.dcp.loans.bizlogic.GetMortgageDetailsLogic;
import com.rhbgroup.dcp.model.Capsule;
import com.rhbgroup.dcp.util.JsonUtil;
import com.rhbgroup.dcpbo.customer.controller.MortgageController;
import com.rhbgroup.dcpbo.customer.model.MortgageDetails;
import com.rhbgroup.dcpbo.customer.model.MortgageProfile;
import com.rhbgroup.dcpbo.customer.repository.MortgageProfileRepository;
import com.rhbgroup.dcpbo.customer.service.MortgageDetailsService;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest(classes = {MortgageApiTests.class, MortgageController.class, MortgageDetailsService.class})
@EnableWebMvc
public class MortgageApiTests {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    MortgageDetailsService mortgageDetailsService;

    @MockBean
    GetMortgageDetailsLogic getMortgageDetailsLogicMock;

    @MockBean
    MortgageProfileRepository mortgageProfileRepositoryMock;

    @MockBean
    ProfileRepository profileRepositoryMock;

    private static Logger logger = LogManager.getLogger(MortgageApiTests.class);

    @Test
    public void getMortgageDetailsAdditionalField() throws Exception {

        logger.debug("getMortgageWithoutFlexiDetailsTest()");
        logger.debug("    mortgageDetailsServiceMock: " + mortgageDetailsService);

        int customerId = 1;
        String accountNo = "1";
        logger.debug("    accountNo: " + accountNo);

        InputStream is = getClass().getClassLoader().getResourceAsStream("GetMortgageDetailsLogic.json");
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        StringBuilder sbld = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null)
            sbld.append(line);
        br.close();
        is.close();

        String jsonStr = sbld.toString();
        logger.debug("    jsonStr: " + jsonStr);

        UserProfile userProfile = new UserProfile();
        userProfile.setCisNo("123");

        when(profileRepositoryMock.getUserProfileByUserId(customerId)).thenReturn(userProfile);

        MortgageProfile mortgageProfile = new MortgageProfile();
        mortgageProfile.setId(1);
        mortgageProfile.setAccountNo(accountNo);

        when(mortgageProfileRepositoryMock.findById(customerId)).thenReturn(mortgageProfile);

        Capsule capsule = new Capsule();
        capsule.updateCurrentMessage(jsonStr);
        capsule.setOperationSuccess(true);

        when(getMortgageDetailsLogicMock.executeBusinessLogic(Mockito.any())).thenReturn(capsule);

        MortgageDetails mortgageDetails = JsonUtil.jsonToObject(jsonStr, MortgageDetails.class);
        logger.debug("    mortgageFlexiDetails: " + mortgageDetails);

        mockMvc.perform(MockMvcRequestBuilders.get("/bo/cs/customer/loan/mortgage/" + accountNo + "/details").header("customerId", customerId))
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andExpect(MockMvcResultMatchers.jsonPath("$.isRedrawalAvailable", is(true)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.redrawalAmount", is("500.0")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.remainingAmount", is("4500.0")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.overdueAmount", is("255.0")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.paymentDueDate", is("20180927")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.monthlyPayment", is("200.0")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.loanAmount", is("5000.0")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.typeOfTerm", is("M")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.originalTenure", is("124")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.remainingTenure", is("100")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.interestRate", is("5.12")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.accountOwnership", is("Individual")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.accountHolder[0].name", is("John")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.accountHolder[1].name", is("Paul")));
    }
}
