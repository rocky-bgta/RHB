package com.rhbgroup.dcpbo.customer.controller;

import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
import com.rhbgroup.dcpbo.customer.model.DuitnowDetail;
import com.rhbgroup.dcpbo.customer.model.DuitnowEnquiryInfo;
import com.rhbgroup.dcpbo.customer.model.DuitnowProxyInfo;
import com.rhbgroup.dcpbo.customer.model.DuitnowSenderInfo;
import com.rhbgroup.dcpbo.customer.model.response.BoExceptionResponse;
import com.rhbgroup.dcpbo.customer.service.DuitnowEnquiryService;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest(classes = { DuitnowControllerTests.class, DuitnowControllerTests.Config.class,
		DuitnowController.class })
@EnableWebMvc
public class DuitnowControllerTests {
	@Autowired
	MockMvc mockMvc;

	@MockBean
	DuitnowEnquiryService duitnowEnquiryServiceMock;

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

	private static Logger logger = LogManager.getLogger(DuitnowControllerTests.class);

	@Test
	public void getDuitnowDetailsTest() throws Exception {
		logger.debug("getDuitnowDetailsTest()");
		logger.debug("duitnowEnquiryServiceMock: " + duitnowEnquiryServiceMock);

		int customerId = 1;
		DuitnowDetail response = new DuitnowDetail();
		DuitnowSenderInfo duitnowSenderInfo = new DuitnowSenderInfo();
		duitnowSenderInfo.setIdNumber("123456789012");
		duitnowSenderInfo.setIdRegStatus("ACTC");
		duitnowSenderInfo.setIdType("NRIC");
		duitnowSenderInfo.setOtpMobileNumber("+60123456789");
		response.setSenders(duitnowSenderInfo);
		DuitnowProxyInfo duitnowProxyInfo = new DuitnowProxyInfo();
		List<DuitnowEnquiryInfo> duitnow = new ArrayList<>();
		DuitnowEnquiryInfo duitnowEnquiryInfo = new DuitnowEnquiryInfo();
		duitnowEnquiryInfo.setRegistrationId("12345678901234567890123456789012345");
		duitnowEnquiryInfo.setIdType("PSPT");
		duitnowEnquiryInfo.setCountryName("MYS");
		duitnowEnquiryInfo.setIdVal("A12345678");
		duitnowEnquiryInfo.setStatus("ACTV");
		duitnowEnquiryInfo.setBic("RHBBMYKL");
		duitnowEnquiryInfo.setBankName("RHB Bank");
		duitnowEnquiryInfo.setBankAccount("1221314156");
		duitnowEnquiryInfo.setBankAcctType("CACC");
		duitnowEnquiryInfo.setBankAcctName("Current Account");
		duitnow.add(duitnowEnquiryInfo);

		duitnowEnquiryInfo = new DuitnowEnquiryInfo();
		duitnowEnquiryInfo.setRegistrationId("123457808822a1000");
		duitnowEnquiryInfo.setIdType("MBNO");
		duitnowEnquiryInfo.setCountryName(null);
		duitnowEnquiryInfo.setIdVal("01234567890");
		duitnowEnquiryInfo.setStatus("ACTV");
		duitnowEnquiryInfo.setBic("RHBBMYKL");
		duitnowEnquiryInfo.setBankName("RHB Bank");
		duitnowEnquiryInfo.setBankAccount("1221314156");
		duitnowEnquiryInfo.setBankAcctType("SAVG");
		duitnowEnquiryInfo.setBankAcctName("Savings Account");
		duitnow.add(duitnowEnquiryInfo);
		duitnowProxyInfo.setDuitNow(duitnow);
		response.setProxy(duitnowProxyInfo);
		when(duitnowEnquiryServiceMock.getDuitnowDetails(customerId)).thenReturn(response);

		mockMvc.perform(MockMvcRequestBuilders.get("/bo/cs/customer/duitnow").header("customerId", customerId))
				.andDo(MockMvcResultHandlers.print())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
				.andExpect(MockMvcResultMatchers.status().is2xxSuccessful());
	}

	@Test
	public void getDuitnowDetails_notFoundTest() throws Exception {

		int customerId = 1;

		when(duitnowEnquiryServiceMock.getDuitnowDetails(customerId)).thenThrow(
				new CommonException(CommonException.GENERIC_ERROR_CODE, "unit test", HttpStatus.BAD_REQUEST));

		mockMvc.perform(MockMvcRequestBuilders.get("/bo/cs/customer/duitnow").header("customerId", customerId))
				.andDo(MockMvcResultHandlers.print())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
				.andExpect(MockMvcResultMatchers.status().is4xxClientError());
	}
}
