package com.rhbgroup.dcpbo.customer.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rhbgroup.dcp.data.entity.profiles.UserProfile;
import com.rhbgroup.dcp.data.repository.ProfileRepository;
import com.rhbgroup.dcp.loans.bizlogic.GetHirePurchaseAccountDetailsLogic;
import com.rhbgroup.dcp.model.Capsule;
import com.rhbgroup.dcpbo.customer.exception.CommonException;
import com.rhbgroup.dcpbo.customer.model.HirePurchaseDetails;
import com.rhbgroup.dcpbo.customer.model.HirePurchaseProfile;
import com.rhbgroup.dcpbo.customer.repository.HirePurchaseProfileRepository;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {HirePurchaseDetailsService.class, HirePurchaseProfileRepository.class, HirePurchaseDetailsServiceTests.Config.class})
public class HirePurchaseDetailsServiceTests {
	@Autowired
	HirePurchaseDetailsService hirePurchaseDetailsService;

    @MockBean
    ProfileRepository profileRepositoryMock;

	@MockBean
	HirePurchaseProfileRepository hirePurchaseProfileRepositoryMock;

	@MockBean
	GetHirePurchaseAccountDetailsLogic getHirePurchaseAccountDetailsLogicMock;
	
	@TestConfiguration
	static class Config {
		@Bean
		@Primary
		public GetHirePurchaseAccountDetailsLogic getGetHirePurchaseAccountDetailsLogic() {
			return new GetHirePurchaseAccountDetailsLogic();
		}
	}

	private static Logger logger = LogManager.getLogger(HirePurchaseDetailsServiceTests.class);

	@Test
	public void getHirePurchaseDetailsTest_capsuleSuccessful() throws Throwable {
		getHirePurchaseDetailsTest(true);
	}

	@Test(expected = CommonException.class)
	public void getHirePurchaseDetailsTest_capsuleUnsuccessful() throws Throwable {
		getHirePurchaseDetailsTest(false);
	}

	public void getHirePurchaseDetailsTest(boolean capsuleSuccessful) throws Throwable {
		logger.debug("getHirePurchaseDetailsTest()");
		logger.debug("    hirePurchaseDetailsService: " + hirePurchaseDetailsService);
		logger.debug("    hirePurchaseRepositoryMock: " + hirePurchaseProfileRepositoryMock);
		
		int customerId = 1;
		int accountId = 1;
		
        UserProfile userProfile = new UserProfile();
        userProfile.setCisNo("1234567890");
        userProfile.setUsername("ikhwan");
        when(profileRepositoryMock.getUserProfileByUserId(Mockito.anyInt())).thenReturn(userProfile);
		
		String accountNo = "1234123412341234";
		
		HirePurchaseProfile hirePurchaseProfile = new HirePurchaseProfile();
		hirePurchaseProfile.setId(1);
		hirePurchaseProfile.setAccountNo(accountNo);

		when(hirePurchaseProfileRepositoryMock.findById(accountId)).thenReturn(hirePurchaseProfile);
		
		InputStream is = getClass().getClassLoader().getResourceAsStream("GetHirePurchaseAccountDetailsLogic.json");
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

		when(getHirePurchaseAccountDetailsLogicMock.executeBusinessLogic(Mockito.any())).thenReturn(capsule);
		
		HirePurchaseDetails hirePurchaseDetails = (HirePurchaseDetails) hirePurchaseDetailsService.getHirePurchaseDetails(customerId, accountNo);
		logger.debug("    hirePurchaseDetails: " + hirePurchaseDetails);

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode dataNode = objectMapper.readTree(jsonStr);

		assertNotNull(hirePurchaseDetails);
		assertTrue(hirePurchaseDetails.getTotalOutstandingBalance().compareTo(dataNode.get("totalOutstandingBalance").decimalValue()) == 0);
		assertEquals(hirePurchaseDetails.getVehicleNo(), dataNode.get("vehicleNo").textValue());
		assertTrue(hirePurchaseDetails.getOverdueAmount().compareTo(dataNode.get("overdueAmount").decimalValue()) == 0);
		assertEquals(hirePurchaseDetails.getPaymentDueDate(), dataNode.get("paymentDueDate").textValue());
		assertTrue(hirePurchaseDetails.getMonthlyPayment().compareTo(dataNode.get("monthlyPayment").decimalValue()) == 0);
		assertTrue(hirePurchaseDetails.getLoanAmount().compareTo(dataNode.get("loanAmount").decimalValue()) == 0);
		assertEquals(hirePurchaseDetails.getTypeOfTerm(), dataNode.get("typeOfTerm").textValue());
		assertEquals(hirePurchaseDetails.getOriginalTenure().intValue(), dataNode.get("originalTenure").intValue());
		assertEquals(hirePurchaseDetails.getRemainingTenure().intValue(), dataNode.get("remainingTenure").intValue());
		assertTrue(hirePurchaseDetails.getInterestRate().compareTo(dataNode.get("interestRate").decimalValue()) == 0);
		assertTrue(hirePurchaseDetails.getOverdueInterest().compareTo(dataNode.get("overdueInterest").decimalValue()) == 0);
	}

	@Test(expected = CommonException.class)
	public void getHirePurchaseDetailsTest_notFound() throws Exception {
		logger.debug("getHirePurchaseDetailsTest_notFound()");

		when(hirePurchaseProfileRepositoryMock.findById(Mockito.anyInt())).thenReturn(null);

		int customerId = 1;
		String accountNo = "1";

		hirePurchaseDetailsService.getHirePurchaseDetails(customerId, accountNo);
	}
}
