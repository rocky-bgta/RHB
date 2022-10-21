package com.rhbgroup.dcpbo.user.search;

import static org.mockito.Mockito.when;

import com.rhbgroup.dcpbo.user.info.model.bo.User;
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
import org.springframework.web.util.NestedServletException;

import java.util.ArrayList;
import java.util.List;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest(classes = {SearchControllerTests.class, SearchController.class})
@EnableWebMvc
public class SearchControllerTests {
	@Autowired
	MockMvc mockMvc;
	
	@MockBean
	SearchService searchServiceMock;

	private static Logger logger = LogManager.getLogger(SearchControllerTests.class);

	@Test
	public void searchTest() throws Exception {
		logger.debug("searchTest()");
		logger.debug("    searchServiceMock: " + searchServiceMock);
		
		SearchResult searchResult = new SearchResult();
		User user = new User();
		List<User> userList = new ArrayList<>();
		userList.add(user);
		searchResult.setUser(userList);
    	when(searchServiceMock.search(Mockito.anyString(), Mockito.anyInt(), Mockito.any(),
    			Mockito.anyString(), Mockito.anyString(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(searchResult);
		
		String url = "/bo/user/search?keyword=%man%&pageNum=1&department=1&status=A&userGroup=1"
				+ "&fromDate=2017-03-14T00:00:00+08:00&toDate=2017-04-15T01:00:00+08:00";
		logger.debug("    url: " +  url);

		mockMvc.perform(MockMvcRequestBuilders.get(url))
				.andDo(MockMvcResultHandlers.print())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
				.andExpect(MockMvcResultMatchers.status().is2xxSuccessful());
	}


	@Test
	public void searchTest_emptyValues() throws Exception {
		logger.debug("searchTest_emptyValues()");
		logger.debug("    searchServiceMock: " + searchServiceMock);
		
		SearchResult searchResult = new SearchResult();
		User user = new User();
		List<User> userList = new ArrayList<>();
		userList.add(user);
		searchResult.setUser(userList);
    	when(searchServiceMock.search(Mockito.anyString(), Mockito.anyInt(), Mockito.any(),
    			Mockito.anyString(), Mockito.anyString(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(searchResult);
		
		String url = "/bo/user/search?";
		logger.debug("    url: " +  url);

		mockMvc.perform(MockMvcRequestBuilders.get(url))
				.andDo(MockMvcResultHandlers.print())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
				.andExpect(MockMvcResultMatchers.status().is2xxSuccessful());
	}

	@Test(expected = NestedServletException.class)
	public void searchTest_invalidTimestamp() throws Exception {
		logger.debug("searchTest_invalidTimestamp()");

		String url = "/bo/user/search?keyword=%man%&pageNum=1&department=1&status=A&userGroup=1"
				+ "&fromDate=2017ABC-03-14T00:00:00+08:00&toDate=2017ABC-04-15T01:00:00+08:00";
		logger.debug("    url: " +  url);

		mockMvc.perform(MockMvcRequestBuilders.get(url));
	}
}
