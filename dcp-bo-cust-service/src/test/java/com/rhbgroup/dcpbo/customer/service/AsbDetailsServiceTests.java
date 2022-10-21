package com.rhbgroup.dcpbo.customer.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.rhbgroup.dcp.data.repository.ProfileRepository;
import com.rhbgroup.dcpbo.customer.model.UserProfile;
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

import com.rhbgroup.dcp.loans.bizlogic.GetAsbLoanAccountDetailsLogic;
import com.rhbgroup.dcp.model.Capsule;
import com.rhbgroup.dcp.util.JsonUtil;
import com.rhbgroup.dcpbo.customer.exception.CommonException;
import com.rhbgroup.dcpbo.customer.model.AsbDetails;
import com.rhbgroup.dcpbo.customer.model.AsbProfile;
import com.rhbgroup.dcpbo.customer.repository.AsbProfileRepository;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {AsbDetailsService.class, AsbProfileRepository.class, AsbDetailsServiceTests.Config.class})
public class AsbDetailsServiceTests {
	@Autowired
	AsbDetailsService asbDetailsService;

	@MockBean
	AsbProfileRepository asbProfileRepositoryMock;

	@MockBean
	GetAsbLoanAccountDetailsLogic getAsbLoanAccountDetailsLogicMock;

	@MockBean
	ProfileRepository profileRepositoryMock;

	@TestConfiguration
	static class Config {
		@Bean
		@Primary
		public GetAsbLoanAccountDetailsLogic getGetAsbLoanAccountDetailsLogic() {
			return new GetAsbLoanAccountDetailsLogic();
		}
	}

	private static Logger logger = LogManager.getLogger(AsbDetailsServiceTests.class);

	@Test
	public void getAsbDetailsTest_capsuleSuccessful() throws Throwable {
		getAsbDetailsTest(true);
	}

	@Test(expected = CommonException.class)
	public void getAsbDetailsTest_capsuleUnsuccessful() throws Throwable {
		getAsbDetailsTest(false);
	}

	public void getAsbDetailsTest(boolean capsuleSuccessful) throws Throwable {
		logger.debug("getAsbDetailsTest()");
		logger.debug("    asbDetailsService: " + asbDetailsService);
		logger.debug("    asbRepositoryMock: " + asbProfileRepositoryMock);
		
		int customerId = 1;
		int accountId = 1;
		
		String accountNo = "1234123412341234";
		
		AsbProfile asbProfile = new AsbProfile();
		asbProfile.setId(1);
		asbProfile.setAccountNo(accountNo);

		when(profileRepositoryMock.getUserProfileByUserId(Mockito.any())).thenReturn(new com.rhbgroup.dcp.data.entity.profiles.UserProfile());

		when(asbProfileRepositoryMock.findById(accountId)).thenReturn(asbProfile);
		
		InputStream is = getClass().getClassLoader().getResourceAsStream("GetAsbLoanAccountDetailsLogic.json");
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

		when(getAsbLoanAccountDetailsLogicMock.executeBusinessLogic(Mockito.any())).thenReturn(capsule);
		
		AsbDetails asbDetails  = (AsbDetails) asbDetailsService.getAsbDetails(customerId, accountNo);
		logger.debug("    asbDetails: " + asbDetails);

        AsbDetails dataNode = JsonUtil.jsonToObject(jsonStr, AsbDetails.class);

		assertNotNull(asbDetails);
		assertEquals(asbDetails.getRemainingAmount(), dataNode.getRemainingAmount());
		assertEquals(asbDetails.getOverdueAmount(), dataNode.getOverdueAmount());
		assertEquals(asbDetails.getPaymentDueDate(), dataNode.getPaymentDueDate());
		assertEquals(asbDetails.getMonthlyPayment(), dataNode.getMonthlyPayment());
		assertEquals(asbDetails.getLoanAmount(), dataNode.getLoanAmount());
		assertEquals(asbDetails.getTypeOfTerm(), dataNode.getTypeOfTerm());
		assertEquals(asbDetails.getOriginalTenure(), dataNode.getOriginalTenure());
		assertEquals(asbDetails.getRemainingAmount(), dataNode.getRemainingAmount());
		assertEquals(asbDetails.getInterestRate(), dataNode.getInterestRate());
		assertEquals(asbDetails.getAccountOwnership(), dataNode.getAccountOwnership());
		assertEquals(asbDetails.getAccountHolderName(), dataNode.getAccountHolderName());
	}

	@Test(expected = CommonException.class)
	public void getAsbDetailsTest_notFound() throws Exception {
		logger.debug("getAsbDetailsTest_notFound()");

		Capsule capsule = new Capsule();

		capsule.setOperationSuccess(false);
		when(profileRepositoryMock.getUserProfileByUserId(Mockito.any())).thenReturn(new com.rhbgroup.dcp.data.entity.profiles.UserProfile());

		when(getAsbLoanAccountDetailsLogicMock.executeBusinessLogic(Mockito.any())).thenReturn(capsule);

		int customerId = 1;
		String accountId = "1";

		asbDetailsService.getAsbDetails(customerId, accountId);
	}
}
