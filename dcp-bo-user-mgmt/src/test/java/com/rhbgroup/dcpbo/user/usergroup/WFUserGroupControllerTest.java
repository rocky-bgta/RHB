package com.rhbgroup.dcpbo.user.usergroup;

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
import com.rhbgroup.dcpbo.user.workflow.usergroup.WFUserGroupApprovalActionDetail;
import com.rhbgroup.dcpbo.user.workflow.usergroup.WFUserGroupController;
import com.rhbgroup.dcpbo.user.workflow.usergroup.WFUserGroupService;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest(classes = { WFUserGroupControllerTest.class, WFUserGroupController.class })
@EnableWebMvc
public class WFUserGroupControllerTest {
	@Autowired
	MockMvc mockMvc;

	@MockBean(name = "wfUserGroupService")
	WFUserGroupService wfUserGroupServiceMock;
	
	private static Logger logger = LogManager.getLogger(WFUserGroupControllerTest.class);

	@Test
	public void workflowUserGroupAddTest() throws Exception {
		logger.debug("workflowUserGroupAddTest()");

		WFUserGroupApprovalActionDetail wfUserGroupApprovalActionDetail = new WFUserGroupApprovalActionDetail();
		wfUserGroupApprovalActionDetail.setApprovalId(1);
		wfUserGroupApprovalActionDetail.setReason("dev test");
		
		//silly but this is for code coverage
		wfUserGroupApprovalActionDetail.toString();
		when(wfUserGroupServiceMock.approveCreate(Mockito.anyInt(),Mockito.anyObject())).thenReturn(wfUserGroupApprovalActionDetail);

		int approvalId = 1;
		logger.debug("approvalId : {}", approvalId);

		String url = "/bo/workflow/usergroup/approval/" + approvalId;
		logger.debug("    url: " + url);

		mockMvc.perform(MockMvcRequestBuilders.post(url)
				.header("userId", 1)
				.content(asJsonString(wfUserGroupApprovalActionDetail))
				.contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)).andDo(MockMvcResultHandlers.print())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
				.andExpect(MockMvcResultMatchers.status().is2xxSuccessful());
	}
	
	@Test
	public void workflowUserGroupUpdateTest() throws Exception {
		logger.debug("workflowUserGroupUpdateTest()");

		WFUserGroupApprovalActionDetail wfUserGroupApprovalActionDetail = new WFUserGroupApprovalActionDetail();
		wfUserGroupApprovalActionDetail.setApprovalId(1);
		wfUserGroupApprovalActionDetail.setReason("dev test");
		when(wfUserGroupServiceMock.approveUpdate(Mockito.anyInt(),Mockito.anyObject())).thenReturn(wfUserGroupApprovalActionDetail);

		int approvalId = 1;
		logger.debug("approvalId : {}", approvalId);

		String url = "/bo/workflow/usergroup/update/approval/" + approvalId;
		logger.debug("    url: " + url);

		mockMvc.perform(MockMvcRequestBuilders.put(url)
				.header("userId", 1)
				.content(asJsonString(wfUserGroupApprovalActionDetail))
				.contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)).andDo(MockMvcResultHandlers.print())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
				.andExpect(MockMvcResultMatchers.status().is2xxSuccessful());
	}
	
	@Test
	public void workflowUserGroupDeleteTest() throws Exception {
		logger.debug("workflowUserGroupUpdateTest()");

		WFUserGroupApprovalActionDetail wfUserGroupApprovalActionDetail = new WFUserGroupApprovalActionDetail();
		wfUserGroupApprovalActionDetail.setApprovalId(1);
		wfUserGroupApprovalActionDetail.setReason("dev test");
		when(wfUserGroupServiceMock.approveDelete(Mockito.anyInt(),Mockito.anyObject())).thenReturn(wfUserGroupApprovalActionDetail);

		int approvalId = 1;
		logger.debug("approvalId : {}", approvalId);

		String url = "/bo/workflow/usergroup/delete/approval/" + approvalId;
		logger.debug("    url: " + url);

		mockMvc.perform(MockMvcRequestBuilders.put(url)
				.header("userId", 1)
				.content(asJsonString(wfUserGroupApprovalActionDetail))
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
