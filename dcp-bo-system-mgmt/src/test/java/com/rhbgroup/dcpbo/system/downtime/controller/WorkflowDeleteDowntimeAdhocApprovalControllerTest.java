package com.rhbgroup.dcpbo.system.downtime.controller;

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
import com.rhbgroup.dcpbo.system.downtime.service.WorkflowDowntimeService;
import com.rhbgroup.dcpbo.system.downtime.vo.DeleteDowntimeAdhocApprovalRequestVo;
import com.rhbgroup.dcpbo.system.downtime.vo.DeleteDowntimeAdhocApprovalResponseVo;
import com.rhbgroup.dcpbo.system.exception.DeleteAdhocNotAllowedException;
import com.rhbgroup.dcpbo.system.exception.DowntimeExceptionAdvice;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest(classes = {WorkflowDeleteDowntimeAdhocApprovalControllerTest.class, WorkflowDowntimeController.class})
@EnableWebMvc
public class WorkflowDeleteDowntimeAdhocApprovalControllerTest {
	
	@Autowired
	MockMvc mockMvc;
	
	@MockBean
	WorkflowDowntimeService workflowDowntimeServiceMock;
	
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
	
	private DeleteDowntimeAdhocApprovalRequestVo deleteDowntimeAdhocApprovalRequestVo;

	@Before
    public void setup(){

		deleteDowntimeAdhocApprovalRequestVo = new DeleteDowntimeAdhocApprovalRequestVo();
		deleteDowntimeAdhocApprovalRequestVo.setReason("Wrong config");

    }

	@Test
	public void deleteDowntimeAdhocApprovalSuccessTest() throws Exception {
		
		int approvalId = 123;
		String url = "/bo//workflow/downtime/adhoc/delete/approval/" + approvalId;
		
		String userId = "456789";
		
        DeleteDowntimeAdhocApprovalResponseVo deleteDowntimeAdhocApprovalResponseVo = new DeleteDowntimeAdhocApprovalResponseVo();
        deleteDowntimeAdhocApprovalResponseVo.setApprovalId(approvalId);

		when(workflowDowntimeServiceMock.deleteApproval(Mockito.anyInt(), Mockito.anyObject(), Mockito.anyString())).thenReturn(deleteDowntimeAdhocApprovalResponseVo);

		mockMvc.perform(MockMvcRequestBuilders.put(url)
				.header("userId", userId)
				.contentType(MediaType.APPLICATION_JSON).content(asJsonString(deleteDowntimeAdhocApprovalRequestVo)))
				.andDo(MockMvcResultHandlers.print())
				.andExpect(MockMvcResultMatchers.jsonPath("approvalId", Matchers.is(123)))
                .andExpect(MockMvcResultMatchers.status().isOk());
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void deleteDowntimeAdhocApprovalIsActivatedeFailTest() throws Exception {
		
		int approvalId = 123;
		String url = "/bo//workflow/downtime/adhoc/delete/approval/" + approvalId;
		
		String userId = "456789";
		
		when(workflowDowntimeServiceMock.deleteApproval(Mockito.anyInt(), Mockito.anyObject(), Mockito.anyString())).thenThrow(DeleteAdhocNotAllowedException.class);
		
		mockMvc.perform(MockMvcRequestBuilders.put(url)
				.header("userid",userId)
                .contentType(MediaType.APPLICATION_JSON).content(asJsonString(deleteDowntimeAdhocApprovalRequestVo)))
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
