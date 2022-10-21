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
@SpringBootTest(classes = { BoUmApprovalUserGroupTest.class, BoUmApprovalUserGroup.class })
public class BoUmApprovalUserGroupTest {

	private static Logger logger = LogManager.getLogger(BoUmApprovalUserGroupTest.class);

	@Test
	public void testBoUmApprovalUserGroup() {
		logger.debug("testBoUmApprovalUserGroup()");
		Integer id = 1;
		Integer approvalId = 1;
		String state = "state";
		String lockingId = "lockingId";
		String payload = "payload";
		Date createdTime = new Date();
		String createdBy = "createdBy";
		Date updatedTime = new Date();
		String updatedBy = "updatedBy";

		BoUmApprovalUserGroup boUmApprovalUserGroup = new BoUmApprovalUserGroup();
		boUmApprovalUserGroup.setId(id);
		boUmApprovalUserGroup.setApprovalId(approvalId);
		boUmApprovalUserGroup.setState(state);
		boUmApprovalUserGroup.setLockingId(lockingId);
		boUmApprovalUserGroup.setPayload(payload);
		boUmApprovalUserGroup.setCreatedTime(createdTime);
		boUmApprovalUserGroup.setCreatedBy(createdBy);
		boUmApprovalUserGroup.setUpdatedTime(updatedTime);
		boUmApprovalUserGroup.setUpdatedBy(updatedBy);

		assertEquals(id, boUmApprovalUserGroup.getId());
		assertEquals(approvalId, boUmApprovalUserGroup.getApprovalId());
		assertEquals(state, boUmApprovalUserGroup.getState());
		assertEquals(lockingId, boUmApprovalUserGroup.getLockingId());
		assertEquals(payload, boUmApprovalUserGroup.getPayload());
		assertEquals(createdTime, boUmApprovalUserGroup.getCreatedTime());
		assertEquals(createdBy, boUmApprovalUserGroup.getCreatedBy());
		assertEquals(updatedTime, boUmApprovalUserGroup.getUpdatedTime());
		assertEquals(updatedBy, boUmApprovalUserGroup.getUpdatedBy());
	}

}
