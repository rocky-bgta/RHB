package com.rhbgroup.dcpbo.customer.controller;

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
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
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
import com.rhbgroup.dcpbo.customer.model.McaCallTransactions;
import com.rhbgroup.dcpbo.customer.model.response.BoExceptionResponse;
import com.rhbgroup.dcpbo.customer.service.McaCallTransactionsService;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest(classes = {McaControllerTests.class, McaControllerTests.Config.class, McaController.class})
@EnableWebMvc
public class McaControllerTests {
	@Autowired
	MockMvc mockMvc;
	
	@MockBean
	McaCallTransactionsService mcaCallTransactionsServiceMock;
	
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

	int customerId = 1;
	String accountNo = "1123";
	String foreignCurrency = "EUR";
	int pageCounter = 2;
	String firstKey = "03";
	String lastKey = "04";
	
	private static Logger logger = LogManager.getLogger(McaControllerTests.class);

	@Test
	public void getMcaCallTransactionsTest() throws Exception {
		logger.debug("getMcaCallTransactionsTest()");
		logger.debug("    mcaCallTransactionsServiceMock: " + mcaCallTransactionsServiceMock);
		logger.debug("    customerId: " + customerId);
		logger.debug("    accountNo: " + accountNo);
		logger.debug("    foreignCurrency: " + foreignCurrency);
		logger.debug("    pageCounter: " + pageCounter);
		logger.debug("    firstKey: " + firstKey);
		logger.debug("    lastKey: " + lastKey);
		
        InputStream is = getClass().getClassLoader().getResourceAsStream("GetMcaCallTransactionsLogic.json");
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        StringBuilder sbld = new StringBuilder();
        String line = null;
        while ((line = br.readLine()) != null)
                sbld.append(line);
        br.close();
        is.close();

        String jsonStr = sbld.toString();
        logger.debug("    jsonStr: " + jsonStr);

        McaCallTransactions asbDetails = JsonUtil.jsonToObject(jsonStr, McaCallTransactions.class);
        logger.debug("    asbDetails: " + asbDetails);
		
		when(mcaCallTransactionsServiceMock.getMcaCallTransactions(
				customerId, accountNo, foreignCurrency, pageCounter, firstKey, lastKey)).thenReturn(asbDetails);
		
		String url = "/bo/cs/customer/mca/" + accountNo + "/call/transactions/?foreignCurrency=" + foreignCurrency
				+ "&pageCounter=" + pageCounter + "&firstKey=" + firstKey + "&lastKey=" + lastKey;
		logger.debug("    url: " + url);

		mockMvc.perform(MockMvcRequestBuilders.get(url).header("customerId", customerId))
				.andDo(MockMvcResultHandlers.print())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
				.andExpect(MockMvcResultMatchers.status().is2xxSuccessful());
	}

	@Test
	public void getMcaCallTransactionsTest_notFound() throws Exception {
		logger.debug("getMcaCallTransactionsTest_notFound()");
		logger.debug("    mcaCallTransactionsServiceMock: " + mcaCallTransactionsServiceMock);
		logger.debug("    customerId: " + customerId);
		logger.debug("    accountNo: " + accountNo);
		logger.debug("    foreignCurrency: " + foreignCurrency);
		logger.debug("    pageCounter: " + pageCounter);
		logger.debug("    firstKey: " + firstKey);
		logger.debug("    lastKey: " + lastKey);
		
		when(mcaCallTransactionsServiceMock.getMcaCallTransactions(
				Mockito.anyInt(), Mockito.any(), Mockito.any(), Mockito.anyInt(), Mockito.any(), Mockito.any()))
				.thenThrow(new CommonException(CommonException.GENERIC_ERROR_CODE, "Record not found!!!", HttpStatus.INTERNAL_SERVER_ERROR));

		String url = "/bo/cs/customer/mca/" + accountNo + "/call/transactions/?foreignCurrency=" + foreignCurrency
				+ "&pageCounter=" + pageCounter + "&firstKey=" + firstKey + "&lastKey=" + lastKey;
		logger.debug("    url: " + url);

		mockMvc.perform(MockMvcRequestBuilders.get(url).header("customerId", customerId))
				.andDo(MockMvcResultHandlers.print())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
				.andExpect(MockMvcResultMatchers.status().is5xxServerError());
	}
}
