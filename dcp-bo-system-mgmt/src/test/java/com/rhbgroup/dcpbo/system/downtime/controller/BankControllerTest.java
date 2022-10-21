package com.rhbgroup.dcpbo.system.downtime.controller;

import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.hamcrest.Matchers;
import org.junit.Before;
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
import com.rhbgroup.dcpbo.common.exception.CommonException;
import com.rhbgroup.dcpbo.common.exception.CommonExceptionAdvice;
import com.rhbgroup.dcpbo.common.exception.ConfigErrorInterface;
import com.rhbgroup.dcpbo.common.exception.ErrorResponse;
import com.rhbgroup.dcpbo.system.downtime.dto.BankDetails;
import com.rhbgroup.dcpbo.system.downtime.dto.BankDetailsInfo;
import com.rhbgroup.dcpbo.system.downtime.service.BankService;
import com.rhbgroup.dcpbo.system.exception.DowntimeExceptionAdvice;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest(classes = { BankControllerTest.class, BankController.class })
@EnableWebMvc
public class BankControllerTest {

	public static final int PAGE_SIZE = 15;

	@Autowired
	MockMvc mockMvc;

	@MockBean
	BankService bankServiceMock;

	@TestConfiguration
	static class Config {
		@Bean
		public CommonException getCommonException() {
			return new CommonException(CommonException.GENERIC_ERROR_CODE, "NA");
		}

		@Bean
		public DowntimeExceptionAdvice getDowntimeExceptionAdvice() {
			return new DowntimeExceptionAdvice();
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
			public ErrorResponse getConfigError(String errorCode) {
				return new ErrorResponse(errorCode, "Invalid date format");
			}
		}
	}

	BankDetailsInfo bankDetailsInfo;

	private static Logger logger = LogManager.getLogger(GetAdhocControllerTest.class);

	@Before
	public void setup() {

		bankDetailsInfo = new BankDetailsInfo();
		List<BankDetails> bankDetailsList = new ArrayList<BankDetails>();
		BankDetails bank = new BankDetails();
		bank.setId(16);
		bank.setName("Affin Bank Berhad");
		bank.setShortName("Affin Bank");
		bank.setIsIbg("ON");
		bank.setIsInstant("OFF");

		bankDetailsList.add(bank);
		bank = new BankDetails();

		bank.setId(26);
		bank.setName("Al-Rajhi Bank");
		bank.setShortName("ARB");
		bank.setIsIbg("ON");
		bank.setIsInstant("OFF");

		bankDetailsList.add(bank);
		bankDetailsInfo.setBank(bankDetailsList);

	}

	@Test
	public void getBankPaymentTypeSuccessTest() throws Exception {
		String url = "/bo/bank";
		logger.debug("    url: " + url);

		when(bankServiceMock.getBankPaymentType(Mockito.anyInt())).thenReturn(bankDetailsInfo);

		mockMvc.perform(MockMvcRequestBuilders.get(url).contentType(MediaType.APPLICATION_JSON))
				.andDo(MockMvcResultHandlers.print()).andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("bank[0].id", Matchers.is(16)))
				.andExpect(MockMvcResultMatchers.jsonPath("bank[0].name", Matchers.is("Affin Bank Berhad")))
				.andExpect(MockMvcResultMatchers.jsonPath("bank[0].shortName", Matchers.is("Affin Bank")))
				.andExpect(MockMvcResultMatchers.jsonPath("bank[0].isIbg", Matchers.is("ON")))
				.andExpect(MockMvcResultMatchers.jsonPath("bank[0].isInstant", Matchers.is("OFF")))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("bank[1].id", Matchers.is(26)))
				.andExpect(MockMvcResultMatchers.jsonPath("bank[1].name", Matchers.is("Al-Rajhi Bank")))
				.andExpect(MockMvcResultMatchers.jsonPath("bank[1].shortName", Matchers.is("ARB")))
				.andExpect(MockMvcResultMatchers.jsonPath("bank[1].isIbg", Matchers.is("ON")))
				.andExpect(MockMvcResultMatchers.jsonPath("bank[1].isInstant", Matchers.is("OFF")));

	}

	@Test
	public void getBankPaymentTypeWithAllParametersSuccessTest() throws Exception {
		String url = "/bo/bank"
				+ "?pageNo=2";
		logger.debug("    url: " + url);

		when(bankServiceMock.getBankPaymentType(2)).thenReturn(bankDetailsInfo);

		mockMvc.perform(MockMvcRequestBuilders.get(url).contentType(MediaType.APPLICATION_JSON))
				.andDo(MockMvcResultHandlers.print()).andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("bank[0].id", Matchers.is(16)))
				.andExpect(MockMvcResultMatchers.jsonPath("bank[0].name", Matchers.is("Affin Bank Berhad")))
				.andExpect(MockMvcResultMatchers.jsonPath("bank[0].shortName", Matchers.is("Affin Bank")))
				.andExpect(MockMvcResultMatchers.jsonPath("bank[0].isIbg", Matchers.is("ON")))
				.andExpect(MockMvcResultMatchers.jsonPath("bank[0].isInstant", Matchers.is("OFF")))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("bank[1].id", Matchers.is(26)))
				.andExpect(MockMvcResultMatchers.jsonPath("bank[1].name", Matchers.is("Al-Rajhi Bank")))
				.andExpect(MockMvcResultMatchers.jsonPath("bank[1].shortName", Matchers.is("ARB")))
				.andExpect(MockMvcResultMatchers.jsonPath("bank[1].isIbg", Matchers.is("ON")))
				.andExpect(MockMvcResultMatchers.jsonPath("bank[1].isInstant", Matchers.is("OFF")));

	}

	public static String asJsonString(final Object obj) {
		try {
			return new ObjectMapper().writeValueAsString(obj);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
