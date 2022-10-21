package com.rhbgroup.dcpbo.customer.service;

import java.text.ParseException;

import com.rhbgroup.dcpbo.customer.dto.AuditType;
import com.rhbgroup.dcpbo.customer.dto.OperationName;
import com.rhbgroup.dcpbo.customer.dto.TelemetryData;
import com.rhbgroup.dcpbo.customer.dto.TelemetryLogPayloadData;

public interface InvestigationLogService {
	OperationName getOperationNames();
	AuditType getAuditTypes();
	TelemetryLogPayloadData getTelemetryPayloadData(String messageId, String auditDateTime) throws ParseException;
	TelemetryData getNewLogs();
	TelemetryData getLogs(String auditType, String keyword, Integer pageNum, String fromDateStr, String toDateStr);
}