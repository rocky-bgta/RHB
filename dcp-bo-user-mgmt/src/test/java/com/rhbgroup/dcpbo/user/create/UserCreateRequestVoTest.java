package com.rhbgroup.dcpbo.user.create;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { UserCreateRequestVo.class })
public class UserCreateRequestVoTest {

	@Test
	public void userCreateRequestVoTest() {

		Integer functionId = 1;
		String username = "LeeYK";
		String email = "lee.yaw.khang@rhbgroup.com";
		String name = "Lee Yaw Khang";
		UserFunctionDepartmentVo department = new UserFunctionDepartmentVo();
		List<UserFunctionUserGroupVo> usergroup = new ArrayList<UserFunctionUserGroupVo>();
		String errorCode = "0";
		String errorDesc = "Success";
		Integer approvalId = 30;
		String isWritten = "N";

		UserCreateRequestVo userCreateRequestVo = new UserCreateRequestVo();
		userCreateRequestVo.setFunctionId(functionId);
		userCreateRequestVo.setUsername(username);
		userCreateRequestVo.setEmail(email);
		userCreateRequestVo.setName(name);
		userCreateRequestVo.setErrorCode(errorCode);
		userCreateRequestVo.setErrorDesc(errorDesc);
		userCreateRequestVo.setApprovalId(approvalId);
		userCreateRequestVo.setIsWritten(isWritten);
		
		department = new UserFunctionDepartmentVo();
		department.setDepartmentId(1);
		department.setDepartmentName("Administrators");
		userCreateRequestVo.setDepartment(department);

		UserFunctionUserGroupVo group = new UserFunctionUserGroupVo();
		group.setGroupId(1);
		group.setGroupName("Admin Maker");
		usergroup.add(group);

		group = new UserFunctionUserGroupVo();
		group.setGroupId(3);
		group.setGroupName("Admin Viewer");
		usergroup.add(group);
		userCreateRequestVo.setUsergroup(usergroup);
		userCreateRequestVo.toString();

		assertEquals(functionId, userCreateRequestVo.getFunctionId());
		assertEquals(username, userCreateRequestVo.getUsername());
		assertEquals(email, userCreateRequestVo.getEmail());
		assertEquals(name, userCreateRequestVo.getName());
		assertEquals(department, userCreateRequestVo.getDepartment());
		assertEquals(usergroup, userCreateRequestVo.getUsergroup());
		assertEquals(errorCode, userCreateRequestVo.getErrorCode());
		assertEquals(errorDesc, userCreateRequestVo.getErrorDesc());
		assertEquals(approvalId, userCreateRequestVo.getApprovalId());
		assertEquals(isWritten, userCreateRequestVo.getIsWritten());

	}
}
