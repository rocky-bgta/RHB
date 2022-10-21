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

import com.rhbgroup.dcp.util.JsonUtil;
import com.rhbgroup.dcpbo.common.exception.CommonException;
import com.rhbgroup.dcpbo.common.exception.CommonExceptionAdvice;
import com.rhbgroup.dcpbo.common.exception.ConfigErrorInterface;
import com.rhbgroup.dcpbo.common.exception.ErrorResponse;
import com.rhbgroup.dcpbo.system.common.BoData;
import com.rhbgroup.dcpbo.system.downtime.dto.WorkflowDowntimeApproval;
import com.rhbgroup.dcpbo.system.downtime.service.WorkflowDowntimeService;
import com.rhbgroup.dcpbo.system.downtime.util.Util;
import com.rhbgroup.dcpbo.system.exception.DowntimeExceptionAdvice;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest(classes = {WorkflowDowntimeApprovalControllerTest.class, WorkflowDowntimeController.class})
@EnableWebMvc
public class WorkflowDowntimeApprovalControllerTest {
	
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
		String url = "/bo/workflow/downtime/approval/" + approvalId;
		System.out.println("    url: " + url);
		
		int userId = 456789;
		System.out.println("    userId: " + userId);
		
		String jsonStr = Util.loadJsonResourceFile(getClass(), "WorkflowDowntimeApprovalControllerTest.json");
        WorkflowDowntimeApproval workflowDowntimeApproval = JsonUtil.jsonToObject(jsonStr, WorkflowDowntimeApproval.class);
        System.out.println("    workflowDowntimeApproval: " + workflowDowntimeApproval);

		ResponseEntity<BoData> responseEntity = new ResponseEntity<BoData>(workflowDowntimeApproval, HttpStatus.OK);

		when(workflowDowntimeServiceMock.getApproval(Mockito.anyInt(), Mockito.anyInt())).thenReturn(responseEntity);

		mockMvc.perform(MockMvcRequestBuilders.get(url).header("userId", userId)
                .contentType(MediaType.APPLICATION_JSON))
				.andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk());
	}
	
}
