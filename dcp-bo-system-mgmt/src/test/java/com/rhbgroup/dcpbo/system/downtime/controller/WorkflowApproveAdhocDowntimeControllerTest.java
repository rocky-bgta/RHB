package com.rhbgroup.dcpbo.system.downtime.controller;

import static org.mockito.Mockito.when;

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
import com.rhbgroup.dcpbo.system.downtime.dto.AdhocApproval;
import com.rhbgroup.dcpbo.system.downtime.service.WorkflowDowntimeService;
import com.rhbgroup.dcpbo.system.downtime.vo.ApproveDowntimeAdhocRequestVo;
import com.rhbgroup.dcpbo.system.exception.DowntimeExceptionAdvice;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest(classes = {WorkflowApproveAdhocDowntimeControllerTest.class, WorkflowDowntimeController.class})
@EnableWebMvc
public class WorkflowApproveAdhocDowntimeControllerTest {
	
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

	@Test
	public void controllerTest() throws Exception {
		System.out.println("controllerTest()");
		
		int approvalId = 123;
		String url = "/bo/workflow/downtime/adhoc/approval/" + approvalId;
		System.out.println("    url: " + url);
		
		AdhocApproval adhocApproval = new AdhocApproval();
		adhocApproval.setApprovalId(approvalId);
		System.out.println("    adhocApproval: " + adhocApproval);
		
		ResponseEntity<BoData> responseEntity = new ResponseEntity<BoData>(adhocApproval, HttpStatus.OK);
		when(workflowDowntimeServiceMock.workflowApproveAdhocDowntime(Mockito.anyInt(), Mockito.any(), Mockito.anyString())).thenReturn(responseEntity);

		ApproveDowntimeAdhocRequestVo request = new ApproveDowntimeAdhocRequestVo();
		request.setReason("This is the reason");

		mockMvc.perform(MockMvcRequestBuilders.post(url)
                .contentType(MediaType.APPLICATION_JSON).content(asJsonString(request))
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
