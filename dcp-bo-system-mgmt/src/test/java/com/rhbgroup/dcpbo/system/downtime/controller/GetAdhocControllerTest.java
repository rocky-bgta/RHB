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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
import com.rhbgroup.dcpbo.system.common.BoData;
import com.rhbgroup.dcpbo.system.downtime.dto.Adhoc;
import com.rhbgroup.dcpbo.system.downtime.dto.AdhocCategory;
import com.rhbgroup.dcpbo.system.downtime.dto.AdhocData;
import com.rhbgroup.dcpbo.system.downtime.dto.AdhocType;
import com.rhbgroup.dcpbo.system.downtime.service.DowntimeAdhocService;
import com.rhbgroup.dcpbo.system.exception.DowntimeExceptionAdvice;



@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest(classes = { GetAdhocControllerTest.class, AdhocController.class})
@EnableWebMvc
public class GetAdhocControllerTest {
	
	public static final int PAGE_SIZE = 15;
	
	@Autowired
	MockMvc mockMvc;
	
	@MockBean
	DowntimeAdhocService downtimeAdhocServiceMock;
	
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
	
	private AdhocData adhocData;
	private AdhocType adhocType;

	private ResponseEntity<?> response;
	private AdhocCategory adhocCategory;

	private static Logger logger = LogManager.getLogger(GetAdhocControllerTest.class);
	
	@Before
    public void setup(){
		
		adhocData = new AdhocData();
		List<Adhoc> adhocs = new ArrayList<Adhoc>();
		Adhoc adhoc = new Adhoc();
		adhoc.setId(1);
		adhoc.setAdhocType("ADHOC");
		adhoc.setName("Service down 1");
		adhoc.setIsPushNotification(true);
		adhoc.setPushDate("2019-01-29");
		adhoc.setStartTime("2019-01-29T21:29:25+08:00");
		adhoc.setEndTime("2019-01-29T23:29:25+08:00");
		adhoc.setStatus("Active");
		adhocs.add(adhoc);
		adhoc = new Adhoc();
		adhoc.setId(2);
		adhoc.setAdhocType("ADHOC");
		adhoc.setName("Service down 2");
		adhoc.setIsPushNotification(true);
		adhoc.setPushDate("2019-03-18");
		adhoc.setStartTime("2019-03-18T21:29:25+08:00");
		adhoc.setEndTime("2019-03-18T23:29:25+08:00");
		adhoc.setStatus("Inactive");
		adhocs.add(adhoc);
		adhocData.setAdhoc(adhocs);

		List<String> adhocCategoryList = new ArrayList();
		adhocCategory = new AdhocCategory();

		adhocCategoryList.add("System");
		adhocCategoryList.add("Internal");
		adhocCategoryList.add("External");

		adhocCategory.setAdhocCategory(adhocCategoryList);

		List<String> adhocTypeList = new ArrayList();
		adhocType = new AdhocType();

		adhocTypeList.add("IBG");
		adhocTypeList.add("Instant Transfer");
		
		adhocType.setAdhocType(adhocTypeList);

	}

