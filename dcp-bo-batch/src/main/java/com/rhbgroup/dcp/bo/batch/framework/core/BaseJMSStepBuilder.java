package com.rhbgroup.dcp.bo.batch.framework.core;

import com.rhbgroup.dcp.model.Capsule;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;

import com.rhbgroup.dcp.bo.batch.framework.model.JMSConfig;
import com.rhbgroup.dcp.bo.batch.framework.utils.JMSUtils;

public abstract class BaseJMSStepBuilder extends BaseStepBuilder {
	
	protected static final String JSON_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'+08:00'";

	protected static final String ISO_EVENTTIMESTAMP_FORMAT = "yyyy-MM-dd'T'HH:mm:ssXXX";

    protected static final String ISO_TIMEZONE = "GMT+8";

	@Autowired
	@Qualifier("SmsJMSConfig")
	@Lazy
	public JMSConfig smsJMSConfig;
	
	@Autowired
	@Qualifier("AuditJMSConfig")
	@Lazy
	public JMSConfig auditJMSConfig;
	
	protected void sendMessageToSmsJMS(String message) {
		JMSUtils.sendMessageToJMS(message, smsJMSConfig);
	}
	
	protected void sendMessageToAuditJMS(String message) {
		JMSUtils.sendMessageToJMS(message, auditJMSConfig);
	}

    protected void sendCapsuleMessageToSmsJMS(Capsule capsule) {
        JMSUtils.sendCapsuleMessageToJMS(capsule, smsJMSConfig);
    }
}
