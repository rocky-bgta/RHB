package com.rhbgroup.dcpbo.customer.service.impl;

import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.rhbgroup.dcpbo.customer.audit.collector.AdditionalDataHolder;
import com.rhbgroup.dcpbo.customer.model.McaTermData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.junit4.SpringRunner;

import com.rhbgroup.dcp.adaptor.BusinessAdaptor;
import com.rhbgroup.dcp.data.entity.profiles.UserProfile;
import com.rhbgroup.dcp.data.repository.ProfileRepository;
import com.rhbgroup.dcp.model.Capsule;
import com.rhbgroup.dcp.util.JsonUtil;
import com.rhbgroup.dcpbo.customer.exception.CommonException;
import com.rhbgroup.dcpbo.customer.model.McaTermDetails;
import com.rhbgroup.dcpbo.customer.service.McaTermService;
import com.rhbgroup.dcpbo.customer.vo.GetMcaTermLogicRequestVo;
import com.rhbgroup.dcpbo.customer.vo.McaTermsVo;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { McaTermServiceImpl.class, McaTermServiceImplTest.Config.class, AdditionalDataHolder.class})
public class McaTermServiceImplTest {

	private static Logger LOGGER = LogManager.getLogger();

	@Autowired
	McaTermService mcaTermService;

	@MockBean(name = "mcaTermDetailsLogic")
	BusinessAdaptor getMcaTermDetailsLogic;
	
	@MockBean(name = "getMcaTermLogic")
	BusinessAdaptor getMcaTermLogic;

	@MockBean(name = "profileRepository")
	private ProfileRepository profileRepository;

	@TestConfiguration
	static class Config {
		@Bean
		@Primary
		public McaTermService getMcaTermService(
				@Qualifier("mcaTermDetailsLogic") BusinessAdaptor getMcaTermDetailsLogic,
				@Qualifier("getMcaTermLogic") BusinessAdaptor getMcaTermLogic) {
			return new McaTermServiceImpl(getMcaTermDetailsLogic, getMcaTermLogic);
		}
	}

	@Test
	public void getMcaTermDetailTest_capsuleSuccessful() throws Exception {
		UserProfile userProfile = new UserProfile();
		userProfile.setId(1);
		userProfile.setCisNo("1234");
		BDDMockito.given(profileRepository.getUserProfileByUserId(1)).willReturn(userProfile);
		getMcaTermDetailTest(true);
	}

	@Test(expected = CommonException.class)
	public void getMcaTermDetailTest_capsuleUnSuccessful() throws Exception {
		UserProfile userProfile = new UserProfile();
		userProfile.setId(1);
		userProfile.setCisNo("1234");
		BDDMockito.given(profileRepository.getUserProfileByUserId(1)).willReturn(null);
		getMcaTermDetailTest(false);
	}

	public void getMcaTermDetailTest(boolean capsuleSuccessful) throws Exception {
		LOGGER.debug("mcaTermService: {}", mcaTermService);
		LOGGER.debug("getMcaTermDetailsLogic: {}", getMcaTermDetailsLogic);
		LOGGER.debug("profileRepository: {}", profileRepository);
		Integer customerId = 1;
		String accountNo = "123";
		String referenceNo = "456";
		String currencyCode = "MYR";
		Capsule capsule = getDCPLogicResponse(capsuleSuccessful, "GetMcaTermDetailsLogic.json");

		when(getMcaTermDetailsLogic.executeBusinessLogic(Mockito.any())).thenReturn(capsule);

		McaTermDetails mcaTermDetails = (McaTermDetails) mcaTermService.getMcaTermDetails(customerId, accountNo,
				referenceNo, currencyCode);
		LOGGER.debug("mcaTermDetails: {}", JsonUtil.objectToJson(mcaTermDetails));
	}
	
	
	//GETMCATERM TEST [START]
	
	@Test
	public void getMcaTermTest_capsuleSuccessful() throws Exception {
		UserProfile userProfile = new UserProfile();
		userProfile.setId(1);
		userProfile.setCisNo("1234");
		BDDMockito.given(profileRepository.getUserProfileByUserId(1)).willReturn(userProfile);
		getMcaTermTest(true);
	}

	@Test(expected = CommonException.class)
	public void getMcaTermTest_capsuleUnSuccessful() throws Exception {
		UserProfile userProfile = new UserProfile();
		userProfile.setId(1);
		userProfile.setCisNo("1234");
		BDDMockito.given(profileRepository.getUserProfileByUserId(1)).willReturn(null);
		getMcaTermTest(false);
	}

	public void getMcaTermTest(boolean capsuleSuccessful) throws Exception {
		LOGGER.debug("mcaTermService: {}", mcaTermService);
		LOGGER.debug("getMcaTermLogic: {}", getMcaTermLogic);
		LOGGER.debug("profileRepository: {}", profileRepository);
		Integer customerId = 1;
		Capsule capsule = getDCPLogicResponse(capsuleSuccessful, "GetMcaTermLogic.json");

		when(getMcaTermLogic.executeBusinessLogic(Mockito.any())).thenReturn(capsule);
		
		GetMcaTermLogicRequestVo request = new GetMcaTermLogicRequestVo();
		List<String> currencyList = new ArrayList<String>();
		currencyList.add("CNY");
		currencyList.add("MYR");
		currencyList.add("GBP");
		currencyList.add("SGD");
		request.setAccountNo("1234567890");
		request.setCurrencyCode(currencyList);

		McaTermsVo mcaTerm = (McaTermsVo) mcaTermService.getMcaTerm(customerId, request);
		LOGGER.debug("mcaTermDetails: {}", JsonUtil.objectToJson(mcaTerm));
	}
	
	//GETMCATERM TEST [END]

	private Capsule getDCPLogicResponse(boolean capsuleSuccessful, String resource) throws IOException {
		InputStream is = getClass().getClassLoader().getResourceAsStream(resource);
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		StringBuilder sbld = new StringBuilder();
		String line = null;
		while ((line = br.readLine()) != null)
			sbld.append(line);
		br.close();
		is.close();

		String jsonStr = sbld.toString();

		Capsule capsule = new Capsule();
		capsule.updateCurrentMessage(jsonStr);
		capsule.setOperationSuccess(capsuleSuccessful);
		return capsule;
	}

}
