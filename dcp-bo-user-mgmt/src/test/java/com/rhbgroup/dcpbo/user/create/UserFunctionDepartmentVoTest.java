package com.rhbgroup.dcpbo.user.create;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { UserFunctionDepartmentVo.class })
public class UserFunctionDepartmentVoTest {

	@Test
	public void userFunctionDepartmentVoTest() {

		Integer departmentId = 1;
		String departmentName = "Administrative";

		UserFunctionDepartmentVo userFunctionDepartmentVo = new UserFunctionDepartmentVo();
		userFunctionDepartmentVo.setDepartmentId(departmentId);
		userFunctionDepartmentVo.setDepartmentName(departmentName);
		userFunctionDepartmentVo.toString();

		assertEquals(departmentId, userFunctionDepartmentVo.getDepartmentId());
		assertEquals(departmentName, userFunctionDepartmentVo.getDepartmentName());

	}
}