	@Test
	public void getDowntimeAdhocListSuccessTest() throws Exception {
		String url = "/bo/system/downtime/adhoc";
		logger.debug("    url: " + url);
		
		when(downtimeAdhocServiceMock.getDowntimeAdhocs(Mockito.anyInt(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(adhocData);
		
		mockMvc.perform(MockMvcRequestBuilders.get(url)
                .contentType(MediaType.APPLICATION_JSON))
				.andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("adhoc[0].id", Matchers.is(1)))
        		.andExpect(MockMvcResultMatchers.jsonPath("adhoc[0].name", Matchers.is("Service down 1")))
        		.andExpect(MockMvcResultMatchers.jsonPath("adhoc[0].startTime", Matchers.is("2019-01-29T21:29:25+08:00")))
        		.andExpect(MockMvcResultMatchers.jsonPath("adhoc[0].endTime", Matchers.is("2019-01-29T23:29:25+08:00")))
        		.andExpect(MockMvcResultMatchers.jsonPath("adhoc[0].isPushNotification", Matchers.is(true)))
        		.andExpect(MockMvcResultMatchers.jsonPath("adhoc[0].pushDate", Matchers.is("2019-01-29")))
        		.andExpect(MockMvcResultMatchers.jsonPath("adhoc[0].status", Matchers.is("Active")))
        		.andExpect(MockMvcResultMatchers.jsonPath("adhoc[0].adhocType", Matchers.is("ADHOC")))        		
                .andExpect(MockMvcResultMatchers.jsonPath("adhoc[1].id", Matchers.is(2)))
        		.andExpect(MockMvcResultMatchers.jsonPath("adhoc[1].name", Matchers.is("Service down 2")))
        		.andExpect(MockMvcResultMatchers.jsonPath("adhoc[1].startTime", Matchers.is("2019-03-18T21:29:25+08:00")))
        		.andExpect(MockMvcResultMatchers.jsonPath("adhoc[1].endTime", Matchers.is("2019-03-18T23:29:25+08:00")))
        		.andExpect(MockMvcResultMatchers.jsonPath("adhoc[1].isPushNotification", Matchers.is(true)))
        		.andExpect(MockMvcResultMatchers.jsonPath("adhoc[1].pushDate", Matchers.is("2019-03-18")))
        		.andExpect(MockMvcResultMatchers.jsonPath("adhoc[1].status", Matchers.is("Inactive")))
        		.andExpect(MockMvcResultMatchers.jsonPath("adhoc[1].adhocType", Matchers.is("ADHOC")));

	}
	
	@Test
	public void getDowntimeAdhocListWithAllParametersSuccessTest() throws Exception {
		String url = "/bo/system/downtime/adhoc" + "?startTime=2019-10-01T00:00:00+08:00&endTime=2019-11-06T00:00:00+08:00&status=Active,Inactive&adhocCategory=System&pageNo=2";
		logger.debug("    url: " + url);
		
		when(downtimeAdhocServiceMock.getDowntimeAdhocs(2, "2019-10-01T00:00:00+08:00", "2019-11-06T00:00:00+08:00", "System", "Active,Inactive")).thenReturn(adhocData);
		
		mockMvc.perform(MockMvcRequestBuilders.get(url)
                .contentType(MediaType.APPLICATION_JSON))
				.andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("adhoc[0].id", Matchers.is(1)))
        		.andExpect(MockMvcResultMatchers.jsonPath("adhoc[0].name", Matchers.is("Service down 1")))
        		.andExpect(MockMvcResultMatchers.jsonPath("adhoc[0].startTime", Matchers.is("2019-01-29T21:29:25+08:00")))
        		.andExpect(MockMvcResultMatchers.jsonPath("adhoc[0].endTime", Matchers.is("2019-01-29T23:29:25+08:00")))
        		.andExpect(MockMvcResultMatchers.jsonPath("adhoc[0].isPushNotification", Matchers.is(true)))
        		.andExpect(MockMvcResultMatchers.jsonPath("adhoc[0].pushDate", Matchers.is("2019-01-29")))
        		.andExpect(MockMvcResultMatchers.jsonPath("adhoc[0].status", Matchers.is("Active")))
        		.andExpect(MockMvcResultMatchers.jsonPath("adhoc[0].adhocType", Matchers.is("ADHOC")))        		
                .andExpect(MockMvcResultMatchers.jsonPath("adhoc[1].id", Matchers.is(2)))
        		.andExpect(MockMvcResultMatchers.jsonPath("adhoc[1].name", Matchers.is("Service down 2")))
        		.andExpect(MockMvcResultMatchers.jsonPath("adhoc[1].startTime", Matchers.is("2019-03-18T21:29:25+08:00")))
        		.andExpect(MockMvcResultMatchers.jsonPath("adhoc[1].endTime", Matchers.is("2019-03-18T23:29:25+08:00")))
        		.andExpect(MockMvcResultMatchers.jsonPath("adhoc[1].isPushNotification", Matchers.is(true)))
        		.andExpect(MockMvcResultMatchers.jsonPath("adhoc[1].pushDate", Matchers.is("2019-03-18")))
        		.andExpect(MockMvcResultMatchers.jsonPath("adhoc[1].status", Matchers.is("Inactive")))
        		.andExpect(MockMvcResultMatchers.jsonPath("adhoc[1].adhocType", Matchers.is("ADHOC")));

	}
	
	@Test
	public void getAdhocTypeListSuccessTest() throws Exception {
		logger.debug("getAdhocTypeListSuccessTest()");

		String url = "/bo/system/downtime/adhoc/adhocType";
		logger.debug("    url: " + url);

		when(downtimeAdhocServiceMock.getAdhocTypesList( Mockito.anyString())).thenReturn(adhocType);

		mockMvc.perform(MockMvcRequestBuilders.get(url).contentType(MediaType.APPLICATION_JSON))
		.andDo(MockMvcResultHandlers.print())
		.andExpect(MockMvcResultMatchers.status().isOk())
		.andExpect(MockMvcResultMatchers.jsonPath("adhocType[0]", Matchers.is("IBG")))
		.andExpect(MockMvcResultMatchers.status().isOk())
		.andExpect(MockMvcResultMatchers.jsonPath("adhocType[1]", Matchers.is("Instant Transfer")));
		
	}
	
	@Test
	public void getAdhocCategoryListSuccessTest() throws Exception {
		logger.debug("getAdhocCategoryListSuccessTest()");

		String url = "/bo/system/downtime/adhoc/adhocCategory";
		logger.debug("    url: " + url);

		ResponseEntity<BoData> responseEntity = new ResponseEntity<BoData>(adhocCategory, HttpStatus.OK);
		
		when(downtimeAdhocServiceMock.getAdhocCategoryList()).thenReturn(responseEntity);


		mockMvc.perform(MockMvcRequestBuilders.get(url)
                .contentType(MediaType.APPLICATION_JSON))
				.andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk());

		
	}


	public static String asJsonString(final Object obj) {
		try {
			return new ObjectMapper().writeValueAsString(obj);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}


}
