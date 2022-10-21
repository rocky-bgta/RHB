package com.rhbgroup.dcpbo.user.workflow.user.delete.dto;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
		ApprovalVo.class
})
public class ApprovalVoTest {
	private static Logger logger = LogManager.getLogger(ApprovalVoTest.class);

	@Test
	public void approvalVoTest() {

		int id = 1;
		int functionId = 2;
		int creatorId = 3;
		String description = "Description";
		String actionType = "Action Type";
		String status = "S";
		String reason = "Reason";
		String scopeId = "Scope";

		ApprovalVo approvalVo = new ApprovalVo();
		approvalVo.setId(id);
		approvalVo.setFunctionId(functionId);
		approvalVo.setCreatorId(creatorId);
		approvalVo.setDescription(description);
		approvalVo.setActionType(actionType);
		approvalVo.setStatus(status);
		approvalVo.setReason(reason);
		approvalVo.setScopeId(scopeId);

		assertEquals(id, approvalVo.getId());
		assertEquals(functionId, approvalVo.getFunctionId());
		assertEquals(creatorId, approvalVo.getCreatorId());
		assertEquals(description, approvalVo.getDescription());
		assertEquals(actionType, approvalVo.getActionType());
		assertEquals(status, approvalVo.getStatus());
		assertEquals(reason, approvalVo.getReason());
		assertEquals(scopeId, approvalVo.getScopeId());
	}
}
