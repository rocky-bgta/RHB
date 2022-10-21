package com.rhbgroup.dcpbo.customer.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rhbgroup.dcp.data.entity.profiles.UserProfile;
import com.rhbgroup.dcp.data.repository.ProfileRepository;
import com.rhbgroup.dcp.loans.bizlogic.GetMortgageDetailsLogic;
import com.rhbgroup.dcp.model.Capsule;
import com.rhbgroup.dcpbo.customer.exception.CommonException;
import com.rhbgroup.dcpbo.customer.model.MortgageDetails;
import com.rhbgroup.dcpbo.customer.model.MortgageProfile;
import com.rhbgroup.dcpbo.customer.repository.MortgageProfileRepository;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {MortgageDetailsService.class, MortgageProfileRepository.class, MortgageDetailsServiceTests.Config.class})
public class MortgageDetailsServiceTests {

    private static final double DELTA = 1e-15;

    @Autowired
    MortgageDetailsService mortgageDetailsService;

    @MockBean
    MortgageProfileRepository mortgageProfileRepositoryMock;

    @MockBean
    ProfileRepository profileRepositoryMock;

    @MockBean
    GetMortgageDetailsLogic getMortgageDetailsLogicMock;

    @TestConfiguration
    static class Config {
        @Bean
        @Primary
        public GetMortgageDetailsLogic getGetMortgageDetailsLogic() {
            return new GetMortgageDetailsLogic();
        }
    }

    private static Logger logger = LogManager.getLogger(MortgageDetailsServiceTests.class);

    @Test
    public void getMortgageDetailsTest_capsuleSuccessful() throws Throwable {
        getMortgageDetailsTest(true);
    }

    @Test
    public void getMortgageWithFlexiDetailsTest_capsuleSuccessful() throws Throwable {
        getMortgageWithFlexiDetailsTest(true);
    }

    @Test
    public void getMortgageWithoutFlexiDetailsTest_capsuleSuccessful() throws Throwable {
        getMortgageWithoutFlexiDetailsTest(true);
    }

    @Test(expected = CommonException.class)
    public void getMortgageDetailsTest_capsuleUnsuccessful() throws Throwable {
        getMortgageDetailsTest(false);
    }

    public void getMortgageDetailsTest(boolean capsuleSuccessful) throws Throwable {
        logger.debug("getMortgageDetailsTest()");
        logger.debug("    mortgageDetailsService: " + mortgageDetailsService);
        logger.debug("    mortgageRepositoryMock: " + mortgageProfileRepositoryMock);

        int customerId = 1;
        int accountId = 1;

        String accountNo = "1234123412341234";

        UserProfile userProfile = new UserProfile();
        userProfile.setCisNo("123");

        when(profileRepositoryMock.getUserProfileByUserId(customerId)).thenReturn(userProfile);

        MortgageProfile mortgageProfile = new MortgageProfile();
        mortgageProfile.setId(1);
        mortgageProfile.setAccountNo(accountNo);

        when(mortgageProfileRepositoryMock.findById(accountId)).thenReturn(mortgageProfile);

        InputStream is = getClass().getClassLoader().getResourceAsStream("GetMortgageDetailsLogic.json");
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        StringBuilder sbld = new StringBuilder();
        String line = null;
        while ((line = br.readLine()) != null)
            sbld.append(line);
        br.close();
        is.close();

        String jsonStr = sbld.toString();
        logger.debug("    jsonStr: " + jsonStr);

        Capsule capsule = new Capsule();
        capsule.updateCurrentMessage(jsonStr);
        capsule.setOperationSuccess(capsuleSuccessful);

        when(getMortgageDetailsLogicMock.executeBusinessLogic(Mockito.any())).thenReturn(capsule);

        MortgageDetails mortgageDetails = (MortgageDetails) mortgageDetailsService.getMortgageDetails(customerId, accountNo);
        logger.debug("    mortgageDetails: " + mortgageDetails);

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode dataNode = objectMapper.readTree(jsonStr);

        assertNotNull(mortgageDetails);
        assertEquals(4500.0, (dataNode.get("remainingAmount").doubleValue()), DELTA);
        assertEquals(500.0, (dataNode.get("redrawalAmount").doubleValue()), DELTA);
        assertEquals(255.0, (dataNode.get("overdueAmount").doubleValue()), DELTA);
        assertEquals(mortgageDetails.getPaymentDueDate(), dataNode.get("paymentDueDate").textValue());
        assertEquals(200.0, (dataNode.get("monthlyPayment").doubleValue()), DELTA);
        assertEquals(5000.0, (dataNode.get("loanAmount").doubleValue()), DELTA);
        assertEquals(mortgageDetails.getTypeOfTerm(), dataNode.get("typeOfTerm").textValue());
        assertEquals(124, dataNode.get("originalTenure").intValue());
        assertEquals(4500.0, dataNode.get("remainingAmount").doubleValue(), DELTA);
        assertEquals(mortgageDetails.getInterestRate(), (String.valueOf(dataNode.get("interestRate"))));
        assertEquals(mortgageDetails.getAccountOwnership(), dataNode.get("accountOwnership").textValue());
        assertTrue(dataNode.get("accountHolder").isArray());
        assertEquals(2, dataNode.get("accountHolder").size());
        assertEquals(mortgageDetails.getAccountHolder().get(0).getName(), dataNode.get("accountHolder").get(0).get("name").textValue());
        assertEquals(mortgageDetails.getAccountHolder().get(1).getName(), dataNode.get("accountHolder").get(1).get("name").textValue());
    }

