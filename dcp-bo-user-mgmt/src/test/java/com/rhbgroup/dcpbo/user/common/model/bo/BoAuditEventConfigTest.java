package com.rhbgroup.dcpbo.user.common.model.bo;

import com.rhbgroup.dcpbo.user.info.model.bo.ConfigFunctionTest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { BoAuditEventConfigTest.class, BoAuditEventConfig.class })
public class BoAuditEventConfigTest {

	private static Logger logger = LogManager.getLogger(ConfigFunctionTest.class);

	@Test
	public void testBoAuditEventConfig() {
		logger.debug("testBoAuditEventConfig()");
		Integer id = 1;
		String eventCode = "eventCode";
		Integer functionId = 1;
		String actionType = "actionType";
		String activityName = "activityName";
		String detailsTableName = "detailsTableName";

		BoAuditEventConfig boAuditEventConfig = new BoAuditEventConfig();
		boAuditEventConfig.setId(id);
		boAuditEventConfig.setEventCode(eventCode);
		boAuditEventConfig.setFunctionId(functionId);
		boAuditEventConfig.setActionType(actionType);
		boAuditEventConfig.setActivityName(activityName);
		boAuditEventConfig.setDetailsTableName(detailsTableName);

		assertEquals(id, boAuditEventConfig.getId());
		assertEquals(eventCode, boAuditEventConfig.getEventCode());
		assertEquals(functionId, boAuditEventConfig.getFunctionId());
		assertEquals(actionType, boAuditEventConfig.getActionType());
		assertEquals(activityName, boAuditEventConfig.getActivityName());
		assertEquals(detailsTableName, boAuditEventConfig.getDetailsTableName());
	}
}
