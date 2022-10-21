package com.rhbgroup.dcpbo.customer.service.impl;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.rhbgroup.dcpbo.customer.audit.collector.AdditionalDataHolder;
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

import com.rhbgroup.dcp.data.entity.profiles.UserProfile;
import com.rhbgroup.dcp.data.repository.ProfileRepository;
import com.rhbgroup.dcp.deposits.mca.bizlogic.GetDepositsMcaTransaction;
import com.rhbgroup.dcp.model.Capsule;
import com.rhbgroup.dcpbo.customer.dto.DepositMcaTransaction;
import com.rhbgroup.dcpbo.customer.exception.CommonException;
import com.rhbgroup.dcpbo.customer.service.McaCallTransactionsService;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { McaCallTransactionsServiceImpl.class, McaCallTransactionsServiceImplTests.Config.class, AdditionalDataHolder.class})
public class McaCallTransactionsServiceImplTests {
	@Autowired
	McaCallTransactionsService mcaCallTransactionsService;

	@MockBean
	ProfileRepository profileRepositoryMock;

	@MockBean
	GetDepositsMcaTransaction getDepositsMcaTransactionMock;

	@TestConfiguration
	static class Config {
		@Bean
		@Primary
		public GetDepositsMcaTransaction getGetDepositsMcaTransaction() {
			return new GetDepositsMcaTransaction();
		}
	}

	int customerId = 1;
	String accountNo = "123";
	String foreignCurrency = "EUR";
	String firstKey = "361";
	String lastKey = "359";
	int pageCounter = 1;

	private static Logger logger = LogManager.getLogger(McaCallTransactionsServiceImplTests.class);

	@Test
	public void getCardTransactionsTest_capsuleSuccessful() throws Throwable {
		getMcaCallTransactionsTest(true);
	}

	@Test(expected = CommonException.class)
	public void getCardTransactionsTest_capsuleUnsuccessful() throws Throwable {
		getMcaCallTransactionsTest(false);
	}

	public void getMcaCallTransactionsTest(boolean capsuleSuccessful) throws Throwable {
		logger.debug("getMcaCallTransactionsTest()");
		logger.debug("    cardTransactionsService: " + mcaCallTransactionsService);
		logger.debug("    profileRepositoryMock: " + profileRepositoryMock);
		logger.debug("    getDepositsMcaTransactionMock: " + getDepositsMcaTransactionMock);

		UserProfile userProfile = new UserProfile();
		userProfile.setCisNo("1234567890");
		userProfile.setUsername("ikhwan");
		when(profileRepositoryMock.getUserProfileByUserId(Mockito.anyInt())).thenReturn(userProfile);

		InputStream is = getClass().getClassLoader().getResourceAsStream("GetDepositsMcaTransaction.json");
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
		when(getDepositsMcaTransactionMock.executeBusinessLogic(Mockito.any())).thenReturn(capsule);

		DepositMcaTransaction depositMcaTransaction = (DepositMcaTransaction) mcaCallTransactionsService
				.getMcaCallTransactions(customerId, accountNo, foreignCurrency, pageCounter, firstKey, lastKey);
		logger.debug("    depositMcaTransaction: " + depositMcaTransaction);
		assertNotNull(depositMcaTransaction);
	}

	@Test(expected = CommonException.class)
	public void getCardTransactionsTest_notFound() throws Exception {
		logger.debug("getMcaCallTransactionsTest_notFound()");

		when(getDepositsMcaTransactionMock.executeBusinessLogic(Mockito.any())).thenReturn(null);

		mcaCallTransactionsService.getMcaCallTransactions(customerId, accountNo, foreignCurrency, pageCounter, firstKey,
				lastKey);
	}
}
