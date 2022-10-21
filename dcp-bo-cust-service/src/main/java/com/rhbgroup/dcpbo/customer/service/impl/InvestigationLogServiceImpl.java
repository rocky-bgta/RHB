package com.rhbgroup.dcpbo.customer.service.impl;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.rhbgroup.dcpbo.customer.dcpbo.TelemetryAuditType;
import com.rhbgroup.dcpbo.customer.dcpbo.TelemetryOperationName;
import com.rhbgroup.dcpbo.customer.dto.AuditPagination;
import com.rhbgroup.dcpbo.customer.dto.AuditType;
import com.rhbgroup.dcpbo.customer.dto.OperationName;
import com.rhbgroup.dcpbo.customer.dto.Telemetry;
import com.rhbgroup.dcpbo.customer.dto.TelemetryData;
import com.rhbgroup.dcpbo.customer.dto.TelemetryLogPayloadData;
import com.rhbgroup.dcpbo.customer.dto.TelemetryLogPayloadDetails;
import com.rhbgroup.dcpbo.customer.exception.CommonException;
import com.rhbgroup.dcpbo.customer.model.TelemetryLog;
import com.rhbgroup.dcpbo.customer.model.TelemetryLogPayload;
import com.rhbgroup.dcpbo.customer.repository.TelemetryAuditTypeRepository;
import com.rhbgroup.dcpbo.customer.repository.TelemetryLogPayloadRepository;
import com.rhbgroup.dcpbo.customer.repository.TelemetryLogRepository;
import com.rhbgroup.dcpbo.customer.repository.TelemetryOperationNameRepository;
import com.rhbgroup.dcpbo.customer.service.InvestigationLogService;
import com.rhbgroup.dcpbo.customer.utils.DateUtils;


@Service
public class InvestigationLogServiceImpl implements InvestigationLogService {
	
    @Autowired
    private TelemetryOperationNameRepository telemetryOperationNameRepository;

    @Autowired
    private TelemetryAuditTypeRepository telemetryAuditTypeRepository;
    
    @Autowired
    private TelemetryLogRepository telemetryLogRepository;
    
    @Autowired
    private TelemetryLogPayloadRepository telemetryLogPayloadRepository;
    
    public static final int PAGE_SIZE = 20;
    private static Logger logger = LogManager.getLogger(InvestigationLogServiceImpl.class);

    @Override
    public OperationName getOperationNames() {
    	
         List<TelemetryOperationName> operationNames = telemetryOperationNameRepository.findAll();
         List<String> operNames = operationNames.stream().map(k -> k.getOperationName()).collect(Collectors.toList());
         OperationName operationName = new OperationName();
         operationName.setOperationName(operNames);
         return operationName;
    }

	@Override
	public AuditType getAuditTypes() {
		List<TelemetryAuditType> auditTypeList = telemetryAuditTypeRepository.findAll();
		List<String> types = auditTypeList.stream().map(k -> k.getAuditType()).collect(Collectors.toList());
		AuditType auditType = new AuditType();
		auditType.setAuditType(types);
		return auditType;
	}

	@Override
	public TelemetryData getNewLogs() {
		List<TelemetryLog> telemetryLogs = telemetryLogRepository.findTop20();
		TelemetryData telemetryData = new TelemetryData();
		List<Telemetry> telemetry = telemetryLogs.stream()
				.map(k -> new Telemetry(k.getId().getMessageId(), k.getId().getOperationName(), k.getId().getAuditType(), k.getAuditDateTimeString(), k.getUsername(), k.getTotalError()))
				.collect(Collectors.toList());
		telemetryData.setData(telemetry);
		return telemetryData;
	}
	
