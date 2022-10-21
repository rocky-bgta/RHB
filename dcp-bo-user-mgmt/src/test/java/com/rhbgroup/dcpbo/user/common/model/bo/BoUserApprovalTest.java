package com.rhbgroup.dcpbo.user.common.model.bo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { BoUserApprovalTest.class, BoUserApproval.class })
public class BoUserApprovalTest {
	private static Logger logger = LogManager.getLogger(BoUserApprovalTest.class);

	@Test
	public void testBoUserApproval() {
		logger.debug("testBoUserApproval()");

		Integer id = 1;
		Integer functionId = 1;
		Integer creatorId = 1;
		String description = "description";
		String actionType = "actionType";
		String status = "status";
		String reason = "reason";
		Date createdTime = new Date();
		String createdBy = "createdBy";
		Date updatedTime = new Date();
		String updatedBy = "updatedBy";

		BoUserApproval boUserApproval = new BoUserApproval();
		boUserApproval.setId(id);
		boUserApproval.setFunctionId(functionId);
		boUserApproval.setCreatorId(creatorId);
		boUserApproval.setDescription(description);
		boUserApproval.setActionType(actionType);
		boUserApproval.setStatus(status);
		boUserApproval.setReason(reason);
		boUserApproval.setCreatedTime(createdTime);
		boUserApproval.setCreatedBy(createdBy);
		boUserApproval.setUpdatedTime(updatedTime);
		boUserApproval.setUpdatedBy(updatedBy);

		assertEquals(id, boUserApproval.getId());
		assertEquals(functionId, boUserApproval.getFunctionId());
		assertEquals(creatorId, boUserApproval.getCreatorId());
		assertEquals(description, boUserApproval.getDescription());
		assertEquals(actionType, boUserApproval.getActionType());
		assertEquals(status, boUserApproval.getStatus());
		assertEquals(reason, boUserApproval.getReason());
		assertEquals(createdTime, boUserApproval.getCreatedTime());
		assertEquals(createdBy, boUserApproval.getCreatedBy());
		assertEquals(updatedTime, boUserApproval.getUpdatedTime());
		assertEquals(updatedBy, boUserApproval.getUpdatedBy());
	}
}
