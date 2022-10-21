package com.rhbgroup.dcpbo.customer.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

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
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.rhbgroup.dcp.creditcards.bizlogic.GetCreditCardDetailsLogic;
import com.rhbgroup.dcp.creditcards.model.DcpSuppCard;
import com.rhbgroup.dcp.data.entity.profiles.UserProfile;
import com.rhbgroup.dcp.data.repository.ProfileRepository;
import com.rhbgroup.dcp.model.Capsule;
import com.rhbgroup.dcpbo.customer.contract.BoData;
import com.rhbgroup.dcpbo.customer.exception.CommonException;
import com.rhbgroup.dcpbo.customer.model.CardProfile;
import com.rhbgroup.dcpbo.customer.model.CreditCardDetails;
import com.rhbgroup.dcpbo.customer.repository.CardRepository;
import com.rhbgroup.dcpbo.customer.service.impl.CardDetailsServiceImpl;
import com.rhbgroup.dcpbo.customer.dto.CreditCardDetailsRequest;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {CardDetailsServiceImpl.class, CardDetailsServiceTests.Config.class})
public class CardDetailsServiceTests {
	@Autowired
	CardDetailsService cardDetailsService;

	@MockBean (name = "profileRepository")
	ProfileRepository profileRepositoryMock;
	
	@MockBean
	CardRepository cardRepositoryMock;

	@MockBean
	GetCreditCardDetailsLogic getCreditCardDetailsLogicMock;

	@TestConfiguration
	static class Config {
		@Bean
		public GetCreditCardDetailsLogic getCreditCardDetailsLogic() {
			return new GetCreditCardDetailsLogic();
		}
	}
	
	private static Logger logger = LogManager.getLogger(CardDetailsServiceTests.class);

	@Test
	public void getCardDetailsTest() throws Exception {
		logger.debug("getCardDetailsTest()");
		logger.debug("    cardDetailsService: " + cardDetailsService);
		logger.debug("    profileRepositoryMock: " + profileRepositoryMock);
		logger.debug("    cardRepositoryMock: " + cardRepositoryMock);
		
		int customerId = 1;
		int userId = customerId;
		int cardId = 1;
		
		String cisNo = "1234567890";
		String username = "ikhwan";
		
		UserProfile userProfile = new UserProfile();
		userProfile.setCisNo(cisNo);
		userProfile.setUsername(username);
		
		CardProfile cardProfile = new CardProfile();
		cardProfile.setCardNo("4321123456789012");
		
		CreditCardDetailsRequest creditCardDetailsRequest = new CreditCardDetailsRequest();
		creditCardDetailsRequest.setCardNo("4363467191000003");
		creditCardDetailsRequest.setChannelFlag("D");
		creditCardDetailsRequest.setConnectorCode("PRIIND");
		creditCardDetailsRequest.setBlockCode("A");
		creditCardDetailsRequest.setAccountBlockCode("Q");

		when(profileRepositoryMock.getUserProfileByUserId(userId)).thenReturn(userProfile);
		when(cardRepositoryMock.findById(cardId)).thenReturn(cardProfile);
		
		InputStream is = getClass().getClassLoader().getResourceAsStream("GetCreditCardDetailsLogic.json");
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
		capsule.setOperationSuccess(true);

		when(getCreditCardDetailsLogicMock.executeBusinessLogic(Mockito.any())).thenReturn(capsule);
		
		CreditCardDetails cardDetails = (CreditCardDetails) cardDetailsService.getCardDetails(customerId, creditCardDetailsRequest.getCardNo(),
										creditCardDetailsRequest.getChannelFlag(),creditCardDetailsRequest.getConnectorCode(),
										creditCardDetailsRequest.getBlockCode(),creditCardDetailsRequest.getAccountBlockCode());
		logger.debug("    cardDetails: " + cardDetails);

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode dataNode = objectMapper.readTree(jsonStr);

		assertNotNull(cardDetails);
		assertEquals(cardDetails.getCardHolderName(), dataNode.get("cardHolderName").textValue());
		assertTrue(cardDetails.getOutstandingBalance().compareTo(dataNode.get("outstandingBalance").decimalValue()) == 0);
		assertEquals(cardDetails.getStatementBalance(), dataNode.get("statementBalance").decimalValue());
		assertEquals(cardDetails.getAvailableLimit(), dataNode.get("availableLimit").decimalValue());
		assertEquals(cardDetails.getAvailableCredit(), dataNode.get("availableCredit").decimalValue());
		assertEquals(cardDetails.getCreditLimit(), dataNode.get("creditLimit").decimalValue());
		assertEquals(cardDetails.getPaymentDueDate(), dataNode.get("paymentDueDate").textValue());
		assertEquals(cardDetails.getMinPaymentDue(), dataNode.get("minPaymentDue").decimalValue());
		assertEquals(cardDetails.getRewardPointBalance(), dataNode.get("rewardPointBalance").decimalValue());
		assertEquals(cardDetails.getEmbossedName(), dataNode.get("embossedName").textValue());
		assertEquals(cardDetails.getIsRewardPoint(), dataNode.get("isRewardPoint").asBoolean());

		List<DcpSuppCard> suppCardsList = cardDetails.getSuppCards();
		assertNotNull(suppCardsList);
		
		ArrayNode arrayNode = (ArrayNode) dataNode.get("suppCards");
		assertEquals(suppCardsList.size(), arrayNode.size());
		
		for (int i=0; i<arrayNode.size(); i++) {
			DcpSuppCard suppCard = suppCardsList.get(i);
			JsonNode suppCardNode = arrayNode.get(i);
			
			assertEquals(suppCard.getCardNo(), suppCardNode.get("cardNo").textValue());
			assertEquals(suppCard.getSuppCreditLimit(), suppCardNode.get("suppCreditLimit").decimalValue());
			assertEquals(suppCard.getOutstandingBalance(), suppCardNode.get("outstandingBalance").decimalValue());
			assertEquals(suppCard.getEmbossName(), suppCardNode.get("embossName").textValue());
			assertEquals(suppCard.getPaymentNetwork(), suppCardNode.get("paymentNetwork").textValue());
		}
	}

