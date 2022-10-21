package com.rhbgroup.dcpbo.user.workflow.user;

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

import com.rhbgroup.dcp.util.JsonUtil;
import com.rhbgroup.dcpbo.user.workflow.user.delete.WorkflowUserDeleteService;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest(classes = { WFUserApprovalUpdateActionControllerTest.class, WorkflowUserController.class })
@EnableWebMvc
public class WFUserApprovalUpdateActionControllerTest {
	@Autowired
	MockMvc mockMvc;
 
	@MockBean(name = "wfUserApprovalActionService")
	WFUserApprovalService wfUserApprovalServiceMock;
	
	@MockBean
	WorkflowUserDeleteService workflowUserDeleteServiceMock;

	private static Logger logger = LogManager.getLogger(WFUserApprovalUpdateActionControllerTest.class);

	@Test
	public void workflowUserApprovalUpdateActionDetailTest() throws Exception {
		logger.debug("workflowUserApprovalUpdateActionDetailTest()");

		WFUserApprovalActionDetail wfUserApprovalActionDetail = new WFUserApprovalActionDetail();
		wfUserApprovalActionDetail.setApprovalId(1);
		wfUserApprovalActionDetail.setReason("dev test");
		when(wfUserApprovalServiceMock.approveUpdate(Mockito.anyInt(),Mockito.anyObject())).thenReturn(wfUserApprovalActionDetail);

		int approvalId = 1;
		logger.debug("approvalId : {}", approvalId);

		String url = "/bo/workflow/user/update/approval/" + approvalId;
		logger.debug("    url: " + url);

		mockMvc.perform(MockMvcRequestBuilders.put(url)
				.header("userId", 1)
				.content(asJsonString(wfUserApprovalActionDetail))
				.contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)).andDo(MockMvcResultHandlers.print())
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
