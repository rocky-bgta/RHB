package com.rhbgroup.dcpbo.system.downtime.controller;

import static org.mockito.Mockito.when;

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
import com.rhbgroup.dcpbo.system.downtime.dto.DowntimeAdhoc;
import com.rhbgroup.dcpbo.system.downtime.service.DowntimeAdhocService;
import com.rhbgroup.dcpbo.system.downtime.vo.UpdateDowntimeAdhocRequestVo;
import com.rhbgroup.dcpbo.system.exception.AdhocDurationOverlappedException;
import com.rhbgroup.dcpbo.system.exception.DowntimeExceptionAdvice;



@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest(classes = { UpdateAdhocControllerTest.class, AdhocController.class})
@EnableWebMvc
public class UpdateAdhocControllerTest {
	
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
	
	private UpdateDowntimeAdhocRequestVo updateDowntimeAdhocRequestVo;

	private static Logger logger = LogManager.getLogger(UpdateAdhocControllerTest.class);
	
	private static final String requestBodyMissingEndTime = "{\n" +
			"    \"functionId\": 15,\n" +
			"    \"name\": \"Downtime adhoc 1\",\n" +
			"    \"adhocType\": \"System\",\n" +
			"    \"startTime\": \"2019-04-08T00:00:00+08:00\",\n" +
			"    \"isPushNotification\": false\n" +			
			"}";
	
	private static final String requestBodyMissingAdhocType = "{\n" +
			"    \"functionId\": 15,\n" +
			"    \"name\": \"Downtime adhoc 1\",\n" +
			"    \"startTime\": \"2019-04-08T00:00:00+08:00\",\n" +
			"    \"endTime\": \"2019-04-08T00:00:00+08:00\",\n" +
			"    \"isPushNotification\": false\n" +	
			"}";
	
	@Before
    public void setup(){

		updateDowntimeAdhocRequestVo = new UpdateDowntimeAdhocRequestVo();
		
		updateDowntimeAdhocRequestVo.setAdhocType("ADHOC");
		updateDowntimeAdhocRequestVo.setFunctionId(1);
		updateDowntimeAdhocRequestVo.setName("Service down");
		updateDowntimeAdhocRequestVo.setPushNotification(true);
		updateDowntimeAdhocRequestVo.setPushDate("2019-01-29");
		updateDowntimeAdhocRequestVo.setStartTime("2019-04-08T00:00:00+08:00");
		updateDowntimeAdhocRequestVo.setEndTime("2019-04-08T09:00:00+08:00");

    }

	@Test
	public void updateDowntimeAdhocRequireApprovalSuccessTest() throws Exception {
		logger.debug("updateDowntimeAdhocRequireApprovalSuccessTest()");
		
		String url = "/bo/system/downtime/adhoc/10";
		logger.debug("    url: " + url);
		
		DowntimeAdhoc downtimeAdhoc  = new DowntimeAdhoc();
		downtimeAdhoc.setApprovalId(1);
		ResponseEntity<BoData> responseEntity = new ResponseEntity<BoData>(downtimeAdhoc, HttpStatus.CREATED);
		
		when(downtimeAdhocServiceMock.updateDowntimeAdhoc(Mockito.anyObject(), Mockito.anyInt(), Mockito.anyString())).thenReturn(responseEntity);
		
		mockMvc.perform(MockMvcRequestBuilders.put(url)
				.header("userid","2")
                .contentType(MediaType.APPLICATION_JSON).content(asJsonString(updateDowntimeAdhocRequestVo)))
				.andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("approvalId", Matchers.is(1)));
	}
	
	@Test
	public void updateDowntimeAdhocNoApprovalRequireSuccessTest() throws Exception {
		logger.debug("updateDowntimeAdhocNoApprovalRequireSuccessTest()");
		
		String url = "/bo/system/downtime/adhoc/10";
		logger.debug("    url: " + url);
		
		DowntimeAdhoc downtimeAdhoc  = new DowntimeAdhoc();
		ResponseEntity<BoData> responseEntity = new ResponseEntity<BoData>(downtimeAdhoc, HttpStatus.OK);
		
		when(downtimeAdhocServiceMock.updateDowntimeAdhoc(Mockito.anyObject(), Mockito.anyInt(), Mockito.anyString())).thenReturn(responseEntity);
		
		mockMvc.perform(MockMvcRequestBuilders.put(url)
				.header("userid","2")
                .contentType(MediaType.APPLICATION_JSON).content(asJsonString(updateDowntimeAdhocRequestVo)))
				.andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("approvalId", Matchers.is(0)));
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void updateDowntimeAdhocRequireApprovalDuplicateFailTest() throws Exception {
		logger.debug("updateDowntimeAdhocRequireApprovalDuplicateFailTest()");
		
		String url = "/bo/system/downtime/adhoc/10";
		logger.debug("    url: " + url);
		
		when(downtimeAdhocServiceMock.updateDowntimeAdhoc(Mockito.anyObject(), Mockito.anyInt(), Mockito.anyString())).thenThrow(AdhocDurationOverlappedException.class);
		
		mockMvc.perform(MockMvcRequestBuilders.put(url)
				.header("userid","2")
                .contentType(MediaType.APPLICATION_JSON).content(asJsonString(updateDowntimeAdhocRequestVo)))
				.andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isForbidden());
	}
	
    public static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


}