	@Test(expected = CommonException.class)
	public void getCardDetailsTest_notFound() throws Exception {
		logger.debug("getCardDetailsTest_notFound()");

		Capsule capsule = new Capsule();

		capsule.setOperationSuccess(false);

		when(getCreditCardDetailsLogicMock.executeBusinessLogic(Mockito.any())).thenReturn(capsule);

		int customerId = 1;
		String cardNo = "1";
		String channelFlag="D";
		String blockCode = "A";
		String connectorCode = "PRIIND";
		String accountBlockCode  = "Q";

		cardDetailsService.getCardDetails(customerId, cardNo, channelFlag, connectorCode, blockCode, accountBlockCode);

	}
	
	@Test
	public void getCardDetailsDuringEmptyFieldsTest() throws Exception {
		logger.debug("getCardDetailsTest()");
		logger.debug("    cardDetailsService: " + cardDetailsService);
		logger.debug("    profileRepositoryMock: " + profileRepositoryMock);
		logger.debug("    cardRepositoryMock: " + cardRepositoryMock);
		
		int customerId = 1;
		int userId = customerId;
		int cardId = 1;
		
		String cisNo = "1234567890";
		String username = "ikhwan";
		
		UserProfile userProfile = new UserProfile();
		userProfile.setCisNo(cisNo);
		userProfile.setUsername(username);
		
		CardProfile cardProfile = new CardProfile();
		cardProfile.setCardNo("4321123456789012");
		
		CreditCardDetailsRequest creditCardDetailsRequest = new CreditCardDetailsRequest();
		creditCardDetailsRequest.setCardNo("4363467191000003");
		creditCardDetailsRequest.setChannelFlag("D");
		creditCardDetailsRequest.setConnectorCode("PRIIND");
		creditCardDetailsRequest.setBlockCode("empty");
		creditCardDetailsRequest.setAccountBlockCode("empty");

		when(profileRepositoryMock.getUserProfileByUserId(userId)).thenReturn(userProfile);
		when(cardRepositoryMock.findById(cardId)).thenReturn(cardProfile);
		
		InputStream is = getClass().getClassLoader().getResourceAsStream("GetCreditCardDetailsLogic.json");
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
		capsule.setOperationSuccess(true);

		when(getCreditCardDetailsLogicMock.executeBusinessLogic(Mockito.any())).thenReturn(capsule);
		
		CreditCardDetails cardDetails = (CreditCardDetails) cardDetailsService.getCardDetails(customerId, creditCardDetailsRequest.getCardNo(),
										creditCardDetailsRequest.getChannelFlag(),creditCardDetailsRequest.getConnectorCode(),
										creditCardDetailsRequest.getBlockCode(),creditCardDetailsRequest.getAccountBlockCode());
		logger.debug("    cardDetails: " + cardDetails);

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode dataNode = objectMapper.readTree(jsonStr);

		assertNotNull(cardDetails);
		assertEquals(cardDetails.getCardHolderName(), dataNode.get("cardHolderName").textValue());
		assertTrue(cardDetails.getOutstandingBalance().compareTo(dataNode.get("outstandingBalance").decimalValue()) == 0);
		assertEquals(cardDetails.getStatementBalance(), dataNode.get("statementBalance").decimalValue());
		assertEquals(cardDetails.getAvailableLimit(), dataNode.get("availableLimit").decimalValue());
		assertEquals(cardDetails.getAvailableCredit(), dataNode.get("availableCredit").decimalValue());
		assertEquals(cardDetails.getCreditLimit(), dataNode.get("creditLimit").decimalValue());
		assertEquals(cardDetails.getPaymentDueDate(), dataNode.get("paymentDueDate").textValue());
		assertEquals(cardDetails.getMinPaymentDue(), dataNode.get("minPaymentDue").decimalValue());
		assertEquals(cardDetails.getRewardPointBalance(), dataNode.get("rewardPointBalance").decimalValue());
		assertEquals(cardDetails.getEmbossedName(), dataNode.get("embossedName").textValue());
		assertEquals(cardDetails.getIsRewardPoint(), dataNode.get("isRewardPoint").asBoolean());

		List<DcpSuppCard> suppCardsList = cardDetails.getSuppCards();
		assertNotNull(suppCardsList);
		
		ArrayNode arrayNode = (ArrayNode) dataNode.get("suppCards");
		assertEquals(suppCardsList.size(), arrayNode.size());
		
		for (int i=0; i<arrayNode.size(); i++) {
			DcpSuppCard suppCard = suppCardsList.get(i);
			JsonNode suppCardNode = arrayNode.get(i);
			
			assertEquals(suppCard.getCardNo(), suppCardNode.get("cardNo").textValue());
			assertEquals(suppCard.getSuppCreditLimit(), suppCardNode.get("suppCreditLimit").decimalValue());
			assertEquals(suppCard.getOutstandingBalance(), suppCardNode.get("outstandingBalance").decimalValue());
			assertEquals(suppCard.getEmbossName(), suppCardNode.get("embossName").textValue());
			assertEquals(suppCard.getPaymentNetwork(), suppCardNode.get("paymentNetwork").textValue());
		}
	}
}
