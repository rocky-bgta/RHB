package com.rhbgroup.dcpbo.customer.controller;

import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import com.rhbgroup.dcp.util.JsonUtil;
import com.rhbgroup.dcpbo.customer.client.ConfigErrorInterface;
import com.rhbgroup.dcpbo.customer.exception.CommonException;
import com.rhbgroup.dcpbo.customer.exception.CommonExceptionAdvice;
import com.rhbgroup.dcpbo.customer.model.HirePurchaseDetails;
import com.rhbgroup.dcpbo.customer.model.response.BoExceptionResponse;
import com.rhbgroup.dcpbo.customer.service.HirePurchaseDetailsService;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest(classes = {HirePurchaseControllerTests.class, HirePurchaseControllerTests.Config.class, HirePurchaseController.class})
@EnableWebMvc
public class HirePurchaseControllerTests {
	@Autowired
	MockMvc mockMvc;
	
	@MockBean
	HirePurchaseDetailsService hirePurchaseDetailsServiceMock;
	
	@TestConfiguration
	static class Config {
		@Bean
		public CommonException getCommonException() {
			return new CommonException(CommonException.GENERIC_ERROR_CODE);
		}

		@Bean
		public CommonExceptionAdvice getCommonExceptionAdvice() {
			return new CommonExceptionAdvice();
		}
		
		@Bean
		public ConfigErrorInterface getConfigErrorInterface() {
			return new ConfigInterfaceImpl();
		}
	
		class ConfigInterfaceImpl implements ConfigErrorInterface {
			@Override
			public BoExceptionResponse getConfigError(String errorCode) {
				return new BoExceptionResponse(errorCode, "Not found !!!");
			}
		}
	}
	
	private static Logger logger = LogManager.getLogger(HirePurchaseControllerTests.class);

	@Test
	public void getHirePurchaseDetailsTest() throws Exception {
		logger.debug("getHirePurchaseDetailsTest()");
		logger.debug("    hirePurchaseDetailsServiceMock: " + hirePurchaseDetailsServiceMock);
		
		int customerId = 1;
		String accountNo = "1";
		logger.debug("    accountId: " + accountNo);
		
        InputStream is = getClass().getClassLoader().getResourceAsStream("GetHirePurchaseAccountDetailsLogic.json");
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        StringBuilder sbld = new StringBuilder();
        String line = null;
        while ((line = br.readLine()) != null)
                sbld.append(line);
        br.close();
        is.close();

        String jsonStr = sbld.toString();
        logger.debug("    jsonStr: " + jsonStr);

        HirePurchaseDetails hirePurchaseDetails = JsonUtil.jsonToObject(jsonStr, HirePurchaseDetails.class);
        logger.debug("    hirePurchaseDetails: " + hirePurchaseDetails);
		
		when(hirePurchaseDetailsServiceMock.getHirePurchaseDetails(customerId, accountNo)).thenReturn(hirePurchaseDetails);
		
		mockMvc.perform(MockMvcRequestBuilders.get("/bo/cs/customer/loan/hp/" + accountNo + "/details").header("customerId", customerId))
				.andDo(MockMvcResultHandlers.print())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
				.andExpect(MockMvcResultMatchers.status().is2xxSuccessful());
	}

	@Test
	public void getHirePurchaseDetailsTest_notFound() throws Exception {
		logger.debug("getHirePurchaseDetailsTest_notFound()");
		logger.debug("    hirePurchaseDetailsServiceMock: " + hirePurchaseDetailsServiceMock);
		
		int customerId = 1;
		String accountNo = "1";
		
		when(hirePurchaseDetailsServiceMock.getHirePurchaseDetails(customerId, accountNo)).thenThrow(new CommonException(CommonException.GENERIC_ERROR_CODE));

		mockMvc.perform(MockMvcRequestBuilders.get("/bo/cs/customer/loan/hp/" + accountNo + "/details").header("customerId", customerId))
				.andDo(MockMvcResultHandlers.print())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
				.andExpect(MockMvcResultMatchers.status().is5xxServerError());
	}
}
