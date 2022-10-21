package com.rhbgroup.dcpbo.user.create;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { UserPayloadUpdateVo.class })
public class UserPayloadUpdateVoTest {

	@Test
	public void userCreateRequestVoTest() {

		String username = "leeyawkhang";
		Integer departmentId = 1;
		String departmentName = "Administrative";
		String email = "lee.yaw.khang@rhbgroup.com";
		String name = "Lee Yaw Khang";
		List<UserFunctionUserGroupVo> group = new ArrayList<UserFunctionUserGroupVo>();
		String status = "E";

		UserPayloadUpdateVo userPayloadUpdateVo = new UserPayloadUpdateVo();
		userPayloadUpdateVo.setUsername(username);
		userPayloadUpdateVo.setDepartmentId(departmentId);
		userPayloadUpdateVo.setDepartmentName(departmentName);
		userPayloadUpdateVo.setEmail(email);
		userPayloadUpdateVo.setName(name);
		userPayloadUpdateVo.setStatus(status);
		userPayloadUpdateVo.toString();

		UserFunctionUserGroupVo group1 = new UserFunctionUserGroupVo();
		group1.setGroupId(1);
		group1.setGroupName("Admin Maker");
		group.add(group1);
		UserFunctionUserGroupVo group2 = new UserFunctionUserGroupVo();
		group2.setGroupId(3);
		group2.setGroupName("Admin Viewer");
		group.add(group2);
		userPayloadUpdateVo.setGroup(group);

		assertEquals(username, userPayloadUpdateVo.getUsername());
		assertEquals(departmentId, userPayloadUpdateVo.getDepartmentId());
		assertEquals(departmentName, userPayloadUpdateVo.getDepartmentName());
		assertEquals(email, userPayloadUpdateVo.getEmail());
		assertEquals(name, userPayloadUpdateVo.getName());
		assertEquals(group, userPayloadUpdateVo.getGroup());
		assertEquals(status, userPayloadUpdateVo.getStatus());

	}

}