    //	@Test(expected = CommonException.class)
    public void getMortgageDetailsTest_notFound() throws Exception {
        logger.debug("getMortgageDetailsTest_notFound()");

        Capsule capsule = new Capsule();

        capsule.setOperationSuccess(false);

        when(getMortgageDetailsLogicMock.executeBusinessLogic(Mockito.any())).thenReturn(capsule);

        int customerId = 1;
        String accountNo = "1";

        mortgageDetailsService.getMortgageDetails(customerId, accountNo);
    }

    public void getMortgageWithFlexiDetailsTest(boolean capsuleSuccessful) throws IOException {
        logger.debug("getMortgageWithFlexiDetailsTest()");
        logger.debug("    mortgageDetailsService: " + mortgageDetailsService);
        logger.debug("    mortgageRepositoryMock: " + mortgageProfileRepositoryMock);

        int customerId = 1;
        int accountId = 1;

        String accountNo = "234567898765";

        UserProfile userProfile = new UserProfile();
        userProfile.setCisNo("123");

        when(profileRepositoryMock.getUserProfileByUserId(customerId)).thenReturn(userProfile);

        MortgageProfile mortgageProfile = new MortgageProfile();
        mortgageProfile.setId(1);
        mortgageProfile.setAccountNo(accountNo);

        when(mortgageProfileRepositoryMock.findById(accountId)).thenReturn(mortgageProfile);

        InputStream is = getClass().getClassLoader().getResourceAsStream("GetMortgageWithFlexiDetailsLogic.json");
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        StringBuilder sbld = new StringBuilder();
        String line = null;
        while ((line = br.readLine()) != null)
            sbld.append(line);
        br.close();
        is.close();

        String jsonStr = sbld.toString();
        logger.debug("    jsonStr: " + jsonStr);

        Capsule capsule = new Capsule();
        capsule.updateCurrentMessage(jsonStr);
        capsule.setOperationSuccess(capsuleSuccessful);

        when(getMortgageDetailsLogicMock.executeBusinessLogic(Mockito.any())).thenReturn(capsule);

        MortgageDetails mortgageDetails = (MortgageDetails) mortgageDetailsService.getMortgageDetails(customerId, accountNo);
        logger.debug("    mortgageFlexiDetails: " + mortgageDetails);

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode dataNode = objectMapper.readTree(jsonStr);

        assertNotNull(mortgageDetails);
        // Mortgage details
        assertEquals(4500.0, (dataNode.get("remainingAmount").doubleValue()), DELTA);
        assertEquals(255.0, (dataNode.get("overdueAmount").doubleValue()), DELTA);
        assertEquals(mortgageDetails.getPaymentDueDate(), dataNode.get("paymentDueDate").textValue());
        assertEquals(200.0, (dataNode.get("monthlyPayment").doubleValue()), DELTA);
        assertEquals(5000.0, (dataNode.get("loanAmount").doubleValue()), DELTA);
        assertEquals(mortgageDetails.getTypeOfTerm(), dataNode.get("typeOfTerm").textValue());
        assertEquals(124, dataNode.get("originalTenure").intValue());
        assertEquals(4500.0, dataNode.get("remainingAmount").doubleValue(), DELTA);
        assertEquals(mortgageDetails.getInterestRate(), (String.valueOf(dataNode.get("interestRate"))));
        assertEquals(mortgageDetails.getAccountNo(), dataNode.get("accountNo").textValue());
        // Flexi details
        assertEquals(true, dataNode.get("isRedrawalAvailable").booleanValue());
        assertEquals(500.0, (dataNode.get("redrawalAmount").doubleValue()), DELTA);
    }

