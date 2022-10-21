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
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rhbgroup.dcpbo.customer.client.ConfigErrorInterface;
import com.rhbgroup.dcpbo.customer.dto.CardTransactions;
import com.rhbgroup.dcpbo.customer.exception.CommonException;
import com.rhbgroup.dcpbo.customer.exception.CommonExceptionAdvice;
import com.rhbgroup.dcpbo.customer.model.response.BoExceptionResponse;
import com.rhbgroup.dcpbo.customer.service.CardDetailsService;
import com.rhbgroup.dcpbo.customer.service.CardTransactionsService;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest(classes = {CardTransactionsControllerTests.class, CardTransactionsControllerTests.Config.class, CardController.class})
@EnableWebMvc
public class CardTransactionsControllerTests {
	@Autowired
	MockMvc mockMvc;
	
	@MockBean
	CardTransactionsService cardTransactionsServiceMock;
	
	@MockBean
	CardDetailsService cardDetailsServiceMock;
	
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
	int cardId = 1;
	String firstKey = "361";
	String lastKey = "359";
	String pageCounter = "02";

	private static Logger logger = LogManager.getLogger(CardTransactionsControllerTests.class);

	@Test
	public void getCardTransactionsDetailsTest() throws Exception {
		logger.debug("getCardTransactionsDetailsTest()");
		logger.debug("    cardTransactionsServiceMock: " + cardTransactionsServiceMock);
		
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
        
        ObjectMapper objectMapper = new ObjectMapper();
		CardTransactions cardTransactions = objectMapper.readValue(jsonStr, CardTransactions.class);

		when(cardTransactionsServiceMock.getCardTransactions(Mockito.anyInt(), Mockito.anyString(), Mockito.any(),
				Mockito.any(), Mockito.any())).thenReturn(cardTransactions);

		String url = "/bo/cs/customer/card/" + cardId + "/transactions/?pageCounter=" + pageCounter + "&firstKey="
				+ firstKey + "&lastKey=" + lastKey;
		logger.debug("    url: " + url);

		mockMvc.perform(MockMvcRequestBuilders.get(url).header("customerId", customerId))
				.andDo(MockMvcResultHandlers.print())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
				.andExpect(MockMvcResultMatchers.status().is2xxSuccessful());
	}

	@Test
	public void getCardTransactionsDetailsTest_notFound() throws Exception {
		logger.debug("getCardTransactionsDetailsTest_notFound()");
		logger.debug("    cardTransactionsServiceMock: " + cardTransactionsServiceMock);
		
		int customerId = 1;
		
		when(cardTransactionsServiceMock.getCardTransactions(Mockito.anyInt(), Mockito.anyString(), Mockito.any(),
				Mockito.any(), Mockito.any())).thenThrow(new CommonException(CommonException.GENERIC_ERROR_CODE));

		String url = "/bo/cs/customer/card/" + cardId + "/transactions/?pageCounter=" + pageCounter + "&firstKey=" + firstKey + "&lastKey=" + lastKey;
		logger.debug("    url: " + url);

		mockMvc.perform(MockMvcRequestBuilders.get(url).header("customerId", customerId))
				.andDo(MockMvcResultHandlers.print())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
				.andExpect(MockMvcResultMatchers.status().is5xxServerError());
	}
}
