package com.rhbgroup.dcpbo.customer.controller;

import static org.mockito.Mockito.when;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.hamcrest.Matchers;
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

import com.rhbgroup.dcpbo.customer.dto.ResetUserNameResponse;
import com.rhbgroup.dcpbo.customer.service.ResetUserNameService;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest(classes = {ResetUserNameControllerTest.class, ResetUserNameController.class})
@EnableWebMvc
public class ResetUserNameControllerTest {
	
	@Autowired
	MockMvc mockMvc;

	@MockBean
	ResetUserNameService resetUserNameServiceMock;
	
	ResetUserNameResponse resetUserNameResponse;
	
	private static Logger logger = LogManager.getLogger(ResetUserNameControllerTest.class);
	
	@Before
	public void setup() {
		resetUserNameResponse = new ResetUserNameResponse();
		resetUserNameResponse.setStatusTitle("Username Successfully Reset");
		resetUserNameResponse.setStatusDesc("User can login to DCP now");
	}
	
	@Test
	public void resetUserNameTest() throws Exception {
		String id= "1";
		String code = "L";
		String url = "/bo/cs/customer/reset/" + id +"/" + code;
		System.out.println("url -"+url);
		logger.debug("    url: " + url);
		when(resetUserNameServiceMock.resetUserName(id, code)).thenReturn(resetUserNameResponse);

		mockMvc.perform(MockMvcRequestBuilders.put(url)
                .contentType(MediaType.APPLICATION_JSON))
				.andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk());
	}

}
