package com.rhbgroup.dcpbo.customer.controller;

import java.text.ParseException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.rhbgroup.dcpbo.customer.audit.collector.BoControllerAudit;
import com.rhbgroup.dcpbo.customer.contract.BoData;
import com.rhbgroup.dcpbo.customer.dto.AuditType;
import com.rhbgroup.dcpbo.customer.dto.OperationName;
import com.rhbgroup.dcpbo.customer.dto.TelemetryData;
import com.rhbgroup.dcpbo.customer.dto.TelemetryLogPayloadData;
import com.rhbgroup.dcpbo.customer.service.DcpTelemetryErrorLogService;
import com.rhbgroup.dcpbo.customer.service.InvestigationLogService;

@RestController
@RequestMapping(path = "/bo/investigation")
public class InvestigationLogController {

	@Autowired
	private InvestigationLogService investigationLogService;
	
	@Autowired
	private DcpTelemetryErrorLogService dcpTelemetryErrorLogService;

	private static Logger logger = LogManager.getLogger(InvestigationLogController.class);

	@GetMapping(path = "/telemetry/operationName", produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
	public @ResponseBody OperationName getOperationName () {
        return investigationLogService.getOperationNames();
	}

	@GetMapping(path = "/telemetry/auditType", produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
	public @ResponseBody AuditType getAuditType() {
        return investigationLogService.getAuditTypes();
	}
	
	@BoControllerAudit(eventCode = "61001")
	@GetMapping(path = "/telemetry/newLogs", produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
	public @ResponseBody TelemetryData getNewLogs() {
        return investigationLogService.getNewLogs();
	}

    @BoControllerAudit(eventCode = "61003")
	@GetMapping(path = "/telemetry/error", produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
	public @ResponseBody BoData getError(
			@RequestParam(value = "keyword", required = true, defaultValue = "") String keyword,
			@RequestParam(value = "fromDate", required = false, defaultValue = "") String fromDate,
			@RequestParam(value = "toDate", required = false, defaultValue = "") String toDate,
			@RequestParam(value = "pageNum", required = true, defaultValue = "1") Integer pageNum) {
        return dcpTelemetryErrorLogService.listing(keyword, fromDate, toDate, pageNum);
	}
    
	@BoControllerAudit(eventCode = "61001")
	@GetMapping(path = "/telemetry", produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
	public @ResponseBody TelemetryData getLogs(
			@RequestParam(value = "auditType", required = true, defaultValue = "ALL") String auditType,
			@RequestParam(value = "keyword", required = true) String keyword,
			@RequestParam(value = "pageNum", required = true, defaultValue = "1") Integer pageNum,
			@RequestParam(value = "fromDate", required = false, defaultValue = "") String fromDate,
			@RequestParam(value = "toDate", required = false, defaultValue = "") String toDate) {
        return investigationLogService.getLogs(auditType, keyword, pageNum, fromDate, toDate);
	}
	
	@BoControllerAudit(eventCode = "61004")
	@GetMapping(path = "/telemetry/{messageId}/{auditDateTime}/errorDetails", produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
	public @ResponseBody BoData getErrorDetails(
			@PathVariable String messageId,
			@PathVariable String auditDateTime) {
        return dcpTelemetryErrorLogService.getTelemetryErrorLogDetails(messageId, auditDateTime);
	}

	@BoControllerAudit(eventCode = "61002")
	@GetMapping(path = "/telemetry/{messageId}/{auditDateTime}/payload", produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
	public @ResponseBody TelemetryLogPayloadData getTelemetryPayload(
			@PathVariable(value = "messageId") String messageId,
			@PathVariable(value = "auditDateTime") String auditDateTime) throws ParseException {
		logger.debug("getTelemetryPayload()");
		logger.debug("    messageId: " + messageId);
		logger.debug("    auditDateTime: " + auditDateTime);

        return investigationLogService.getTelemetryPayloadData(messageId, auditDateTime);
	}

}
