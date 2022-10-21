package com.rhbgroup.dcpbo.customer.controller;

import static org.mockito.Mockito.when;

import com.rhbgroup.dcpbo.customer.dto.AuditEvent;
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

import com.rhbgroup.dcpbo.customer.client.ConfigErrorInterface;
import com.rhbgroup.dcpbo.customer.dto.AuditDetails;
import com.rhbgroup.dcpbo.customer.dto.AuditDetailsActivity;
import com.rhbgroup.dcpbo.customer.exception.CommonException;
import com.rhbgroup.dcpbo.customer.exception.CommonExceptionAdvice;
import com.rhbgroup.dcpbo.customer.model.response.BoExceptionResponse;
import com.rhbgroup.dcpbo.customer.service.AuditDetailsService;
import com.rhbgroup.dcpbo.customer.service.DcpCustomerAuditService;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest(classes = { AuditControllerTests.class, AuditControllerTests.Config.class,
		AuditController.class })
@EnableWebMvc
public class AuditControllerTests {
	@Autowired
	MockMvc mockMvc;
	
	@MockBean
	AuditDetailsService auditDetailsServiceMock;
	
	@MockBean
	DcpCustomerAuditService dcpCustomerAuditServiceMock;
	
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

	int auditId = 1;
	String eventCode = "10000";

	private static Logger logger = LogManager.getLogger(AuditControllerTests.class);

	@Test
	public void getAuditDetailsTest() throws Exception {
		logger.debug("getAuditDetailsTest()");
		logger.debug("    auditDetailsServiceMock: " + auditDetailsServiceMock);
		
		AuditDetails auditDetails = new AuditDetails();
		auditDetails.setAuditId(1);
		auditDetails.setEventCode("10001");
		auditDetails.setEventName("Transfer - IBG");
		auditDetails.setTimestamp("2017-03-14T00:00:12+08:00");
		auditDetails.setDeviceId("b21ded15-3bb0-4e50-bfec-dded6079a873");
		auditDetails.setChannel("DMB");
		auditDetails.setStatusCode("10000");
		auditDetails.setStatusDescription("successful");
		auditDetails.addDetails("Reference ID", "10001");
		auditDetails.addDetails("From Account Number", "10102468475712");
		auditDetails.addDetails("To Account Number", "10102394875710");
		
		AuditDetailsActivity auditDetailsActivity = new AuditDetailsActivity();
		auditDetailsActivity.setActivity(auditDetails);
		
		when(auditDetailsServiceMock.getAuditDetailsActivity(Mockito.anyInt(), Mockito.any())).thenReturn(auditDetailsActivity);

		String url = "/bo/cs/customer/audit/" + auditId + "/" + eventCode + "/details";
		logger.debug("    url: " + url);

		mockMvc.perform(MockMvcRequestBuilders.get(url))
				.andDo(MockMvcResultHandlers.print())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
				.andExpect(MockMvcResultMatchers.status().is2xxSuccessful());
	}

	@Test
	public void getAuditDetailsTest_notFound() throws Exception {
		logger.debug("getAuditDetailsTest_notFound()");
		logger.debug("    auditDetailsServiceMock: " + auditDetailsServiceMock);

		when(auditDetailsServiceMock.getAuditDetailsActivity(Mockito.anyInt(), Mockito.any())).thenThrow(new CommonException(CommonException.GENERIC_ERROR_CODE));
		
		String url = "/bo/cs/customer/audit/" + auditId + "/" + eventCode + "/details";
		logger.debug("    url: " + url);

		mockMvc.perform(MockMvcRequestBuilders.get(url))
				.andDo(MockMvcResultHandlers.print())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
				.andExpect(MockMvcResultMatchers.status().is5xxServerError());
	}

	@Test
	public void getListTest() throws Exception {
		logger.debug("getListTest()");
		logger.debug("    dcpCustomerAuditServiceMock: " + dcpCustomerAuditServiceMock);

		int customerId = 1;
		AuditEvent auditEvent = new AuditEvent();
		when(dcpCustomerAuditServiceMock.listing(Mockito.anyString(), Mockito.anyString(),   Mockito.anyInt(), Mockito.anyString(), Mockito.anyInt())).thenReturn(auditEvent);

		String url = "/bo/cs/customer/audit/list";
		logger.debug("    url: " + url);

		mockMvc.perform(MockMvcRequestBuilders.get(url).header("customerId", customerId))
				.andDo(MockMvcResultHandlers.print())
				.andExpect(MockMvcResultMatchers.content().contentType("application/xml;charset=UTF-8"))
				.andExpect(MockMvcResultMatchers.status().is2xxSuccessful());
	}
}
