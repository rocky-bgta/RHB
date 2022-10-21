package com.rhbgroup.dcpbo.system.extractexchangerate.service;

import com.rhbgroup.dcp.deposits.mca.bizlogic.GetMcaRateLogic;
import com.rhbgroup.dcp.model.Capsule;
import com.rhbgroup.dcpbo.common.exception.CommonException;
import com.rhbgroup.dcpbo.system.common.BoData;
import com.rhbgroup.dcpbo.system.extractexchangerate.service.impl.ExtractExchangeRateServiceImpl;
import com.rhbgroup.dcpbo.system.extractexchangerate.service.impl.dto.ExtractExchangeRateResponse;
import com.rhbgroup.dcpbo.system.extractexchangerate.service.impl.dto.Rate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.junit.Ignore;
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

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { ExtractExchangeRateService.class, ExtractExchangeRateServiceTest.Config.class })
public class ExtractExchangeRateServiceTest {

	@Autowired
	ExtractExchangeRateService extractExchangeRateService;
	
	@MockBean
	GetMcaRateLogic getMcaRateLogic;
	
	private static Logger logger = LogManager.getLogger(ExtractExchangeRateServiceTest.class);

	private static final double DELTA = 1e-15;

	@TestConfiguration
	static class Config {
		@Bean
		@Primary
		public ExtractExchangeRateService getExtractExchangeRateService(
		        GetMcaRateLogic getMcaRateLogic) {
			return new ExtractExchangeRateServiceImpl(getMcaRateLogic);
		}
	}

	@Test
	@Ignore
	public void testGetExchangeRate() {
		BoData response = extractExchangeRateService.getExchangeRate("AUD");		
	}
	
	@Test
	public void getExtractExchangeRateService_capsuleSuccessful() throws Throwable {
		getExtractExchangeRateServiceTest(true);
	}

	@Test(expected = CommonException.class)
	public void getExtractInterestExchangeRateService_capsuleUnsuccessful() throws Throwable {
		getExtractExchangeRateServiceTest(false);
	}
	
	@Test(expected = CommonException.class)
	public void getExtractExchangeRateServiceTest_notFound() throws Exception {
		logger.debug("getExtractExchangeRateServiceTest_notFound()");

		when(getMcaRateLogic.executeBusinessLogic(Mockito.any())).thenReturn(null);
		
		extractExchangeRateService.getExchangeRate("AUD");
	}
	
	public void getExtractExchangeRateServiceTest(boolean capsuleSuccessful) throws Throwable {
		logger.debug("getExtractExchangeRateServiceTest()");
		logger.debug("    getMcaRateLogic: " + getMcaRateLogic);
		
		InputStream is = getClass().getClassLoader().getResourceAsStream("GetMcaRateLogic.json");
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
		when(getMcaRateLogic.executeBusinessLogic(Mockito.any())).thenReturn(capsule);
		
		ExtractExchangeRateResponse extractExchangeRateResponse = (ExtractExchangeRateResponse) extractExchangeRateService.getExchangeRate("AUD");
		logger.debug("    extractExchangeRateResponse: " + extractExchangeRateResponse);
		assertNotNull(extractExchangeRateResponse);
	}
}
