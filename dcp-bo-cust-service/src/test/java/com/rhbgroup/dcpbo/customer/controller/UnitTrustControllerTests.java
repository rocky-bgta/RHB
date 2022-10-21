package com.rhbgroup.dcpbo.customer.controller;

import static org.mockito.Mockito.when;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import com.rhbgroup.dcpbo.customer.model.UnitTrustDetails;
import com.rhbgroup.dcpbo.customer.service.UnitTrustDetailsService;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest(classes = {UnitTrustControllerTests.class, UnitTrustController.class})
@EnableWebMvc
public class UnitTrustControllerTests {
	@Autowired
	MockMvc mockMvc;

	@MockBean
	UnitTrustDetailsService unitTrustDetailsServiceMock;
	
	private static Logger logger = LogManager.getLogger(UnitTrustControllerTests.class);

	@Test
	public void getUnitTrustDetailsTest() throws Exception {
		logger.debug("getUnitTrustDetailsTest()");
		logger.debug("    unitTrustDetailsServiceMock: " + unitTrustDetailsServiceMock);
		
		String accountNo = "1234-567890";
		Integer customerId = 1;

		UnitTrustDetails unitTrustDetails = new UnitTrustDetails();
		
		when(unitTrustDetailsServiceMock.getUnitTrustDetails(accountNo, customerId)).thenReturn(unitTrustDetails);

		String url = "/bo/cs/customer/ut/" + accountNo + "/details";
		logger.debug("    url: " + url);
		
		mockMvc.perform(MockMvcRequestBuilders.get(url).header("customerId", customerId))
				.andDo(MockMvcResultHandlers.print())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
				.andExpect(MockMvcResultMatchers.status().is2xxSuccessful());
	}
}
