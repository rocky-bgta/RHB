package com.rhbgroup.dcpbo.user.create;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { UserUpdateRequestVo.class })
public class UserUpdateRequestVoTest {

	@Test
	public void userUpdateRequestVoTest() {

		Integer functionId = 1;
		UserPayloadUpdateVo cache = new UserPayloadUpdateVo();
		UserPayloadUpdateVo input = new UserPayloadUpdateVo();

		String username = "leeyawkhang";
		Integer departmentId = 1;
		String departmentName = "Administrative";
		String email = "lee.yaw.khang@rhbgroup.com";
		String name = "Lee Yaw Khang";
		List<UserFunctionUserGroupVo> group = new ArrayList<UserFunctionUserGroupVo>();
		String status = "E";
		UserFunctionUserGroupVo group1 = new UserFunctionUserGroupVo();
		group1.setGroupId(1);
		group1.setGroupName("Admin Maker");
		group.add(group1);
		UserFunctionUserGroupVo group2 = new UserFunctionUserGroupVo();
		group2.setGroupId(3);
		group2.setGroupName("Admin Viewer");
		group.add(group2);

		cache.setUsername(username);
		cache.setDepartmentId(departmentId);
		cache.setDepartmentName(departmentName);
		cache.setEmail(email);
		cache.setName(name);
		cache.setStatus(status);
		cache.setGroup(group);
		cache.toString();

		input.setUsername(username);
		input.setDepartmentId(departmentId);
		input.setDepartmentName(departmentName);
		input.setEmail(email);
		input.setName(name);
		input.setStatus(status);
		input.setGroup(group);
		input.toString();

		UserUpdateRequestVo userUpdateRequestVo = new UserUpdateRequestVo();
		userUpdateRequestVo.setFunctionId(functionId);
		userUpdateRequestVo.setCache(cache);
		userUpdateRequestVo.setInput(input);
		userUpdateRequestVo.toString();

		assertEquals(functionId, userUpdateRequestVo.getFunctionId());
		assertEquals(cache, userUpdateRequestVo.getCache());
		assertEquals(input, userUpdateRequestVo.getInput());

	}
}
