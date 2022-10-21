package com.rhbgroup.dcpbo.customer.controller;

import static org.mockito.Mockito.when;

import java.math.BigDecimal;

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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import com.rhbgroup.dcpbo.customer.client.ConfigErrorInterface;
import com.rhbgroup.dcpbo.customer.exception.CommonException;
import com.rhbgroup.dcpbo.customer.exception.CommonExceptionAdvice;
import com.rhbgroup.dcpbo.customer.model.McaTermCurrency;
import com.rhbgroup.dcpbo.customer.model.McaTermData;
import com.rhbgroup.dcpbo.customer.model.McaTermDetails;
import com.rhbgroup.dcpbo.customer.model.McaTermStatus;
import com.rhbgroup.dcpbo.customer.model.response.BoExceptionResponse;
import com.rhbgroup.dcpbo.customer.service.McaTermService;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest(classes = {McaTermControllerTests.class, McaTermControllerTests.Config.class, McaTermController.class})
@EnableWebMvc
public class McaTermControllerTests {
	@Autowired
	MockMvc mockMvc;

	@MockBean
	McaTermService mcaTermServiceMock;
	
	
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
	
	private static Logger logger = LogManager.getLogger(McaTermControllerTests.class);

	@Test
	public void getMcaTermDetailsTest() throws Exception {
		logger.debug("getMcaTermDetailsTest()");
		logger.debug("mcaTermServiceMock : " + mcaTermServiceMock);
		
		int customerId = 1;
		String accountNo = "123";
		String referenceNo = "456";
		String currencyCode = "MYR";
		
		McaTermDetails mcaTermDetails = new McaTermDetails();
		McaTermStatus mcaTermStatus = new McaTermStatus();
		mcaTermStatus.setCode("10000");
		mcaTermStatus.setDescription("Successful transaction");
		mcaTermStatus.setTitle("Hooray!");
		mcaTermStatus.setStatusType("success");
		mcaTermDetails.setStatus(mcaTermStatus);
		McaTermData mcaTermData = new McaTermData();
		mcaTermData.setAccountNo("1234556456");
		mcaTermData.setReferenceNo("20180627001");
		McaTermCurrency foreignCurrency = new McaTermCurrency();
		foreignCurrency.setCode("SGD");
		foreignCurrency.setBalance(new BigDecimal("1350.00"));
		mcaTermData.setForeignCurrency(foreignCurrency);
		
		McaTermCurrency localCurrency = new McaTermCurrency();
		localCurrency.setCode("MYR");
		localCurrency.setBalance(new BigDecimal("12421.30"));
		mcaTermData.setLocalCurrency(localCurrency);
		mcaTermData.setVisualPercentage(new BigDecimal("66"));
		mcaTermData.setPlacementDate("2018-02-01T17:00:00.000+08:00");
		mcaTermData.setMaturityDate("2019-02-01T17:00:00.000+08:00");
		mcaTermData.setInterestRate(new BigDecimal("1.21"));
		mcaTermData.setTenure(360);
		mcaTermData.setLastRenewalDate("2018-06-01T17:00:00.000+08:00");
		mcaTermData.setAccruedInterest(new BigDecimal("148.50"));
		mcaTermDetails.setData(mcaTermData);
		when(mcaTermServiceMock.getMcaTermDetails(customerId, accountNo, referenceNo, currencyCode)).thenReturn(mcaTermDetails);
		
		mockMvc.perform(MockMvcRequestBuilders.get("/bo/cs/customer/mca/"+accountNo+"/term/details/"+referenceNo+"?currency="+currencyCode).header("customerId", customerId))
				.andDo(MockMvcResultHandlers.print())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
				.andExpect(MockMvcResultMatchers.status().is2xxSuccessful());
	}

	@Test
	public void getMcaTermDetailsTest_notFound() throws Exception {
		logger.debug("getMcaTermDetailsTest_notFound()");
		logger.debug("mcaTermServiceMock : " + mcaTermServiceMock);
		
		int customerId = 1;
		String accountNo = "123";
		String referenceNo = "456";
		String currencyCode = "MYR";
		
		when(mcaTermServiceMock.getMcaTermDetails(customerId, accountNo, referenceNo, currencyCode)).thenThrow(new CommonException(CommonException.GENERIC_ERROR_CODE,"",HttpStatus.BAD_REQUEST));

		mockMvc.perform(MockMvcRequestBuilders.get("/bo/cs/customer/mca/"+accountNo+"/term/details/"+referenceNo+"?currency="+currencyCode).header("customerId", customerId))
				.andDo(MockMvcResultHandlers.print())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
				.andExpect(MockMvcResultMatchers.status().is4xxClientError());
	}
}
