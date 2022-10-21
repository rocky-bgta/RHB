package com.rhbgroup.dcpbo.customer.service.impl;

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
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.junit4.SpringRunner;

import com.rhbgroup.dcp.creditcards.bizlogic.GetCreditCardTransactionsLogic;
import com.rhbgroup.dcp.data.entity.profiles.UserProfile;
import com.rhbgroup.dcp.data.repository.ProfileRepository;
import com.rhbgroup.dcp.model.Capsule;
import com.rhbgroup.dcpbo.customer.dto.CardTransactions;
import com.rhbgroup.dcpbo.customer.exception.CommonException;
import com.rhbgroup.dcpbo.customer.model.CardProfile;
import com.rhbgroup.dcpbo.customer.repository.CardRepository;
import com.rhbgroup.dcpbo.customer.service.CardTransactionsService;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {CardTransactionsServiceImpl.class, CardTransactionsServiceImplTests.Config.class})
public class CardTransactionsServiceImplTests {
	@Autowired
	CardTransactionsService cardTransactionsService;

	@MockBean
	ProfileRepository profileRepositoryMock;

	@MockBean
	CardRepository cardRepositoryMock;

	@MockBean
	GetCreditCardTransactionsLogic getCreditCardTransactionsLogicMock;
	
	@TestConfiguration
	static class Config {
		@Bean
		@Primary
		public GetCreditCardTransactionsLogic getGetCreditCardTransactionsLogic() {
			return new GetCreditCardTransactionsLogic();
		}
	}

	int customerId = 1;
	String accountNo = "123";
	String firstKey = "361";
	String lastKey = "359";
	String pageCounter = "02";

	private static Logger logger = LogManager.getLogger(CardTransactionsServiceImplTests.class);

	@Test
	public void getCardTransactionsTest_capsuleSuccessful() throws Throwable {
		getCardTransactionsTest(true);
	}

	@Test(expected = CommonException.class)
	public void getCardTransactionsTest_capsuleUnsuccessful() throws Throwable {
		getCardTransactionsTest(false);
	}

	public void getCardTransactionsTest(boolean capsuleSuccessful) throws Throwable {
		logger.debug("getCardTransactionsTest()");
		logger.debug("    cardTransactionsService: " + cardTransactionsService);
		logger.debug("    profileRepositoryMock: " + profileRepositoryMock);
		logger.debug("    cardProfileRepositoryMock: " + cardRepositoryMock);
		logger.debug("    getCreditCardTransactionsLogicMock: " + getCreditCardTransactionsLogicMock);
		
        UserProfile userProfile = new UserProfile();
        userProfile.setCisNo("1234567890");
        userProfile.setUsername("ikhwan");
		when(profileRepositoryMock.getUserProfileByUserId(Mockito.anyInt())).thenReturn(userProfile);
		
		CardProfile cardProfile = new CardProfile();
		cardProfile.setId(customerId);
		cardProfile.setCardNo("1234-5678-9012-3456");
		when(cardRepositoryMock.findById(Mockito.anyInt())).thenReturn(cardProfile);
		
		InputStream is = getClass().getClassLoader().getResourceAsStream("GetCreditCardTransactionsLogic.json");
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
		when(getCreditCardTransactionsLogicMock.executeBusinessLogic(Mockito.any())).thenReturn(capsule);
		
		CardTransactions cardTransactions = (CardTransactions) cardTransactionsService.getCardTransactions(customerId, accountNo, firstKey, lastKey, pageCounter);
		logger.debug("    cardTransactions: " + cardTransactions);
		assertNotNull(cardTransactions);
	}

	@Test(expected = CommonException.class)
	public void getCardTransactionsTest_notFound() throws Exception {
		logger.debug("getCardTransactionsTest_notFound()");

		when(getCreditCardTransactionsLogicMock.executeBusinessLogic(Mockito.any())).thenReturn(null);

		cardTransactionsService.getCardTransactions(customerId, accountNo, firstKey, lastKey, pageCounter);
	}
}
