package com.rhbgroup.dcpbo.user.common.model.bo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { BoConfigDepartmentTest.class, BoConfigDepartment.class })
public class BoConfigDepartmentTest {

	private static Logger logger = LogManager.getLogger(BoConfigDepartmentTest.class);

	@Test
	public void testBoConfigDepartment() {
		logger.debug("testBoConfigDepartment()");
		Integer id = 1;
		String departmentName = "departmentName";
		String departmentCode = "departmentCode";
		String status = "status";
		Date createdTime = new Date();
		String createdBy = "createdBy";
		Date updatedTime = new Date();
		String updatedBy = "updatedBy";

		BoConfigDepartment boConfigDepartment = new BoConfigDepartment();
		boConfigDepartment.setId(id);
		boConfigDepartment.setDepartmentName(departmentName);
		boConfigDepartment.setDepartmentCode(departmentCode);
		boConfigDepartment.setStatus(status);
		boConfigDepartment.setCreatedTime(createdTime);
		boConfigDepartment.setCreatedBy(createdBy);
		boConfigDepartment.setUpdatedTime(updatedTime);
		boConfigDepartment.setUpdatedBy(updatedBy);

		assertEquals(id, boConfigDepartment.getId());
		assertEquals(departmentName, boConfigDepartment.getDepartmentName());
		assertEquals(departmentCode, boConfigDepartment.getDepartmentCode());
		assertEquals(status, boConfigDepartment.getStatus());
		assertEquals(createdTime, boConfigDepartment.getCreatedTime());
		assertEquals(createdBy, boConfigDepartment.getCreatedBy());
		assertEquals(updatedTime, boConfigDepartment.getUpdatedTime());
		assertEquals(updatedBy, boConfigDepartment.getUpdatedBy());

	}
}