	@Override
	public TelemetryData getLogs(String auditType, String keyword, Integer pageNum, String fromDateStr, String toDateStr) {
		
		logger.info("auditType=" + auditType + ", keyword=" + keyword + ", fromDateStr=" + fromDateStr + ", toDateStr=" + toDateStr + ", pageNum=" + pageNum);
		//Parse the fromDate and toDate and some processing logic
        HashMap<?, ?> formattedDate = DateUtils.parseInputDate(fromDateStr, toDateStr);
        Timestamp frDate = (Timestamp) formattedDate.get("frDate");
        Timestamp toDate = (Timestamp) formattedDate.get("toDate");
        logger.info("frDate=" + frDate + ", toDate=" + toDate);

        List<TelemetryLog> telemetryLogs = new ArrayList<>();
        Integer totalPageNum = 0;
        Integer offset = 0;
        AuditPagination auditPagination = new AuditPagination();
        auditPagination.setPageNum(1);

        offset = (pageNum - 1) * PAGE_SIZE;
        auditPagination.setPageNum(pageNum);
        keyword = "%" + keyword + "%";

        List<String> auditTypes = new ArrayList<>();

        if (!auditType.equals("ALL")) { // query all records for specific audit types
        	auditTypes = constructAuditTypes(auditType);

        	telemetryLogs = telemetryLogRepository.findByAuditTypeKeywordAndAuditDateTime(auditTypes, keyword, frDate, toDate, offset, PAGE_SIZE);
            totalPageNum = (int) Math.ceil((double) telemetryLogRepository.findByAuditTypeKeywordAndAuditDateTimeCount(auditTypes, keyword, frDate, toDate) / PAGE_SIZE);
            if (telemetryLogs.size() == 0) {
                logger.warn("No telemetry log found for audit type:  " + auditTypes.toString() + " from " + fromDateStr + " to " + toDateStr);
            }
        } else { // query all records regardless of audit type 
        	telemetryLogs = telemetryLogRepository.findByKeywordAndAuditDateTime(keyword, frDate, toDate, offset, PAGE_SIZE);
            totalPageNum = (int) Math.ceil((double) telemetryLogRepository.findByKeywordAndAuditDateTimeCount(keyword, frDate, toDate) / PAGE_SIZE);
            if (telemetryLogs.size() == 0) {
                logger.warn("No telemetry log found for from " + fromDateStr + " to " + toDateStr);
            }
        }
        
		TelemetryData telemetryData = new TelemetryData();
		List<Telemetry> telemetry = telemetryLogs.stream()
				.filter(k -> k != null)
				.map(k -> new Telemetry(k.getId().getMessageId(), k.getId().getOperationName(), k.getId().getAuditType(), k.getAuditDateTimeString(), k.getUsername(), k.getTotalError()))
				.collect(Collectors.toList());
		telemetryData.setData(telemetry);

         //Constructing pagination
        if(telemetryLogs.size() < PAGE_SIZE) {
            auditPagination.setPageIndicator("L");
        } else {
            auditPagination.setPageIndicator("N");
        }
        auditPagination.setActivityCount(telemetryLogs.size());
        auditPagination.setPageNum(pageNum);
        auditPagination.setTotalPageNum(totalPageNum);
        telemetryData.setPagination(auditPagination);

 		return telemetryData;
	}
	
    public List<String> constructAuditTypes(String auditType) {
    	String[] auditTypesStr = auditType.split(",");
        List<String> auditTypes = new ArrayList<>();
        for(int i = 0; i < auditTypesStr.length; i++) {
        	auditTypes.add(auditTypesStr[i]);
        }
        return auditTypes;
    }

	@Override
	public TelemetryLogPayloadData getTelemetryPayloadData(String messageId, String sAuditDateTime) throws ParseException {
		logger.debug("getTelemetryPayload()");
		
		TelemetryLogPayloadData telemetryPayloadData = null;
		List<TelemetryLogPayload> telemetryLogPayload = telemetryLogPayloadRepository.findByMessageIdAndAuditDateTime(messageId, sAuditDateTime);
		if (telemetryLogPayload == null || telemetryLogPayload.size() == 0)
			throw new CommonException(CommonException.GENERIC_ERROR_CODE, "TelemetryLogPayload not found for messageId: " + messageId +  ", sAuditDateTime: " + sAuditDateTime);

		TelemetryLogPayloadDetails telemetryPayloadDetails = new TelemetryLogPayloadDetails(telemetryLogPayload.get(0));
		logger.debug("    telemetryPayloadDetails: " + telemetryPayloadDetails);

		telemetryPayloadData = new TelemetryLogPayloadData(telemetryPayloadDetails);
		logger.debug("    telemetryPayloadData: " + telemetryPayloadData);
		return telemetryPayloadData;
	}
	
	@SuppressWarnings("serial")
	class InvalidTimeFormatException extends Exception {
	}

}
