package com.rhbgroup.dcpbo.user.common.model.bo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.sql.Timestamp;
import java.util.Date;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { UmApprovalUser.class, UmApprovalUserTest.class })
public class UmApprovalUserTest {
	private static Logger logger = LogManager.getLogger(UmApprovalUserTest.class);

	@Test
	public void testUmApprovalUser() {
		logger.debug("testUmApprovalUser()");

		Integer id = 1;
		Integer approvalId = 2;
		String state = "state";
		String lockingId = "lockingId";
		String payload = "payload";
		Timestamp createdTime = new Timestamp(new Date().getTime());
		String createdBy = "createdBy";
		Timestamp updatedTime = new Timestamp(new Date().getTime());
		String updatedBy = "updatedBy";

		UmApprovalUser umApprovalUser = new UmApprovalUser();
		umApprovalUser.setId(id);
		umApprovalUser.setApprovalId(approvalId);
		umApprovalUser.setState(state);
		umApprovalUser.setLockingId(lockingId);
		umApprovalUser.setPayload(payload);
		umApprovalUser.setCreatedTime(createdTime);
		umApprovalUser.setCreatedBy(createdBy);
		umApprovalUser.setUpdatedTime(updatedTime);
		umApprovalUser.setUpdatedBy(updatedBy);

		assertEquals(id, umApprovalUser.getId());
		assertEquals(approvalId, umApprovalUser.getApprovalId());
		assertEquals(state, umApprovalUser.getState());
		assertEquals(lockingId, umApprovalUser.getLockingId());
		assertEquals(payload, umApprovalUser.getPayload());
		assertEquals(createdTime, umApprovalUser.getCreatedTime());
		assertEquals(createdBy, umApprovalUser.getCreatedBy());
		assertEquals(updatedTime, umApprovalUser.getUpdatedTime());
		assertEquals(updatedBy, umApprovalUser.getUpdatedBy());
	}
}
