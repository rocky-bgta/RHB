package com.rhbgroup.dcpbo.user.info.model.bo;

import com.rhbgroup.dcpbo.user.common.model.bo.Usergroup;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { User.class, UserTest.class })

public class UserTest {
	private static Logger logger = LogManager.getLogger(UserTest.class);

	@Test
	public void testUser() {
		logger.debug("testUser()");
		Integer id = 1;
		String username = "Username";
		String email = "email@email.com";
		String name = "name";
		Integer userDepartmentId = 11;
		String userStatusId = "Active";
		Timestamp lastLoginTime = new Timestamp(new Date().getTime());
		Integer failedLoginCount = 1;
		Timestamp createdTime = new Timestamp(new Date().getTime());
		String createdBy = "createdBy";
		Timestamp updatedTime = new Timestamp(new Date().getTime());
		String updatedBy = "updatedBy";
		String departmentName = "DepartmentName";

		Usergroup usergroup1 = new Usergroup();
		usergroup1.setId(1);
		usergroup1.setGroupName("Usergroup1");
		Usergroup usergroup2 = new Usergroup();
		usergroup2.setId(1);
		usergroup2.setGroupName("Usergroup2");

		List<Usergroup> usergroup = new ArrayList<>();
		usergroup.add(usergroup1);

		User user = new User();
		user.setId(id);
		user.setUsername(username);
		user.setEmail(email);
		user.setName(name);
		user.setUserDepartmentId(userDepartmentId);
		user.setUserStatusId(userStatusId);
		user.setLastLoginTime(lastLoginTime);
		user.setFailedLoginCount(failedLoginCount);
		user.setCreatedTime(createdTime);
		user.setCreatedBy(createdBy);
		user.setUpdatedTime(updatedTime);
		user.setUpdatedBy(updatedBy);
		user.setDepartmentName(departmentName);
		user.setUsergroup(usergroup);

		assertEquals(id, user.getId());
		assertEquals(username, user.getUsername());
		assertEquals(email, user.getEmail());
		assertEquals(name, user.getName());
		assertEquals(userDepartmentId, user.getUserDepartmentId());
		assertEquals(userStatusId, user.getUserStatusId());
		assertEquals(lastLoginTime, user.getLastLoginTime());
		assertEquals(failedLoginCount, user.getFailedLoginCount());
		assertEquals(createdTime, user.getCreatedTime());
		assertEquals(createdBy, user.getCreatedBy());
		assertEquals(updatedTime, user.getUpdatedTime());
		assertEquals(updatedBy, user.getUpdatedBy());
		assertEquals(departmentName, user.getDepartmentName());
		assertEquals(usergroup, user.getUsergroup());
	}
}
