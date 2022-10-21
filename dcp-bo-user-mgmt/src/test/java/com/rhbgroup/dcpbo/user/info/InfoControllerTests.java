package com.rhbgroup.dcpbo.user.info;

import static org.mockito.Mockito.when;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
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

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest(classes = {InfoControllerTests.class, InfoController.class})
@EnableWebMvc
public class InfoControllerTests {
	@Autowired
	MockMvc mockMvc;
	
	@MockBean(name = "userService")
	InfoService userServiceMock;

	private static Logger logger = LogManager.getLogger(InfoControllerTests.class);

	@Test
	public void getStaffIdTest() throws Exception {
		logger.debug("getStaffIdTest()");
		logger.debug("    userServiceMock: " + userServiceMock);
		
		UserInfo userInfo = new UserInfo();
		userInfo.setName("Mohd Ikhwan Haris");
		userInfo.setEmail("ikhwan@gmail.com");
		when(userServiceMock.getStaffId(Mockito.anyString())).thenReturn(userInfo);
		
		int staffId = 1;
		logger.debug("    staffId: " + staffId);
		
		String url = "/bo/user/staffid/" + staffId;
		logger.debug("    url: " +  url);

		mockMvc.perform(MockMvcRequestBuilders.get(url))
				.andDo(MockMvcResultHandlers.print())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
				.andExpect(MockMvcResultMatchers.status().is2xxSuccessful());
	}
}
