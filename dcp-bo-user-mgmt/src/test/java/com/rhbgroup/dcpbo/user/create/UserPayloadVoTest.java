package com.rhbgroup.dcpbo.user.create;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { UserPayloadVo.class })
public class UserPayloadVoTest {

	@Test
	public void userPayloadVoTest() {

		String username = "leeyawkhang";
		Integer departmentId = 1;
		String departmentName = "Administrative";
		UserFunctionDepartmentVo department = new UserFunctionDepartmentVo();
		String email = "lee.yaw.khang@rhbgroup.com";
		String name = "Lee Yaw Khang";
		List<UserFunctionUserGroupVo> group = new ArrayList<UserFunctionUserGroupVo>();
		String status = "A";

		UserFunctionUserGroupVo group1 = new UserFunctionUserGroupVo();
		group1.setGroupId(1);
		group1.setGroupName("Admin Maker");
		group.add(group1);
		UserFunctionUserGroupVo group2 = new UserFunctionUserGroupVo();
		group2.setGroupId(3);
		group2.setGroupName("Admin Viewer");
		group.add(group2);

		department.setDepartmentId(departmentId);
		department.setDepartmentName(departmentName);

		UserPayloadVo userPayloadVo = new UserPayloadVo();
		userPayloadVo.setUsername(username);
		userPayloadVo.setDepartment(department);
		userPayloadVo.setEmail(email);
		userPayloadVo.setName(name);
		userPayloadVo.setGroup(group);
		userPayloadVo.setStatus(status);
		userPayloadVo.toString();

		assertEquals(username, userPayloadVo.getUsername());
		assertEquals(department, userPayloadVo.getDepartment());
		assertEquals(email, userPayloadVo.getEmail());
		assertEquals(name, userPayloadVo.getName());
		assertEquals(group, userPayloadVo.getGroup());
		assertEquals(status, userPayloadVo.getStatus());

	}

}
