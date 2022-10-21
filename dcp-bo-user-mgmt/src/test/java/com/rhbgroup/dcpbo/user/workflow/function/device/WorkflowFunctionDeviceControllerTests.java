package com.rhbgroup.dcpbo.user.workflow.function.device;

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
@SpringBootTest(classes = {WorkflowFunctionDeviceControllerTests.class, WorkflowFunctionDeviceController.class})
@EnableWebMvc
public class WorkflowFunctionDeviceControllerTests {
	@Autowired
	MockMvc mockMvc;
	
	@MockBean
	WorkflowFunctionDeviceApprovalService workflowFunctionDeviceApprovalServiceMock;

	private static Logger logger = LogManager.getLogger(WorkflowFunctionDeviceControllerTests.class);
	
	int approvalId = 123;

	@Test
	public void getDeviceApprovalTest() throws Exception {
		logger.debug("getDeviceApprovalTest()");
		logger.debug("    workflowFunctionDeviceApprovalServiceMock: " + workflowFunctionDeviceApprovalServiceMock);
		
		WorkflowFunctionDeviceApproval workflowFunctionDeviceApproval = new WorkflowFunctionDeviceApproval();
    	when(workflowFunctionDeviceApprovalServiceMock.getDeviceApproval(Mockito.anyInt(), Mockito.anyInt())).thenReturn(workflowFunctionDeviceApproval);
		
		String url = "/bo/workflow/function/device/approval/" + approvalId;
		logger.debug("    url: " +  url);

		mockMvc.perform(MockMvcRequestBuilders.get(url)
				.header("userid", 123))
				.andDo(MockMvcResultHandlers.print())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
				.andExpect(MockMvcResultMatchers.status().is2xxSuccessful());
	}
}
