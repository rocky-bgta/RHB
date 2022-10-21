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
@SpringBootTest(classes = { UsergroupAccessTest.class, UsergroupAccess.class })
public class UsergroupAccessTest {
	private static Logger logger = LogManager.getLogger(UsergroupAccessTest.class);

	@Test
	public void testUsergroupAccess() {
		logger.debug("testUsergroupAccess()");
		Integer userGroupId = 1;
		Integer functionId = 1;
		Integer moduleId = 1;
		String scopeId = "scopeId";
		String accessType = "accessType";
		String status = "status";
		Timestamp createdTime = new Timestamp(new Date().getTime());
		String createdBy = "createdBy";
		Timestamp updatedTime = new Timestamp(new Date().getTime());
		String updatedBy = "updatedBy";

		UsergroupAccess usergroupAccess = new UsergroupAccess();
		usergroupAccess.setUserGroupId(userGroupId);
		usergroupAccess.setFunctionId(functionId);
		usergroupAccess.setScopeId(scopeId);
		usergroupAccess.setAccessType(accessType);
		usergroupAccess.setStatus(status);
		usergroupAccess.setCreatedTime(createdTime);
		usergroupAccess.setCreatedBy(createdBy);
		usergroupAccess.setUpdatedTime(updatedTime);
		usergroupAccess.setUpdatedBy(updatedBy);

		assertEquals(userGroupId, usergroupAccess.getUserGroupId());
		assertEquals(functionId, usergroupAccess.getFunctionId());

		assertEquals(scopeId, usergroupAccess.getScopeId());
		assertEquals(accessType, usergroupAccess.getAccessType());
		assertEquals(status, usergroupAccess.getStatus());
		assertEquals(createdTime, usergroupAccess.getCreatedTime());
		assertEquals(createdBy, usergroupAccess.getCreatedBy());
		assertEquals(updatedTime, usergroupAccess.getUpdatedTime());
		assertEquals(updatedBy, usergroupAccess.getUpdatedBy());

	}
}
