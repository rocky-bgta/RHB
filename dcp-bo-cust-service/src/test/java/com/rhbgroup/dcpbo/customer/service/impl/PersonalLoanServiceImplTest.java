package com.rhbgroup.dcpbo.customer.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;

import com.rhbgroup.dcpbo.customer.contract.BoData;
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
import com.rhbgroup.dcp.loans.bizlogic.GetPersonalFinanceAccountDetailsLogic;
import com.rhbgroup.dcp.model.Capsule;
import com.rhbgroup.dcpbo.customer.exception.CommonException;
import com.rhbgroup.dcpbo.customer.model.AsbProfile;
import com.rhbgroup.dcpbo.customer.repository.AsbProfileRepository;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { CommonException.class, ProfileRepository.class, PersonalLoanServiceImpl.class,
		AsbProfileRepository.class, PersonalLoanServiceImplTest.Config.class })
public class PersonalLoanServiceImplTest {

	private static Logger logger = LogManager.getLogger(PersonalLoanServiceImplTest.class);
	
	@Autowired
	PersonalLoanServiceImpl mockPersonalLoanService;

	@MockBean
	AsbProfileRepository asbProfileRepositoryMock;

	@MockBean
	GetPersonalFinanceAccountDetailsLogic getPersonalFinanceAccountDetailsLogic;

	@MockBean
	ProfileRepository profileRepositoryMock;

	@TestConfiguration
	static class Config {
		@Bean
		@Primary
		public GetPersonalFinanceAccountDetailsLogic getPersonalFinanceAccountDetailsLogic() {
			return new GetPersonalFinanceAccountDetailsLogic();
		}
	}

	@Test
	public void getPersonalLoanDetailsTest() throws Throwable {

		int customerId = 1;
		int accountId = 1;
		String accountNo = "1234123412341234";

		AsbProfile asbProfile = new AsbProfile();
		asbProfile.setId(1);
		asbProfile.setAccountNo(accountNo);

		when(asbProfileRepositoryMock.findById(accountId)).thenReturn(asbProfile);

		InputStream is = getClass().getClassLoader().getResourceAsStream("GetPersonalFinanceAccountDetailsLogic.json");
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		StringBuilder sbld = new StringBuilder();
		String line = null;

		while ((line = br.readLine()) != null) {
			sbld.append(line);
		}

		br.close();
		is.close();

		String jsonStr = sbld.toString();
		logger.debug("jsonStr: " + jsonStr);

		Capsule capsule = new Capsule();
		capsule.updateCurrentMessage(jsonStr);

		when(getPersonalFinanceAccountDetailsLogic.executeBusinessLogic(Mockito.any())).thenReturn(capsule);

		UserProfile userProfile = new UserProfile();
		userProfile.setId(1);
		userProfile.setCisNo("1");

		when(profileRepositoryMock.getUserProfileByUserId(customerId)).thenReturn(userProfile);

		BoData resultStr = mockPersonalLoanService.getPersonalLoanDetails(customerId, accountNo);
		logger.debug("resultStr: " + resultStr);
		assertNotNull(resultStr);

		ObjectMapper objectMapper = new ObjectMapper();
		JsonNode inputNode = objectMapper.readTree(jsonStr);
		JsonNode outputNode = objectMapper.convertValue(resultStr, JsonNode.class);
		assertEquals("4500.00",outputNode.get("remainingAmount").textValue());
		assertEquals("255.00",outputNode.get("overdueAmount").textValue());
		assertEquals("2018-02-02T00:00:00.000+08:00", outputNode.get("paymentDueDate").textValue());
		assertEquals("200.00",outputNode.get("monthlyPayment").textValue());
		assertEquals("5000.00",outputNode.get("loanAmount").textValue());
		assertEquals("M",outputNode.get("typeOfTerm").textValue());
		assertEquals("124",outputNode.get("originalTenure").textValue());
		assertEquals("100",outputNode.get("remainingTenure").textValue());
		assertEquals("5.12",outputNode.get("interestRate").textValue());
		assertEquals(inputNode.size(), outputNode.size());
	}

	@Test(expected = CommonException.class)
	public void getPersonalLoanDetailsTest_notFound() throws CommonException {
		logger.debug("getPersonalLoanDetailsTest_notFound()");

		when(asbProfileRepositoryMock.findById(Mockito.anyInt())).thenReturn(null);

		int customerId = 1;
		String accountNo = "123";

		UserProfile userProfile = new UserProfile();
		userProfile.setId(1);
		userProfile.setCisNo("1");

		when(profileRepositoryMock.getUserProfileByUserId(customerId)).thenReturn(null);

		mockPersonalLoanService.getPersonalLoanDetails(customerId, accountNo);
	}

}
