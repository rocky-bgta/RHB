package com.rhbgroup.dcpbo.customer.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
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
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rhbgroup.dcp.data.entity.profiles.UserProfile;
import com.rhbgroup.dcp.data.repository.ProfileRepository;
import com.rhbgroup.dcp.deposits.casa.bizlogic.GetAccountDetailsLogic;
import com.rhbgroup.dcp.eai.adaptors.accountinquiry.model.response.DcpAccountStatus;
import com.rhbgroup.dcp.eai.adaptors.accountinquiry.model.response.DcpDebitCard;
import com.rhbgroup.dcp.eai.adaptors.accountinquiry.model.response.DcpProduct;
import com.rhbgroup.dcp.model.Capsule;
import com.rhbgroup.dcpbo.customer.exception.CommonException;
import com.rhbgroup.dcpbo.customer.model.AccountDetails;
import com.rhbgroup.dcpbo.customer.model.CasaProfile;
import com.rhbgroup.dcpbo.customer.repository.CasaProfileRepository;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {CasaDetailsService.class})
public class CasaDetailsServiceTests {
	@Autowired
	CasaDetailsService casaDetailsService;

	@MockBean (name = "profileRepository")
	ProfileRepository profileRepositoryMock;
	
	@MockBean
	CasaProfileRepository casaProfileRepositoryMock;

	@MockBean
	GetAccountDetailsLogic getAccountDetailsLogicMock;
	
	private static Logger logger = LogManager.getLogger(CasaDetailsServiceTests.class);

	@Test
	public void getCasaDetailsTest_capsuleSuccessful() throws Exception {
		getCasaDetailsTest(true);
	}

	@Test(expected = CommonException.class)
	public void getCasaDetailsTest_capsuleUnsuccessful() throws Exception {
		getCasaDetailsTest(false);
	}

	public void getCasaDetailsTest(boolean capsuleSuccessful) throws Exception {
		logger.debug("getCasaDetailsTest()");
		logger.debug("    casaDetailsService: " + casaDetailsService);
		logger.debug("    profileRepositoryMock: " + profileRepositoryMock);
		logger.debug("    cardRepositoryMock: " + casaProfileRepositoryMock);
		
		int customerId = 1;
		int userId = customerId;
		int accountId = 1;
		
		String cisNo = "1234567890";
		String username = "ikhwan";
		String accountNo = "1234123412341234";
		
		UserProfile userProfile = new UserProfile();
		userProfile.setCisNo(cisNo);
		userProfile.setUsername(username);
		
		CasaProfile casaProfile = new CasaProfile();
		casaProfile.setId(1);
		casaProfile.setAccountNo(accountNo);

		when(profileRepositoryMock.getUserProfileByUserId(userId)).thenReturn(userProfile);
		when(casaProfileRepositoryMock.findById(accountId)).thenReturn(casaProfile);
		
		InputStream is = getClass().getClassLoader().getResourceAsStream("GetAccountDetailsLogic.json");
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

		when(getAccountDetailsLogicMock.executeBusinessLogic(Mockito.any())).thenReturn(capsule);
		
		AccountDetails casaDetails = (AccountDetails) casaDetailsService.getCasaDetails(customerId, accountNo);
		logger.debug("    casaDetails: " + casaDetails);

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode dataNode = objectMapper.readTree(jsonStr);

		assertNotNull(casaDetails);
		assertEquals(casaDetails.getNickname(), dataNode.get("nickname").textValue());
		assertEquals(casaDetails.getOwnershipType(), dataNode.get("ownershipType").textValue());
		assertEquals(casaDetails.getCurrentBalance(), dataNode.get("currentBalance").decimalValue());
		assertEquals(casaDetails.getAvailableBalance(), dataNode.get("availableBalance").decimalValue());
		assertEquals(casaDetails.getOverdraft(), dataNode.get("overdraft").decimalValue());
		assertEquals(casaDetails.getFloat1Day(), dataNode.get("float1Day").decimalValue());
		
		DcpProduct product = casaDetails.getProduct();
		assertNotNull(product);
		JsonNode productNode = dataNode.get("product");
		assertEquals(product.getCode(), productNode.get("code").textValue());
		assertEquals(product.getDescription(), productNode.get("description").textValue());
		
		DcpAccountStatus accountStatus = casaDetails.getAccountStatus();
		assertNotNull(accountStatus);
		JsonNode accountStatusNode = dataNode.get("accountStatus");
		assertEquals(accountStatus.getCode(), accountStatusNode.get("code").textValue());
		assertEquals(accountStatus.getDescription(), accountStatusNode.get("description").textValue());
		
		DcpDebitCard debitCard = casaDetails.getDebitCard();
		assertNotNull(debitCard);
		JsonNode debitCardNode = dataNode.get("debitCard");
		assertEquals(debitCard.getDebitCardNo(), debitCardNode.get("debitCardNo").textValue());
		assertEquals(debitCard.getCardType(), debitCardNode.get("cardType").textValue());
	}

	@Test(expected = CommonException.class)
	public void getCasaDetailsTest_notFound() throws Exception {
		logger.debug("getCasaDetailsTest_notFound()");

		when(profileRepositoryMock.getUserProfileByUserId(Mockito.anyInt())).thenReturn(null);

		int customerId = 1;
		String accountNo = "1";

		casaDetailsService.getCasaDetails(customerId, accountNo);
	}
}
