package com.rhbgroup.dcpbo.user.create;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { UserUpdateInputVo.class })
public class UserUpdateInputVoTest {

	@Test
	public void userUpdateInputVoTest() {

		String email = "lee.yaw.khang@rhbgroup.com";
		String name = "Lee Yaw Khang";
		Integer departmentId = 1;
		String departmentName = "Administrative";
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

		UserUpdateInputVo userUpdateInputVo = new UserUpdateInputVo();
		userUpdateInputVo.setEmail(email);
		userUpdateInputVo.setName(name);
		userUpdateInputVo.setDepartmentId(departmentId);
		userUpdateInputVo.setDepartmentName(departmentName);
		userUpdateInputVo.setGroup(group);
		userUpdateInputVo.setStatus(status);
		userUpdateInputVo.toString();

		assertEquals(email, userUpdateInputVo.getEmail());
		assertEquals(name, userUpdateInputVo.getName());
		assertEquals(departmentId, userUpdateInputVo.getDepartmentId());
		assertEquals(departmentName, userUpdateInputVo.getDepartmentName());
		assertEquals(group, userUpdateInputVo.getGroup());
		assertEquals(status, userUpdateInputVo.getStatus());

	}
}
