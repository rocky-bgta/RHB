package com.rhbgroup.dcpbo.system.extractexchangerate.service.impl;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
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
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.junit4.SpringRunner;
import com.rhbgroup.dcpbo.common.exception.CommonException;
import com.rhbgroup.dcp.data.entity.profiles.UserProfile;
import com.rhbgroup.dcp.deposits.mca.bizlogic.GetMcaInterestRateLogic;
import com.rhbgroup.dcp.model.Capsule;
import com.rhbgroup.dcpbo.system.extractexchangerate.service.ExtractInterestExchangeRateService;
import com.rhbgroup.dcpbo.system.extractexchangerate.service.impl.dto.ExtractInterestExchangeRateRequest;
import com.rhbgroup.dcpbo.system.extractexchangerate.service.impl.dto.ExtractInterestExchangeRateResponse;
import com.rhbgroup.dcpbo.system.extractexchangerate.service.impl.dto.InterestRate;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {ExtractInterestExchangeRateServiceImpl.class, ExtractInterestExchangeRateServiceImplTests.Config.class})
public class ExtractInterestExchangeRateServiceImplTests {

	@Autowired
	ExtractInterestExchangeRateService extractInterestExchangeRateService;
	

	@MockBean
	GetMcaInterestRateLogic getMcaInterestRateLogic;

	private static Logger logger = LogManager.getLogger(ExtractInterestExchangeRateServiceImplTests.class);
	
	@TestConfiguration
	static class Config {
		@Bean
		@Primary
		public ExtractInterestExchangeRateService getExtractInterestExchangeRateService(
		        GetMcaInterestRateLogic getMcaInterestRateLogic) {
			return new ExtractInterestExchangeRateServiceImpl(getMcaInterestRateLogic);
		}
	}
	
	@Test
	public void getExtractInterestExchangeRateService_capsuleSuccessful() throws Throwable {
		getExtractInterestExchangeRateServiceTest(true);
	}

	@Test(expected = CommonException.class)
	public void getExtractInterestExchangeRateService_capsuleUnsuccessful() throws Throwable {
		getExtractInterestExchangeRateServiceTest(false);
	}
	
	@Test(expected = CommonException.class)
	public void getExtractInterestExchangeRateServiceTest_notFound() throws Exception {
		logger.debug("getExtractInterestExchangeRateServiceTest_notFound()");

		when(getMcaInterestRateLogic.executeBusinessLogic(Mockito.any())).thenReturn(null);

		ExtractInterestExchangeRateRequest request = new ExtractInterestExchangeRateRequest();
		
		extractInterestExchangeRateService.getInterestExchangeRate(request);
	}
	
	public void getExtractInterestExchangeRateServiceTest(boolean capsuleSuccessful) throws Throwable {
		logger.debug("getExtractInterestExchangeRateServiceTest()");
		logger.debug("    getMcaInterestRateLogic: " + getMcaInterestRateLogic);
		
		ExtractInterestExchangeRateRequest request = new ExtractInterestExchangeRateRequest();
		List<InterestRate> rates = new ArrayList<>();
		
		InterestRate rate = new InterestRate();
		rate.setCode("aud");
		rate.setTenure("01W");
		
		InterestRate rate1 = new InterestRate();
		rate1.setCode("bwd");
		rate1.setTenure("01W");
		
		rates.add(rate);
		rates.add(rate1);
		request.setRate(rates);
		
		InputStream is = getClass().getClassLoader().getResourceAsStream("GetMcaInterestRateLogic.json");
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
		when(getMcaInterestRateLogic.executeBusinessLogic(Mockito.any())).thenReturn(capsule);
		
		ExtractInterestExchangeRateResponse extractInterestExchangeRateResponse = (ExtractInterestExchangeRateResponse) extractInterestExchangeRateService.getInterestExchangeRate(request);
		logger.debug("    extractInterestExchangeRateResponse: " + extractInterestExchangeRateResponse);
		assertNotNull(extractInterestExchangeRateResponse);
	}
}
