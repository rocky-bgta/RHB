package com.rhbgroup.dcpbo.user.function.model;

import com.rhbgroup.dcpbo.user.function.model.bo.Module;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import java.sql.Timestamp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ModuleTest {
	private static Logger logger = LogManager.getLogger(ModuleTest.class);

	@Test
	public void testModule() {
		logger.debug("testModule()");
		final Integer id = 1;
		final String moduleName = "Module Name";
		final Timestamp createdTime = new Timestamp(System.currentTimeMillis());
		final String createdBy = "Admin";
		final Timestamp updatedTime = new Timestamp(System.currentTimeMillis());
		final String updatedBy = "Admin";

		Module module = new Module();
		module.setId(id);
		module.setModuleName(moduleName);
		module.setCreatedTime(createdTime);
		module.setCreatedBy(createdBy);
		module.setUpdatedTime(updatedTime);
		module.setUpdatedBy(updatedBy);

		assertEquals(id, module.getId());
		assertEquals(moduleName, module.getModuleName());
		assertEquals(createdTime, module.getCreatedTime());
		assertEquals(createdBy, module.getCreatedBy());
		assertEquals(updatedTime, module.getUpdatedTime());
		assertEquals(updatedBy, module.getUpdatedBy());
	}
}
