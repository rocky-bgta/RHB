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
import com.rhbgroup.dcpbo.system.downtime.vo.DeleteDowntimeAdhocRequestVo;
import com.rhbgroup.dcpbo.system.exception.DeleteAdhocNotAllowedException;
import com.rhbgroup.dcpbo.system.exception.DowntimeExceptionAdvice;
import com.rhbgroup.dcpbo.system.exception.PendingApprovalException;



@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest(classes = { DeleteAdhocControllerTest.class, AdhocController.class})
@EnableWebMvc
public class DeleteAdhocControllerTest {
	
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
	
	private DeleteDowntimeAdhocRequestVo deleteDowntimeAdhocRequestVo;

	private static Logger logger = LogManager.getLogger(DeleteAdhocControllerTest.class);
	
	@Before
    public void setup(){

		deleteDowntimeAdhocRequestVo = new DeleteDowntimeAdhocRequestVo();
		deleteDowntimeAdhocRequestVo.setFunctionId(1);

    }

	@Test
	public void deleteDowntimeAdhocRequireApprovalSuccessTest() throws Exception {
		logger.debug("deleteDowntimeAdhocRequireApprovalSuccessTest()");
		
		String url = "/bo/system/downtime/adhoc/10";
		logger.debug("    url: " + url);
		
		DowntimeAdhoc downtimeAdhoc  = new DowntimeAdhoc();
		downtimeAdhoc.setApprovalId(1);
		ResponseEntity<BoData> responseEntity = new ResponseEntity<BoData>(downtimeAdhoc, HttpStatus.OK);
		
		when(downtimeAdhocServiceMock.deleteDowntimeAdhoc(Mockito.anyObject(), Mockito.anyInt(), Mockito.anyString())).thenReturn(responseEntity);
		
		mockMvc.perform(MockMvcRequestBuilders.delete(url)
				.header("userid","2")
                .contentType(MediaType.APPLICATION_JSON).content(asJsonString(deleteDowntimeAdhocRequestVo)))
				.andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("approvalId", Matchers.is(1)));
	}
	
	@Test
	public void deleteDowntimeAdhocNoApprovalRequireSuccessTest() throws Exception {
		logger.debug("deleteDowntimeAdhocNoApprovalRequireSuccessTest()");
		
		String url = "/bo/system/downtime/adhoc/10";
		logger.debug("    url: " + url);
		
		DowntimeAdhoc downtimeAdhoc  = new DowntimeAdhoc();
		ResponseEntity<BoData> responseEntity = new ResponseEntity<BoData>(downtimeAdhoc, HttpStatus.CREATED);
		
		when(downtimeAdhocServiceMock.deleteDowntimeAdhoc(Mockito.anyObject(), Mockito.anyInt(), Mockito.anyString())).thenReturn(responseEntity);
		
		mockMvc.perform(MockMvcRequestBuilders.delete(url)
				.header("userid","2")
                .contentType(MediaType.APPLICATION_JSON).content(asJsonString(deleteDowntimeAdhocRequestVo)))
				.andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("approvalId", Matchers.is(0)));
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void deleteDowntimeAdhocIsActivatedeFailTest() throws Exception {
		logger.debug("deleteDowntimeAdhocIsActivatedeFailTest()");
		
		String url = "/bo/system/downtime/adhoc/10";
		logger.debug("    url: " + url);
		
		when(downtimeAdhocServiceMock.deleteDowntimeAdhoc(Mockito.anyObject(), Mockito.anyInt(), Mockito.anyString())).thenThrow(DeleteAdhocNotAllowedException.class);
		
		mockMvc.perform(MockMvcRequestBuilders.delete(url)
				.header("userid","2")
                .contentType(MediaType.APPLICATION_JSON).content(asJsonString(deleteDowntimeAdhocRequestVo)))
				.andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isForbidden());
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void deleteDowntimeAdhocPendingApprovalExceptionFailTest() throws Exception {
		logger.debug("deleteDowntimeAdhocPendingApprovalExceptionFailTest()");
		
		String url = "/bo/system/downtime/adhoc/10";
		logger.debug("    url: " + url);
		
		when(downtimeAdhocServiceMock.deleteDowntimeAdhoc(Mockito.anyObject(), Mockito.anyInt(), Mockito.anyString())).thenThrow(PendingApprovalException.class);
		
		mockMvc.perform(MockMvcRequestBuilders.delete(url)
				.header("userid","2")
                .contentType(MediaType.APPLICATION_JSON).content(asJsonString(deleteDowntimeAdhocRequestVo)))
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
