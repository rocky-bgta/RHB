package com.rhbgroup.dcpbo.customer.service;

import com.rhbgroup.dcpbo.customer.contract.BoData;

public interface DcpTelemetryErrorLogService {
	
	BoData listing(String keyword, String frDate, String toDate, Integer pageNo);
	
	BoData getTelemetryErrorLogDetails(String messageId, String auditDateTime);

}
