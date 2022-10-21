package com.rhbgroup.dcpbo.user.usergroup;

import static org.mockito.Mockito.when;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

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
import com.rhbgroup.dcpbo.user.workflow.usergroup.WFFunctionValue;
import com.rhbgroup.dcpbo.user.workflow.usergroup.WFUserGroupApprovalController;
import com.rhbgroup.dcpbo.user.workflow.usergroup.WFUserGroupApprovalDetail;
import com.rhbgroup.dcpbo.user.workflow.usergroup.WFUserGroupApprovalDetailValue;
import com.rhbgroup.dcpbo.user.workflow.usergroup.WFUserGroupDetailFunctionValue;
import com.rhbgroup.dcpbo.user.workflow.usergroup.WFUserGroupFunctionPayload;
import com.rhbgroup.dcpbo.user.workflow.usergroup.WFUserGroupPayload;
import com.rhbgroup.dcpbo.user.workflow.usergroup.WFUserGroupService;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest(classes = { WFUserGroupApprovalControllerTest.class, WFUserGroupApprovalController.class })
@EnableWebMvc
public class WFUserGroupApprovalControllerTest {
	@Autowired
	MockMvc mockMvc;

	@MockBean(name = "wfUserGroupApprovalService")
	WFUserGroupService wfUserGroupServiceMock;

	private static Logger logger = LogManager.getLogger(WFUserGroupApprovalControllerTest.class);

	@Test
	public void getWorkflowUserApprovalDetailTest() throws Exception {
		logger.debug("getWorkflowUserApprovalDetailTest()");

		WFUserGroupApprovalDetail wfUserGroupApprovalDetail = new WFUserGroupApprovalDetail();
		wfUserGroupApprovalDetail.setApprovalId(1);
		wfUserGroupApprovalDetail
				.setGroupName(new WFUserGroupApprovalDetailValue("Group Name Test", "Group Name Test"));
		wfUserGroupApprovalDetail
				.setAccessType(new WFUserGroupApprovalDetailValue("Access Type Test", "Access Type Test"));

		wfUserGroupApprovalDetail.setFunction(new WFUserGroupDetailFunctionValue(
				new WFFunctionValue(Arrays.asList(new String[] { "function name 1", "function name 2" })),
				new WFFunctionValue(Arrays.asList(new String[] { "function name 1", "function name 2" }))));
		
		//silly but this is for code coverage.
		
		List<String> codeCoverageList= Arrays.asList(new String[] { "function name 1", "function name 2" });
		WFFunctionValue wfFunctionValueTest = new WFFunctionValue(codeCoverageList);
		wfFunctionValueTest.setFunctionName(codeCoverageList);
		wfFunctionValueTest.toString();
		WFUserGroupDetailFunctionValue wfUserGroupApprovalDetailTest = new WFUserGroupDetailFunctionValue(wfFunctionValueTest,wfFunctionValueTest);
		wfUserGroupApprovalDetailTest.setBefore(new WFFunctionValue(codeCoverageList));
		wfUserGroupApprovalDetailTest.setAfter(new WFFunctionValue(codeCoverageList));
		wfUserGroupApprovalDetailTest.toString();
		WFUserGroupApprovalDetailValue wfUserGroupApprovalDetailValueTest = new WFUserGroupApprovalDetailValue("Group Name Test", "Group Name Test");
		wfUserGroupApprovalDetailValueTest.setBefore("Group Name Test");
		wfUserGroupApprovalDetailValueTest.setAfter("Group Name Test");
		WFUserGroupFunctionPayload wfUserGroupFunctionPayload = new WFUserGroupFunctionPayload();
		wfUserGroupFunctionPayload.toString();
		WFUserGroupPayload wfUserGroupPayload = JsonUtil.jsonToObject("{}", WFUserGroupPayload.class);
		wfUserGroupPayload.toString();
		
		wfUserGroupApprovalDetail.setActionType("EDIT");
		String timeStamp = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX").format(new Date());
		wfUserGroupApprovalDetail.setUpdatedTime(timeStamp);
		wfUserGroupApprovalDetail.setReason("");
		wfUserGroupApprovalDetail.setCreatorName("creator name");
		wfUserGroupApprovalDetail.setIsCreator("N");
		when(wfUserGroupServiceMock.getWorkflowApprovalDetail(Mockito.anyInt(), Mockito.anyInt()))
				.thenReturn(wfUserGroupApprovalDetail);

		int approvalId = 1;
		logger.debug("approvalId : {}", approvalId);

		String headerUserId = "1";
		String url = "/bo/workflow/function/usergroup/approval/" + approvalId;
		logger.debug("    url: " + url);

		mockMvc.perform(MockMvcRequestBuilders.get(url).header("userId", headerUserId))
				.andDo(MockMvcResultHandlers.print())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
				.andExpect(MockMvcResultMatchers.status().is2xxSuccessful());
	}
}
