package com.rhbgroup.dcpbo.customer.service.impl;

import com.rhbgroup.dcpbo.customer.contract.BoData;
import com.rhbgroup.dcpbo.customer.dto.AuditPagination;
import com.rhbgroup.dcpbo.customer.dto.TelemetryErrorLog;
import com.rhbgroup.dcpbo.customer.dto.TelemetryErrorLogs;
import com.rhbgroup.dcpbo.customer.exception.CommonException;
import com.rhbgroup.dcpbo.customer.model.DcpTelemetryErrorLog;
import com.rhbgroup.dcpbo.customer.repository.DcpTelemetryErrorLogRepository;
import com.rhbgroup.dcpbo.customer.service.DcpTelemetryErrorLogService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
public class DcpTelemetryErrorLogServiceImpl implements DcpTelemetryErrorLogService {
	
	private final Logger log = LogManager.getLogger(DcpTelemetryErrorLogServiceImpl.class);
	
	private static final int PAGE_SIZE = 20;
	
	private static final long MINUS_MONTHS = 6;
	
	private static final String FORMAT_DATE = "yyyy-MM-dd'T'HH:mm:ssXXX";
    
    private static final String FORMAT_DATE2 = "yyyy-MM-dd HH:mm:ss.SSS";
    
	@Autowired
	private DcpTelemetryErrorLogRepository dcpTelemetryErrorLogRepository;

	@Override
	public BoData listing(String keyword, String fDate, String tDate, Integer pageNo) {
		
		HashMap<String, Timestamp> inputDate = parseInputDate(fDate, tDate);
		String frDate = inputDate.get("frDate").toString();
		String toDate = inputDate.get("toDate").toString();
		
		// Count offset
		Integer offset = (pageNo - 1) * PAGE_SIZE;
		
		// Get data from repository
		List<DcpTelemetryErrorLog> dcpTelemetryErrorLogs = new ArrayList<>();
		Integer totalPageNum = 0;
		if(dcpTelemetryErrorLogRepository.findByAuditDateTimeAndKeyword(keyword, frDate, toDate, offset, PAGE_SIZE) != null && 
				!dcpTelemetryErrorLogRepository.findByAuditDateTimeAndKeyword(keyword, frDate, toDate, offset, PAGE_SIZE).isEmpty()) {
			dcpTelemetryErrorLogs = dcpTelemetryErrorLogRepository.findByAuditDateTimeAndKeyword(keyword, frDate, toDate, offset, PAGE_SIZE);
			totalPageNum = dcpTelemetryErrorLogRepository.countFindByKeywordAndAuditDateTime(keyword, frDate, toDate);
		}
		
		// Construct BoData
		TelemetryErrorLogs telemetryErrorLogs = new TelemetryErrorLogs();
		
		// TelemetryErrorLog
		if(dcpTelemetryErrorLogs.size() > 0) {
			for(DcpTelemetryErrorLog dcpTelemetryErrorLog : dcpTelemetryErrorLogs) {
				TelemetryErrorLog telemetryErrorLog = new TelemetryErrorLog();
				telemetryErrorLog.setAuditDateTime(dcpTelemetryErrorLog.getAuditDateTimeString());
				telemetryErrorLog.setErrorCode(dcpTelemetryErrorLog.getErrorCode());
				telemetryErrorLog.setMessageId(dcpTelemetryErrorLog.getMessageId());
				telemetryErrorLog.setOperationName(dcpTelemetryErrorLog.getOperationName());
				
				telemetryErrorLogs.addTelemetryErrorLog(telemetryErrorLog);
			}
		} else {
			log.warn("No audit log found for user with keyword " + keyword + " from " + frDate + " to " + toDate);
		}
		
		// AuditPagination
		telemetryErrorLogs.setPagination(constructPagination(totalPageNum.intValue(), pageNo));
		
		return telemetryErrorLogs;
	}
	
	private AuditPagination constructPagination(int logSize, int pageNo) {
		AuditPagination pagination = new AuditPagination();
		
		if(logSize < PAGE_SIZE) {
            pagination.setPageIndicator("L");
        } else {
            pagination.setPageIndicator("N");
        }
		
		pagination.setActivityCount(logSize);
		
		pagination.setPageNum(pageNo);
		
		if(logSize > 0) {
			Integer totalPageNum = (int) Math.ceil(Double.parseDouble(Long.toString(logSize)) / PAGE_SIZE);
			pagination.setTotalPageNum(totalPageNum);
		} else {
			pagination.setTotalPageNum(0);
		}
		
		return pagination;
	}
	
	private HashMap<String, Timestamp> parseInputDate(String frDateStr, String toDateStr) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(FORMAT_DATE);
        
        LocalDateTime localDateTimeFrom = null;
        LocalDateTime localDateTimeTo = null;
        
        // If to date empty, pre-select to Today
        if (toDateStr != null && !toDateStr.isEmpty()) {
        	localDateTimeTo = LocalDateTime.parse(toDateStr, formatter);
        } else {
        	localDateTimeTo = LocalDateTime.now();
        }
        
        // If from date empty, pre-select to 6 months before toDate
        if (frDateStr != null && !frDateStr.isEmpty()) {
        	localDateTimeFrom = LocalDateTime.parse(frDateStr, formatter);
        } else {
        	if(toDateStr != null && !toDateStr.isEmpty()) {
        		localDateTimeFrom = LocalDateTime.parse(toDateStr, formatter).minusMonths(MINUS_MONTHS);
        	} else {
        		localDateTimeFrom = localDateTimeTo.minusMonths(MINUS_MONTHS);
        	}
        }
        
        Timestamp frDate = Timestamp.valueOf(localDateTimeFrom);
        Timestamp toDate = Timestamp.valueOf(localDateTimeTo);

        HashMap<String, Timestamp> formattedDate = new HashMap<>();

        formattedDate.put("frDate", frDate);
        formattedDate.put("toDate", toDate);

        return formattedDate;
    }

	@Override
	public BoData getTelemetryErrorLogDetails(String messageId, String auditDateTime) {
	
		List<DcpTelemetryErrorLog> dcpTelemetryErrorLogs = dcpTelemetryErrorLogRepository.findByMessageIdAndAuditDateTime(messageId, auditDateTime);
		if (dcpTelemetryErrorLogs == null || dcpTelemetryErrorLogs.size() == 0) {
			log.error("TelemetryErrorLog not found for messageId: " + messageId +  ", auditDateTime: " + auditDateTime);
			throw new CommonException(CommonException.GENERIC_ERROR_CODE, "TelemetryErrorLog not found for messageId: " + messageId +  ", auditDateTime: " + auditDateTime);
		}
		
		TelemetryErrorLog telemetryErrorLog = new TelemetryErrorLog();
		if(dcpTelemetryErrorLogs.size() > 0) {
			for(DcpTelemetryErrorLog dcpTelemetryErrorLog : dcpTelemetryErrorLogs) {
				telemetryErrorLog.setAuditDateTime(dcpTelemetryErrorLog.getAuditDateTimeString());
				telemetryErrorLog.setErrorCode(dcpTelemetryErrorLog.getErrorCode());
				telemetryErrorLog.setErrorDetails(dcpTelemetryErrorLog.getErrorDetails());
				telemetryErrorLog.setErrorReason(dcpTelemetryErrorLog.getErrorReason());
				telemetryErrorLog.setMessageId(dcpTelemetryErrorLog.getMessageId());
				telemetryErrorLog.setOperationName(dcpTelemetryErrorLog.getOperationName());
			}
		}
		
		return telemetryErrorLog;
	}

}
