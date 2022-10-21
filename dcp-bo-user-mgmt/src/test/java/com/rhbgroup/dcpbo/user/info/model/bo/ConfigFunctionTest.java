package com.rhbgroup.dcpbo.user.info.model.bo;

import com.rhbgroup.dcpbo.user.function.model.bo.Module;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.sql.Timestamp;
import java.util.Date;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { ConfigFunction.class, ConfigFunctionTest.class })
public class ConfigFunctionTest {
	private static Logger logger = LogManager.getLogger(ConfigFunctionTest.class);

	@Test
	public void testConfigFunction() {
		logger.debug("testConfigFunction()");

		Integer id = 1;
		String functionName = "functionName";
		String checkerScope = "checkerScope";
		String makerScope = "makerScope";
		String inquirerScope = "inquirerScope";
		boolean approvalRequired = true;
		Timestamp created_time = new Timestamp(new Date().getTime());
		String created_by = "created_by";
		Timestamp updated_time = new Timestamp(new Date().getTime());
		String updated_by = "updated_by";

		Module module = new Module();
		module.setId(1);
		module.setModuleName("ModuleName");

		ConfigFunction configFunction = new ConfigFunction();
		configFunction.setId(id);
		configFunction.setFunctionName(functionName);
		configFunction.setCheckerScope(checkerScope);
		configFunction.setMakerScope(makerScope);
		configFunction.setInquirerScope(inquirerScope);
		configFunction.setApprovalRequired(approvalRequired);
		configFunction.setCreated_time(created_time);
		configFunction.setCreated_by(created_by);
		configFunction.setUpdated_time(updated_time);
		configFunction.setUpdated_by(updated_by);
		configFunction.setModule(module);

		assertEquals(id, configFunction.getId());
		assertEquals(functionName, configFunction.getFunctionName());
		assertEquals(checkerScope, configFunction.getCheckerScope());
		assertEquals(makerScope, configFunction.getMakerScope());
		assertEquals(inquirerScope, configFunction.getInquirerScope());
		assertEquals(approvalRequired, configFunction.isApprovalRequired());
		assertEquals(created_time, configFunction.getCreated_time());
		assertEquals(created_by, configFunction.getCreated_by());
		assertEquals(updated_time, configFunction.getUpdated_time());
		assertEquals(updated_by, configFunction.getUpdated_by());
		assertEquals(module, configFunction.getModule());

	}
}
