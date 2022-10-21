package com.rhbgroup.dcpbo.customer.controller;

import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.hamcrest.Matchers;
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

import com.rhbgroup.dcp.eai.adaptors.accountinquiry.model.response.DcpAccountStatus;
import com.rhbgroup.dcp.eai.adaptors.accountinquiry.model.response.DcpDebitCard;
import com.rhbgroup.dcp.eai.adaptors.accountinquiry.model.response.DcpProduct;
import com.rhbgroup.dcpbo.customer.client.ConfigErrorInterface;
import com.rhbgroup.dcpbo.customer.exception.CommonException;
import com.rhbgroup.dcpbo.customer.exception.CommonExceptionAdvice;
import com.rhbgroup.dcpbo.customer.model.AccountDetails;
import com.rhbgroup.dcpbo.customer.model.response.BoExceptionResponse;
import com.rhbgroup.dcpbo.customer.service.CasaDetailsService;
import com.rhbgroup.dcpbo.customer.service.CasaTransactionsService;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest(classes = {CasaControllerTests.class, CasaControllerTests.Config.class, CasaController.class})
@EnableWebMvc
public class CasaControllerTests {
	@Autowired
	MockMvc mockMvc;

	@MockBean
	CasaDetailsService casaDetailsServiceMock;
	
	@MockBean
	CasaTransactionsService casaTransactionsServiceMock;
	
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
	
	private static Logger logger = LogManager.getLogger(CasaControllerTests.class);

	@Test
	public void getCasaDetailsTest() throws Exception {
		logger.debug("getCasaDetailsTest()");
		logger.debug("    casaDetailsServiceMock: " + casaDetailsServiceMock);
		
		int customerId = 1;
		String accountNo = "123";
		AccountDetails response = new AccountDetails();
		response.setNickname("My housing fund");
		response.setOwnershipType("IND");
		response.setCurrentBalance(new BigDecimal(12000.99));
		response.setAvailableBalance(new BigDecimal(1000.1));
		response.setOverdraft(new BigDecimal(2000.1));
		response.setFloat1Day(new BigDecimal(1000.2));
		
		DcpProduct product = new DcpProduct();
		product.setCode("100");
		product.setDescription("Ordinary Savings Account");
		response.setProduct(product);
		
		DcpAccountStatus accountStatus = new DcpAccountStatus();
		accountStatus.setCode("00");
		accountStatus.setDescription("Active");
		response.setAccountStatus(accountStatus);
		
		DcpDebitCard debitCard = new DcpDebitCard();
		debitCard.setDebitCardNo("4677000012340000");
		debitCard.setCardType("MASTER");
		response.setDebitCard(debitCard);
		
		when(casaDetailsServiceMock.getCasaDetails(customerId, accountNo)).thenReturn(response);
		
		mockMvc.perform(MockMvcRequestBuilders.get("/bo/cs/customer/casa/" + accountNo + "/details").header("customerId", customerId))
				.andDo(MockMvcResultHandlers.print())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
				.andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
				.andExpect(MockMvcResultMatchers.jsonPath("nickname", Matchers.is("My housing fund")))
				.andExpect(MockMvcResultMatchers.jsonPath("ownershipType", Matchers.is("IND")))
				.andExpect(MockMvcResultMatchers.jsonPath("currentBalance", Matchers.comparesEqualTo(new BigDecimal(12000.99))))
				.andExpect(MockMvcResultMatchers.jsonPath("availableBalance", Matchers.comparesEqualTo(new BigDecimal(1000.1))))
				.andExpect(MockMvcResultMatchers.jsonPath("overdraft", Matchers.comparesEqualTo(new BigDecimal(2000.1))))
				.andExpect(MockMvcResultMatchers.jsonPath("float1Day", Matchers.comparesEqualTo(new BigDecimal(1000.2))))
				.andExpect(MockMvcResultMatchers.jsonPath("product.code", Matchers.is("100")))
				.andExpect(MockMvcResultMatchers.jsonPath("product.description", Matchers.is("Ordinary Savings Account")))
				.andExpect(MockMvcResultMatchers.jsonPath("accountStatus.code", Matchers.is("00")))
				.andExpect(MockMvcResultMatchers.jsonPath("accountStatus.description", Matchers.is("Active")))
				.andExpect(MockMvcResultMatchers.jsonPath("debitCard.debitCardNo", Matchers.is("4677000012340000")))
				.andExpect(MockMvcResultMatchers.jsonPath("debitCard.cardType", Matchers.is("MASTER")));
	}

	@Test
	public void getCasaDetailsTest_notFound() throws Exception {
		logger.debug("getCasaDetailsTest_notFound()");
		logger.debug("    casaDetailsServiceMock: " + casaDetailsServiceMock);
		
		int customerId = 1;
		String accountNo = "123";
		
		when(casaDetailsServiceMock.getCasaDetails(customerId, accountNo)).thenThrow(new CommonException(CommonException.GENERIC_ERROR_CODE));

		mockMvc.perform(MockMvcRequestBuilders.get("/bo/cs/customer/casa/" + accountNo + "/details").header("customerId", customerId))
				.andDo(MockMvcResultHandlers.print())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
				.andExpect(MockMvcResultMatchers.status().is5xxServerError());
	}
}
