package com.rhbgroup.dcpbo.user.workflow.user;

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

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest(classes = { WFUserApprovalControllerTest.class, WFUserApprovalController.class })
@EnableWebMvc
public class WFUserApprovalControllerTest {
	@Autowired
	MockMvc mockMvc;

	@MockBean(name = "wfUserApprovalService")
	WFUserApprovalService wfUserApprovalServiceMock;

	private static Logger logger = LogManager.getLogger(WFUserApprovalControllerTest.class);

	@Test
	public void getWorkflowUserApprovalDetailTest() throws Exception {
		logger.debug("getWorkflowUserApprovalDetailTest()");

		WFUserApprovalDetail wfUserApprovalDetail = new WFUserApprovalDetail();
		wfUserApprovalDetail.setApprovalId(1);
		wfUserApprovalDetail.setUsername(new WFUserApprovalDetailValue("Nesh1", "Nesh1"));
		wfUserApprovalDetail.setEmail(new WFUserApprovalDetailValue("nesh@rhbgroup.my", "nesh@rhbgroup.my"));
		wfUserApprovalDetail.setName(new WFUserApprovalDetailValue("Sarjen Nesh", "Sarjen Nesh"));

		WFDepartmentPayload departmentPayloadBefore = new WFDepartmentPayload();
		departmentPayloadBefore.setDepartmentId(1);
		departmentPayloadBefore.setDepartmentName("Sales");

		WFDepartmentPayload departmentPayloadAfter = new WFDepartmentPayload();
		departmentPayloadAfter.setDepartmentId(1);
		departmentPayloadAfter.setDepartmentName("Sales");

		wfUserApprovalDetail.setDepartmentName(new WFUserApprovalDetailValue(
				departmentPayloadBefore.getDepartmentName(), departmentPayloadAfter.getDepartmentName()));

		wfUserApprovalDetail.setStatus(new WFUserApprovalDetailValue("A", "A"));

		wfUserApprovalDetail.setUsergroup(new WFUserApprovalDetailUserGroupValue(
				new WFUserGroupValue(Arrays.asList(new String[] { "User Admin", "Customer Admin" })),
				new WFUserGroupValue(Arrays.asList(new String[] { "User Admin", "Customer Admin" }))));
		
		WFUserGroupValue wfUserGroupValue = new WFUserGroupValue(Arrays.asList(new String[] { "User Admin", "Customer Admin" }));
		List<String> groupNameList = Arrays.asList(new String[] { "User Admin", "Customer Admin" });
		WFUserGroupValue wfUserGroupValueTest = new WFUserGroupValue(groupNameList);
		//silly but this is for code coverage
		wfUserGroupValueTest.setGroupName(groupNameList);
		wfUserApprovalDetail.setActionType("EDIT");
		wfUserApprovalDetail.setCreatorName("Jonathanarina");
		String timeStamp = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX").format(new Date());
		wfUserApprovalDetail.setUpdatedTime(timeStamp);
		wfUserApprovalDetail.setReason("");
		wfUserApprovalDetail.setIsCreator("N");
		when(wfUserApprovalServiceMock.getWorkflowApprovalDetail(Mockito.anyInt(), Mockito.anyInt()))
				.thenReturn(wfUserApprovalDetail);

		int approvalId = 1;
		logger.debug("approvalId : {}", approvalId);

		String headerUserId = "1";
		String url = "/bo/workflow/function/user/approval/" + approvalId;
		logger.debug("    url: " + url);
		//silly but this is for code coverage for toString method.
		wfUserApprovalDetail.toString();
		mockMvc.perform(MockMvcRequestBuilders.get(url).header("userId", headerUserId))
				.andDo(MockMvcResultHandlers.print())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
				.andExpect(MockMvcResultMatchers.status().is2xxSuccessful());
	}
}
