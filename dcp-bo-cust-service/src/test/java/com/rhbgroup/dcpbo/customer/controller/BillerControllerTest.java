package com.rhbgroup.dcpbo.customer.controller;

import static org.mockito.Mockito.when;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.junit.Before;
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

import com.rhbgroup.dcpbo.customer.dto.BillerResponse;
import com.rhbgroup.dcpbo.customer.dto.TotalBillersResponse;
import com.rhbgroup.dcpbo.customer.service.BillerService;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest(classes = {BillerControllerTest.class, BillerController.class})
@EnableWebMvc
public class BillerControllerTest {
	
	@Autowired
	MockMvc mockMvc;

	@MockBean
	BillerService billerServiceMock;
	
	BillerResponse billerResponse;
	
	private static Logger logger = LogManager.getLogger(BillerControllerTest.class);
	
	@Before
	public void setup() {
		billerResponse = new BillerResponse();
		TotalBillersResponse totalBillers = new TotalBillersResponse();
		totalBillers.setTotal(70);
		billerResponse.setBiller(totalBillers);
	}
	
	@Test
	public void getBillerCountSuccessTest() throws Exception {
		String url = "/bo/biller/dashboard";
		logger.debug("    url: " + url);

		when(billerServiceMock.getBillerCount()).thenReturn(billerResponse);

		mockMvc.perform(MockMvcRequestBuilders.get(url)
                .contentType(MediaType.APPLICATION_JSON))
				.andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk());
	}

}
