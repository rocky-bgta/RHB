package com.rhbgroup.dcpbo.user.workflow.user;

import com.rhbgroup.dcp.util.JsonUtil;
import com.rhbgroup.dcpbo.user.workflow.user.delete.WorkflowUserDeleteService;
import com.rhbgroup.dcpbo.user.workflow.user.delete.dto.UserDeleteApprovalRequestVo;
import com.rhbgroup.dcpbo.user.workflow.user.delete.dto.UserDeleteApprovalResponseVo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
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

import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest(classes = {
		WorkflowUserController.class,
		WorkflowUserControllerTest.class
})
@EnableWebMvc
public class WorkflowUserControllerTest {

	private static Logger logger = LogManager.getLogger(WorkflowUserControllerTest.class);

	@Autowired
	MockMvc mockMvc;

	@MockBean
	WorkflowUserDeleteService workflowUserDeleteServiceMock;

	@MockBean(name = "wfUserApprovalService")
	WFUserApprovalService wfUserApprovalServiceMock;

	@Test
	public void userDeleteApprovalTest() throws Exception {
		logger.debug("userDeleteApprovalTest()");

		int approvalId = 1;
		String reason = "Delete that user.";

		UserDeleteApprovalRequestVo request = new UserDeleteApprovalRequestVo();
		request.setReason(reason);
		logger.debug(String.format("Request: %s", request.toString()));

		UserDeleteApprovalResponseVo response = new UserDeleteApprovalResponseVo();
		response.setApprovalId(approvalId);
		logger.debug(String.format("Expected Response: %s", response.toString()));

		when(workflowUserDeleteServiceMock.userDeleteApproval(approvalId, reason)).thenReturn(response);

		String url = "/bo/workflow/user/delete/approval/" + approvalId;
		logger.debug("    url: " + url);

		mockMvc.perform(MockMvcRequestBuilders.put(url)
				.content(asJsonString(request))
				.contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
				.andDo(MockMvcResultHandlers.print())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
				.andExpect(MockMvcResultMatchers.status().is2xxSuccessful());

	}

	public static String asJsonString(final Object obj) {
		try {
			return JsonUtil.objectToJson(obj);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
