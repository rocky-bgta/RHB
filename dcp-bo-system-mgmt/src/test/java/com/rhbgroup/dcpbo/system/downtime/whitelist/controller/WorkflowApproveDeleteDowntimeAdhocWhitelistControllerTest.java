package com.rhbgroup.dcpbo.system.downtime.whitelist.controller;

import static org.mockito.Mockito.when;

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
import org.springframework.http.MediaType;
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
import com.rhbgroup.dcpbo.system.downtime.dto.AdhocApproval;
import com.rhbgroup.dcpbo.system.downtime.whitelist.service.WorkflowDowntimeAdhocWhitelistService;
import com.rhbgroup.dcpbo.system.downtime.whitelist.vo.ApproveAddDowntimeAdhocWhitelistRequest;
import com.rhbgroup.dcpbo.system.downtime.whitelist.vo.ApproveDeleteDowntimeAdhocWhitelistRequest;
import com.rhbgroup.dcpbo.system.exception.DowntimeExceptionAdvice;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest(classes = {WorkflowApproveDeleteDowntimeAdhocWhitelistControllerTest.class, WorkflowDowntimeAdhocWhitelistController.class})
@EnableWebMvc
public class WorkflowApproveDeleteDowntimeAdhocWhitelistControllerTest {
	
	@Autowired
	MockMvc mockMvc;
	
	@MockBean
	WorkflowDowntimeAdhocWhitelistService workflowDowntimeAdhocWhitelistServiceMock;
	
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
	
	private ApproveDeleteDowntimeAdhocWhitelistRequest approveDeleteDowntimeAdhocWhitelistRequest;

	@Before
    public void setup(){

		approveDeleteDowntimeAdhocWhitelistRequest = new ApproveDeleteDowntimeAdhocWhitelistRequest();
		approveDeleteDowntimeAdhocWhitelistRequest.setReason("Ok");

    }

	@Test
	public void deleteDowntimeAdhocWhitelistApprovalSuccessTest() throws Exception {
		
		int approvalId = 123;
		String url = "/bo//workflow/downtime/whitelist/delete/approval/" + approvalId;
		
		String userId = "456789";
		
		AdhocApproval adhocApproval = new AdhocApproval();
		adhocApproval.setApprovalId(approvalId);

		when(workflowDowntimeAdhocWhitelistServiceMock.approveDeleteDowntimeWhitelist(Mockito.anyInt(), Mockito.anyObject(), Mockito.anyString())).thenReturn(adhocApproval);

		mockMvc.perform(MockMvcRequestBuilders.put(url)
				.header("userId", userId)
				.contentType(MediaType.APPLICATION_JSON).content(asJsonString(approveDeleteDowntimeAdhocWhitelistRequest)))
				.andDo(MockMvcResultHandlers.print())
				.andExpect(MockMvcResultMatchers.jsonPath("approvalId", Matchers.is(123)))
                .andExpect(MockMvcResultMatchers.status().isOk());
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void deleteDowntimeAdhocWhitelistApprovalDuplicateUserFailTest() throws Exception {
		
		int approvalId = 123;
		String url = "/bo//workflow/downtime/whitelist/delete/approval/" + approvalId;
		
		String userId = "456789";
		
		when(workflowDowntimeAdhocWhitelistServiceMock.approveDeleteDowntimeWhitelist(Mockito.anyInt(), Mockito.anyObject(), Mockito.anyString())).thenThrow(CommonException.class);
		
		mockMvc.perform(MockMvcRequestBuilders.put(url)
				.header("userid",userId)
                .contentType(MediaType.APPLICATION_JSON).content(asJsonString(approveDeleteDowntimeAdhocWhitelistRequest)))
				.andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().is5xxServerError());
	}
	
    public static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
	
}
