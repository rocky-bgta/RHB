package com.rhbgroup.dcpbo.customer.service;

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

import com.rhbgroup.dcp.data.entity.profiles.UserProfile;
import com.rhbgroup.dcp.data.repository.ProfileRepository;
import com.rhbgroup.dcp.deposits.casa.bizlogic.GetDepositTransactionsLogic;
import com.rhbgroup.dcp.model.Capsule;
import com.rhbgroup.dcpbo.customer.dto.CasaTransactions;
import com.rhbgroup.dcpbo.customer.exception.CommonException;
import com.rhbgroup.dcpbo.customer.model.CasaProfile;
import com.rhbgroup.dcpbo.customer.repository.CasaProfileRepository;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {CasaTransactionsService.class, CasaTransactionsServiceTests.Config.class})
public class CasaTransactionsServiceTests {
	@Autowired
	CasaTransactionsService casaTransactionsService;

	@MockBean
	ProfileRepository profileRepositoryMock;

	@MockBean
	CasaProfileRepository casaProfileRepositoryMock;

	@MockBean
	GetDepositTransactionsLogic getDepositTransactionsLogicMock;
	
	@TestConfiguration
	static class Config {
		@Bean
		@Primary
		public GetDepositTransactionsLogic getGetDepositTransactionsLogic() {
			return new GetDepositTransactionsLogic();
		}
	}

	int customerId = 1;
	int accountId = 1;
	String firstKey = "361";
	String lastKey = "359";
	int pageCounter = 2;


	private static Logger logger = LogManager.getLogger(CasaTransactionsServiceTests.class);

	@Test
	public void getCasaTransactionsTest_capsuleSuccessful() throws Throwable {
		getCasaTransactionsTest(true);
	}

	@Test(expected = CommonException.class)
	public void getCasaTransactionsTest_capsuleUnsuccessful() throws Throwable {
		getCasaTransactionsTest(false);
	}

	public void getCasaTransactionsTest(boolean capsuleSuccessful) throws Throwable {
		logger.debug("getCasaTransactionsTest()");
		logger.debug("    casaTransactionsService: " + casaTransactionsService);
		logger.debug("    profileRepositoryMock: " + profileRepositoryMock);
		logger.debug("    casaProfileRepositoryMock: " + casaProfileRepositoryMock);
		logger.debug("    getDepositTransactionsLogicMock: " + getDepositTransactionsLogicMock);
		
        UserProfile userProfile = new UserProfile();
        userProfile.setCisNo("1234567890");
        userProfile.setUsername("ikhwan");
		when(profileRepositoryMock.getUserProfileByUserId(Mockito.anyInt())).thenReturn(userProfile);
		
		CasaProfile casaProfile = new CasaProfile();
		casaProfile.setId(customerId);
		casaProfile.setAccountNo("1234-5678-9012-3456");
		when(casaProfileRepositoryMock.findById(Mockito.anyInt())).thenReturn(casaProfile);
		
		InputStream is = getClass().getClassLoader().getResourceAsStream("GetDepositTransactionsLogic.json");
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
		when(getDepositTransactionsLogicMock.executeBusinessLogic(Mockito.any())).thenReturn(capsule);
		
		CasaTransactions casaTransactions = (CasaTransactions) casaTransactionsService.getCasaTransactions(customerId, casaProfile.getAccountNo(), firstKey, lastKey, pageCounter);
		logger.debug("    casaTransactions: " + casaTransactions);
		assertNotNull(casaTransactions);
	}

	@Test(expected = CommonException.class)
	public void getCasaTransactionsTest_notFound() throws Exception {
		logger.debug("getCasaTransactionsTest_notFound()");

		String accountNo = "1";

		when(getDepositTransactionsLogicMock.executeBusinessLogic(Mockito.any())).thenReturn(null);

		casaTransactionsService.getCasaTransactions(customerId, accountNo, firstKey, lastKey, pageCounter);
	}
}