    public void getMortgageWithFlexiRedrawalFalseDetailsTest(boolean capsuleSuccessful) throws IOException {
        logger.debug("getMortgageWithFlexiRedrawalFalseDetailsTest()");
        logger.debug("    mortgageDetailsService: " + mortgageDetailsService);
        logger.debug("    mortgageRepositoryMock: " + mortgageProfileRepositoryMock);

        int customerId = 1;
        int accountId = 1;

        String accountNo = "234567898765";

        MortgageProfile mortgageProfile = new MortgageProfile();
        mortgageProfile.setId(1);
        mortgageProfile.setAccountNo(accountNo);

        when(mortgageProfileRepositoryMock.findById(accountId)).thenReturn(mortgageProfile);

        InputStream is = getClass().getClassLoader().getResourceAsStream("GetMortgageWithFlexiRedrawalFalseDetailsLogic.json");
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        StringBuilder sbld = new StringBuilder();
        String line = null;
        while ((line = br.readLine()) != null)
            sbld.append(line);
        br.close();
        is.close();

        String jsonStr = sbld.toString();
        logger.debug("    jsonStr: " + jsonStr);

        Capsule capsule = new Capsule();
        capsule.updateCurrentMessage(jsonStr);
        capsule.setOperationSuccess(capsuleSuccessful);

        when(getMortgageDetailsLogicMock.executeBusinessLogic(Mockito.any())).thenReturn(capsule);

        MortgageDetails mortgageDetails = (MortgageDetails) mortgageDetailsService.getMortgageDetails(customerId, accountNo);
        logger.debug("    mortgageFlexiDetails: " + mortgageDetails);

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode dataNode = objectMapper.readTree(jsonStr);

        assertNotNull(mortgageDetails);
        // Mortgage details
        assertEquals(4500.0, (dataNode.get("remainingAmount").doubleValue()), DELTA);
        assertEquals(255.0, (dataNode.get("overdueAmount").doubleValue()), DELTA);
        assertEquals(mortgageDetails.getPaymentDueDate(), dataNode.get("paymentDueDate").textValue());
        assertEquals(200.0, (dataNode.get("monthlyPayment").doubleValue()), DELTA);
        assertEquals(5000.0, (dataNode.get("loanAmount").doubleValue()), DELTA);
        assertEquals(mortgageDetails.getTypeOfTerm(), dataNode.get("typeOfTerm").textValue());
        assertEquals(124, dataNode.get("originalTenure").intValue());
        assertEquals(4500.0, dataNode.get("remainingAmount").doubleValue(), DELTA);
        assertEquals(mortgageDetails.getInterestRate(), (String.valueOf(dataNode.get("interestRate"))));
        assertEquals(mortgageDetails.getAccountNo(), dataNode.get("accountNo").textValue());
        // Flexi details
        assertEquals(false, dataNode.get("isRedrawalAvailable").booleanValue());
        assertEquals(500.0, (dataNode.get("redrawalAmount").doubleValue()), DELTA);
    }

    public void getMortgageWithoutFlexiDetailsTest(boolean capsuleSuccessful) throws IOException {
        logger.debug("getMortgageWithoutFlexiDetailsTest()");
        logger.debug("    mortgageDetailsService: " + mortgageDetailsService);
        logger.debug("    mortgageRepositoryMock: " + mortgageProfileRepositoryMock);

        int customerId = 1;
        int accountId = 1;

        String accountNo = "234567898765";

        UserProfile userProfile = new UserProfile();
        userProfile.setCisNo("123");

        when(profileRepositoryMock.getUserProfileByUserId(customerId)).thenReturn(userProfile);

        MortgageProfile mortgageProfile = new MortgageProfile();
        mortgageProfile.setId(1);
        mortgageProfile.setAccountNo(accountNo);

        when(mortgageProfileRepositoryMock.findById(accountId)).thenReturn(mortgageProfile);

        InputStream is = getClass().getClassLoader().getResourceAsStream("GetMortgageWithoutFlexiDetailsLogic.json");
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        StringBuilder sbld = new StringBuilder();
        String line = null;
        while ((line = br.readLine()) != null)
            sbld.append(line);
        br.close();
        is.close();

        String jsonStr = sbld.toString();
        logger.debug("    jsonStr: " + jsonStr);

        Capsule capsule = new Capsule();
        capsule.updateCurrentMessage(jsonStr);
        capsule.setOperationSuccess(capsuleSuccessful);

        when(getMortgageDetailsLogicMock.executeBusinessLogic(Mockito.any())).thenReturn(capsule);

        MortgageDetails mortgageDetails = (MortgageDetails) mortgageDetailsService.getMortgageDetails(customerId, accountNo);
        logger.debug("    mortgageFlexiDetails: " + mortgageDetails);

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode dataNode = objectMapper.readTree(jsonStr);

        assertNotNull(mortgageDetails);
        // Mortgage details
        assertEquals(4500.0, (dataNode.get("remainingAmount").doubleValue()), DELTA);
        assertEquals(255.0, (dataNode.get("overdueAmount").doubleValue()), DELTA);
        assertEquals(mortgageDetails.getPaymentDueDate(), dataNode.get("paymentDueDate").textValue());
        assertEquals(200.0, (dataNode.get("monthlyPayment").doubleValue()), DELTA);
        assertEquals(5000.0, (dataNode.get("loanAmount").doubleValue()), DELTA);
        assertEquals(mortgageDetails.getTypeOfTerm(), dataNode.get("typeOfTerm").textValue());
        assertEquals(124, dataNode.get("originalTenure").intValue());
        assertEquals(4500.0, dataNode.get("remainingAmount").doubleValue(), DELTA);
        assertEquals(mortgageDetails.getInterestRate(), (String.valueOf(dataNode.get("interestRate"))));
        assertEquals(mortgageDetails.getAccountNo(), dataNode.get("accountNo").textValue());
        // Flexi details
        assertEquals(false, dataNode.get("isRedrawalAvailable").booleanValue());
        assertNull(mortgageDetails.getRedrawalAmount());
    }
}
