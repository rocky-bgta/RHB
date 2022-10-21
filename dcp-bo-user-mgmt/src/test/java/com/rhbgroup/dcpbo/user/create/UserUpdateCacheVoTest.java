package com.rhbgroup.dcpbo.user.create;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { UserUpdateCacheVo.class })
public class UserUpdateCacheVoTest {

	@Test
	public void userUpdateCacheVoTest() {

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

		UserUpdateCacheVo userUpdateCacheVo = new UserUpdateCacheVo();
		userUpdateCacheVo.setEmail(email);
		userUpdateCacheVo.setName(name);
		userUpdateCacheVo.setDepartmentId(departmentId);
		userUpdateCacheVo.setDepartmentName(departmentName);
		userUpdateCacheVo.setGroup(group);
		userUpdateCacheVo.setStatus(status);
		userUpdateCacheVo.toString();

		assertEquals(email, userUpdateCacheVo.getEmail());
		assertEquals(name, userUpdateCacheVo.getName());
		assertEquals(departmentId, userUpdateCacheVo.getDepartmentId());
		assertEquals(departmentName, userUpdateCacheVo.getDepartmentName());
		assertEquals(group, userUpdateCacheVo.getGroup());
		assertEquals(status, userUpdateCacheVo.getStatus());

	}

}
