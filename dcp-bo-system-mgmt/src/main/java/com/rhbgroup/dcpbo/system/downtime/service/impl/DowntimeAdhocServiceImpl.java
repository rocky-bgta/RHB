package com.rhbgroup.dcpbo.system.downtime.service.impl;

import java.sql.Date;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import com.rhbgroup.dcpbo.system.model.BoDowntimeAdhocType;
import jdk.nashorn.internal.ir.FunctionNode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rhbgroup.dcp.util.JsonUtil;
import com.rhbgroup.dcpbo.common.audit.AdditionalDataHolder;
import com.rhbgroup.dcpbo.common.exception.CommonException;
import com.rhbgroup.dcpbo.system.common.BoData;
import com.rhbgroup.dcpbo.system.downtime.dto.Adhoc;
import com.rhbgroup.dcpbo.system.downtime.dto.AdhocCategory;
import com.rhbgroup.dcpbo.system.downtime.dto.AdhocData;
import com.rhbgroup.dcpbo.system.downtime.dto.AdhocType;
import com.rhbgroup.dcpbo.system.downtime.dto.ApprovalDowntimeAdhoc;
import com.rhbgroup.dcpbo.system.downtime.dto.AuditPagination;
import com.rhbgroup.dcpbo.system.downtime.dto.DowntimeAdhoc;
import com.rhbgroup.dcpbo.system.downtime.dto.UpdateApprovalDowntimeAdhoc;
import com.rhbgroup.dcpbo.system.downtime.repository.ApprovalRepository;
import com.rhbgroup.dcpbo.system.downtime.repository.BankRepository;
import com.rhbgroup.dcpbo.system.downtime.repository.BoDowntimeAdhocTypeRepository;
import com.rhbgroup.dcpbo.system.downtime.repository.BoSmApprovalDowntimeRepository;
import com.rhbgroup.dcpbo.system.downtime.repository.ConfigFunctionRepository;
import com.rhbgroup.dcpbo.system.downtime.repository.SystemDowntimeConfigRepository;
import com.rhbgroup.dcpbo.system.downtime.repository.UserRepository;
import com.rhbgroup.dcpbo.system.downtime.service.DowntimeAdhocService;
import com.rhbgroup.dcpbo.system.downtime.vo.AddDowntimeAdhocRequestVo;
import com.rhbgroup.dcpbo.system.downtime.vo.DeleteDowntimeAdhocRequestVo;
import com.rhbgroup.dcpbo.system.downtime.vo.UpdateDowntimeAdhocRequestVo;
import com.rhbgroup.dcpbo.system.enums.ApprovalStatus;
import com.rhbgroup.dcpbo.system.enums.MaintenanceActionType;
import com.rhbgroup.dcpbo.system.exception.AdhocDurationOverlappedException;
import com.rhbgroup.dcpbo.system.exception.DeleteAdhocNotAllowedException;
import com.rhbgroup.dcpbo.system.exception.PendingApprovalException;
import com.rhbgroup.dcpbo.system.model.BoSmApprovalDowntime;
import com.rhbgroup.dcpbo.system.model.ConfigFunction;
import com.rhbgroup.dcpbo.system.model.SystemDowntimeConfig;

@Service
public class DowntimeAdhocServiceImpl implements DowntimeAdhocService {

	private static Logger logger = LogManager.getLogger(DowntimeAdhocServiceImpl.class);
	
	private static final String FORMAT_DATETIME = "yyyy-MM-dd'T'HH:mm:ssXXX";
	
	private static final String FORMAT_DATE = "yyyy-MM-dd";
	
	private static final String ADHOC_TYPE = "ADHOC";
	
	private static final String IS_ACTIVE_1 = "1";
	
	private static final String IS_ACTIVE_0 = "0";
	
        private static final String STRING_DATE_REPLACEMENT = "+";

        private static final String STRING_DATE_PLUS = "%2B";

        private static final int PAGE_SIZE = 15;

        private static final String ADHOC_LIST = "adhocList";

        private static final String TOTAL_COUNT = "totalCount";

        private static final String TOTAL_PAGES = "totalPages";
    
	private static final String STATUS_ACTIVE = "Active";
	
	private static final String STATUS_INACTIVE = "Inactive";
	
	private static final String BANK_ID = "bankId";   
	
	private static final String REFERENCE_NUMBER = "referenceNumber";
	
	private static final String ADHOC_TYP = "adhocType";
	
	private static final String ADHOC_NAME = "adhocName";
	
	private static final String ADHOC_CATEGORY = "adhocCategory";
	
	private static final String BANK_NAME = "bankName";
	
	private static final String CUSTOM_FIELD = "customField"; 
	
	private static final String START_TIME = "startTime";
	
	private static final String END_TIME = "endTime";
	private static final String  USER_NOT_VALID="User not valid for id ";
	private static final String  FUNCTION_ID_NOT_VALID="Function ID cannot be null.";
	@Autowired
	private ConfigFunctionRepository configFunctionRepository;
	
	@Autowired
	private SystemDowntimeConfigRepository systemDowntimeConfigRepository;
	
	@Autowired
	private ApprovalRepository approvalRepository;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private BoSmApprovalDowntimeRepository boSmApprovalDowntimeRepository;
	
    @Autowired
    AdditionalDataHolder additionalDataHolder;
	
	@Autowired
	private BoDowntimeAdhocTypeRepository boDowntimeAdhocTypeRepository;
	
	@Autowired
	private BankRepository bankRepository;

	StringBuilder sb = new StringBuilder(1024);
    Formatter formatter = new Formatter(sb);
	
	@Override
	public ResponseEntity<BoData> addDowntimeAdhoc(AddDowntimeAdhocRequestVo addDowntimeAdhocRequestVo, String userId) {
		List<Integer> approvalIdLs;
		Integer approvalId = 0;
		
		Timestamp now = new Timestamp(System.currentTimeMillis());
		ObjectMapper objectMapper = new ObjectMapper();
		
		logger.debug(formatter.format("Start time: %s" , addDowntimeAdhocRequestVo.getStartTime()));
		logger.debug(formatter.format("End time: %s" , addDowntimeAdhocRequestVo.getEndTime()));
		logger.debug(formatter.format("Push date: %s" , addDowntimeAdhocRequestVo.getPushDate()));
		logger.debug(formatter.format("Is Push Notification: %s" , addDowntimeAdhocRequestVo.isPushNotification()));
//		throw exception
		addDowntimeAdhocThrowsException(addDowntimeAdhocRequestVo);

		DateTimeFormatter dateTimeformatter = DateTimeFormatter.ofPattern(FORMAT_DATETIME);
		DateTimeFormatter dateformatter = DateTimeFormatter.ofPattern(FORMAT_DATE);
		Timestamp startTime= parseInputDateTime(addDowntimeAdhocRequestVo.getStartTime(), dateTimeformatter);
		Timestamp endTime= parseInputDateTime(addDowntimeAdhocRequestVo.getEndTime(), dateTimeformatter);
		String adhocType= addDowntimeAdhocRequestVo.getAdhocType();
		String adhocCategory= addDowntimeAdhocRequestVo.getAdhocCategory();
		
		Integer bankId = getBankId(addDowntimeAdhocRequestVo.getBankId());
		
		Date pushDate = null;
		if (addDowntimeAdhocRequestVo.isPushNotification()) {
			if (addDowntimeAdhocRequestVo.getPushDate() == null) {
				throw new CommonException(CommonException.GENERIC_ERROR_CODE, "Push date is null");
			}
			pushDate = new Date(parseInputDate(addDowntimeAdhocRequestVo.getPushDate(), dateformatter).getTime());
		}
		
		//retrieve user name
		Integer userIdInt = Integer.valueOf(userId);
		String userName = userRepository.findNameById(userIdInt);
		if (userName == null){
			throw new CommonException(CommonException.GENERIC_ERROR_CODE,USER_NOT_VALID + userId);
		}
		
		//check if adhoc downtime duration is overlapped
		List<SystemDowntimeConfig> systemDowntimeConfigs = findByStartTimeAndEndTime(startTime, endTime,adhocType,adhocCategory,bankId);
		
		if (!systemDowntimeConfigs.isEmpty()) {
			throw new AdhocDurationOverlappedException();
		}
		
		ResponseEntity<BoData> responseEntity;
		DowntimeAdhoc downtimeAdhoc  = new DowntimeAdhoc();
		
		//check whether approval is required in db
		ConfigFunction configFunction = configFunctionRepository.findOne(addDowntimeAdhocRequestVo.getFunctionId());
		boolean requireApproval = configFunction.isApprovalRequired();
		
		if (requireApproval) { // approval is required
			String payload = "";
			
			String startTimeB = new SimpleDateFormat(FORMAT_DATETIME).format(startTime);
			String endTimeB = new SimpleDateFormat(FORMAT_DATETIME).format(endTime);
	
			ApprovalDowntimeAdhoc approvalDowntimeAdhoc = new ApprovalDowntimeAdhoc();
			approvalDowntimeAdhoc.setName(addDowntimeAdhocRequestVo.getName());
			approvalDowntimeAdhoc.setStartTime(startTimeB);
			approvalDowntimeAdhoc.setEndTime(endTimeB);
			approvalDowntimeAdhoc.setIsPushNotification(addDowntimeAdhocRequestVo.isPushNotification());
			approvalDowntimeAdhoc.setPushDate(addDowntimeAdhocRequestVo.getPushDate());
			approvalDowntimeAdhoc.setBankId(addDowntimeAdhocRequestVo.getBankId());
			approvalDowntimeAdhoc.setBankName(addDowntimeAdhocRequestVo.getBankName());
			approvalDowntimeAdhoc.setType(ADHOC_TYPE);
			approvalDowntimeAdhoc.setAdhocType(addDowntimeAdhocRequestVo.getAdhocType());
			approvalDowntimeAdhoc.setAdhocCategory(addDowntimeAdhocRequestVo.getAdhocCategory());
			try {
				payload = objectMapper.writeValueAsString(approvalDowntimeAdhoc);
			} catch (JsonProcessingException e) {
				logger.error(e);
			}
			logger.debug(formatter.format("Payload: %s" , payload));
			
			//Write to approval table
			String description = addDowntimeAdhocRequestVo.getName();

			approvalIdLs =  approvalRepository.insert(addDowntimeAdhocRequestVo.getFunctionId(), userIdInt, description, MaintenanceActionType.ADD.getValue(),ApprovalStatus.PENDING_APPROVAL.getValue(), now, userName, now, userName);
			if (approvalIdLs.isEmpty()){
				logger.warn("Write to approval repository failed");
			}
			approvalId = approvalIdLs.get(0);
			boSmApprovalDowntimeRepository.insert(approvalId,MaintenanceActionType.DOWNTIME_APPROVAL_STATE_A .getValue(),null,payload,now,userName,now,userName);
			
			downtimeAdhoc.setApprovalId(approvalId);
			responseEntity = new ResponseEntity<>(downtimeAdhoc, HttpStatus.OK);
			
		} else { //approval not required. insert into table immediately
			SystemDowntimeConfig systemDowntimeConfig = new SystemDowntimeConfig();
			systemDowntimeConfig.setName(addDowntimeAdhocRequestVo.getName());
			systemDowntimeConfig.setStartTime(startTime);
			systemDowntimeConfig.setEndTime(endTime);
			systemDowntimeConfig.setPushNotification(addDowntimeAdhocRequestVo.isPushNotification());
			systemDowntimeConfig.setPushDate(pushDate);
			systemDowntimeConfig.setType(ADHOC_TYPE);
			systemDowntimeConfig.setIsActive(IS_ACTIVE_1);
			systemDowntimeConfig.setAdhocType(addDowntimeAdhocRequestVo.getAdhocType());
			systemDowntimeConfig.setCreatedBy(userName);
			systemDowntimeConfig.setCreatedTime(now);
			systemDowntimeConfig.setUpdatedBy(userName);
			systemDowntimeConfig.setUpdatedTime(now);
			systemDowntimeConfig.setBankId(bankId);
			systemDowntimeConfig.setAdhocTypeCategory(addDowntimeAdhocRequestVo.getAdhocCategory());
			systemDowntimeConfigRepository.saveAndFlush(systemDowntimeConfig);
			responseEntity = new ResponseEntity<>(downtimeAdhoc, HttpStatus.CREATED);
		}
		
		// Publish additional custom data to audit queue
		HashMap<String, Object> additionalDataMap = new HashMap<>();
		HashMap<String, Object> additionalSubDataMap = new HashMap<>();
		additionalSubDataMap.put(REFERENCE_NUMBER, approvalId);
		additionalSubDataMap.put(ADHOC_TYP, addDowntimeAdhocRequestVo.getAdhocType());
		additionalSubDataMap.put(ADHOC_NAME, addDowntimeAdhocRequestVo.getName());
		additionalSubDataMap.put(ADHOC_CATEGORY, addDowntimeAdhocRequestVo.getAdhocCategory());
		additionalDataMap.put(CUSTOM_FIELD, additionalSubDataMap);
        additionalDataHolder.setMap(additionalDataMap);
        logger.debug(formatter.format("audit log data: %s", JsonUtil.objectToJson(additionalDataHolder.getMap())));
		
		return responseEntity;
	}

	public void addDowntimeAdhocThrowsException(AddDowntimeAdhocRequestVo addDowntimeAdhocRequestVo){
		if (addDowntimeAdhocRequestVo.getName() == null) {
			throw new CommonException(CommonException.GENERIC_ERROR_CODE, "Name cannot be null.");
		}
		if (addDowntimeAdhocRequestVo.getFunctionId() == 0) {
			throw new CommonException(CommonException.GENERIC_ERROR_CODE,FUNCTION_ID_NOT_VALID);
		}
		if (addDowntimeAdhocRequestVo.getStartTime() == null) {
			throw new CommonException(CommonException.GENERIC_ERROR_CODE, "Start time cannot be null.");
		}
		if (addDowntimeAdhocRequestVo.getEndTime() == null) {
			throw new CommonException(CommonException.GENERIC_ERROR_CODE, "End time cannot be null.");
		}
		if (addDowntimeAdhocRequestVo.getAdhocType() == null) {
			throw new CommonException(CommonException.GENERIC_ERROR_CODE, "Adhoc type cannot be null.");
		}
		if (addDowntimeAdhocRequestVo.getAdhocCategory() == null) {
			throw new CommonException(CommonException.GENERIC_ERROR_CODE, "Adhoc Category cannot be null.");
		}
	}
	
	private List<SystemDowntimeConfig> findByStartTimeAndEndTime(Timestamp startTime, Timestamp endTime,
			String adhocType, String adhocCategory, Integer bankId) {
		List<SystemDowntimeConfig> systemDowntimeConfigs;
		if(bankId!=null)
			systemDowntimeConfigs= systemDowntimeConfigRepository.findByStartTimeAndEndTime(startTime, endTime,adhocType,adhocCategory,bankId);
		else
			systemDowntimeConfigs= systemDowntimeConfigRepository.findByStartTimeAndEndTime(startTime, endTime,adhocType,adhocCategory);
		return systemDowntimeConfigs;
	}
	
	private List<SystemDowntimeConfig> findByStartTimeAndEndTimeForUpdate(Timestamp startTime, Timestamp endTime, int id,
			String adhocType, String adhocCategory, Integer bankId) {
		List<SystemDowntimeConfig> systemDowntimeConfigs;
		if(bankId!=null)
			systemDowntimeConfigs= systemDowntimeConfigRepository.findByStartTimeAndEndTimeForUpdate(startTime, endTime, id, adhocType, adhocCategory, bankId);
		else
			systemDowntimeConfigs= systemDowntimeConfigRepository.findByStartTimeAndEndTimeForUpdate(startTime, endTime, id, adhocType, adhocCategory);
		return systemDowntimeConfigs;
	}


	private Integer getBankId(String bankIdStr) {
		Integer bankId= null;
		if(bankIdStr != null) {
			bankId= Integer.valueOf(bankIdStr);
		}
		return bankId;
	}


	@Override
	public ResponseEntity<BoData> updateDowntimeAdhoc(UpdateDowntimeAdhocRequestVo updateDowntimeAdhocRequestVo, int id, String userId) {
		List<Integer> approvalIdLs;
		Integer approvalId = 0;
		
		Timestamp now = new Timestamp(System.currentTimeMillis());
		ObjectMapper objectMapper = new ObjectMapper();
		
		logger.debug(formatter.format("Start time: %s" , updateDowntimeAdhocRequestVo.getStartTime()));
		logger.debug(formatter.format("End time: %s" , updateDowntimeAdhocRequestVo.getEndTime()));
		logger.debug(formatter.format("Push date: %s" , updateDowntimeAdhocRequestVo.getPushDate()));
		logger.debug(formatter.format("Is Push Notification: %s" , updateDowntimeAdhocRequestVo.isPushNotification()));
		
		if (id == 0) {
            throw new CommonException(CommonException.GENERIC_ERROR_CODE, "ID cannot be null.");
        }
		if (updateDowntimeAdhocRequestVo.getName() == null) {
            throw new CommonException(CommonException.GENERIC_ERROR_CODE, "Name cannot be null.");
        }
		if (updateDowntimeAdhocRequestVo.getFunctionId() == 0) {
            throw new CommonException(CommonException.GENERIC_ERROR_CODE, FUNCTION_ID_NOT_VALID);
        }
		if (updateDowntimeAdhocRequestVo.getStartTime() == null) {
            throw new CommonException(CommonException.GENERIC_ERROR_CODE, "Start time cannot be null.");
        }
		if (updateDowntimeAdhocRequestVo.getEndTime() == null) {
            throw new CommonException(CommonException.GENERIC_ERROR_CODE, "End time cannot be null.");
        }
		if (updateDowntimeAdhocRequestVo.getAdhocType() == null) {
            throw new CommonException(CommonException.GENERIC_ERROR_CODE, "Adhoc type cannot be null.");
        }
                if (updateDowntimeAdhocRequestVo.getAdhocCategory() == null) {
            throw new CommonException(CommonException.GENERIC_ERROR_CODE, "Adhoc Category cannot be null.");
	}
                
		DateTimeFormatter dateTimeformatter = DateTimeFormatter.ofPattern(FORMAT_DATETIME);
		DateTimeFormatter dateformatter = DateTimeFormatter.ofPattern(FORMAT_DATE);
		Timestamp startTime = parseInputDateTime(updateDowntimeAdhocRequestVo.getStartTime(), dateTimeformatter);
		Timestamp endTime = parseInputDateTime(updateDowntimeAdhocRequestVo.getEndTime(), dateTimeformatter);
		String adhocType= updateDowntimeAdhocRequestVo.getAdhocType();
		String adhocCategory= updateDowntimeAdhocRequestVo.getAdhocCategory();
		
		Integer bankId=getBankId(updateDowntimeAdhocRequestVo.getBankId());
		
		Date pushDate = null;
		if (updateDowntimeAdhocRequestVo.isPushNotification()) {
			if (updateDowntimeAdhocRequestVo.getPushDate() == null) {
				throw new CommonException(CommonException.GENERIC_ERROR_CODE, "Push date cannot be null");
			}
			pushDate = new Date(parseInputDate(updateDowntimeAdhocRequestVo.getPushDate(), dateformatter).getTime());
		}
		
		//retrieve user name
		Integer userIdInt = Integer.valueOf(userId);
		String userName = userRepository.findNameById(userIdInt);
		if (userName == null){
			throw new CommonException(CommonException.GENERIC_ERROR_CODE,USER_NOT_VALID+ userId);
		}
		
		//check if adhoc downtime duration is overlapped
		List<SystemDowntimeConfig> systemDowntimeConfigs= findByStartTimeAndEndTimeForUpdate(startTime, endTime, id, adhocType, adhocCategory, bankId);
			
		if (!systemDowntimeConfigs.isEmpty()) {
			throw new AdhocDurationOverlappedException();
		}
		
		ResponseEntity<BoData> responseEntity;
		DowntimeAdhoc downtimeAdhoc  = new DowntimeAdhoc();
		
		//retrieve existing record from DB
		SystemDowntimeConfig systemDowntimeConfigExisting = systemDowntimeConfigRepository.findOneById(id);
		if (systemDowntimeConfigExisting == null) {
			throw new CommonException(CommonException.GENERIC_ERROR_CODE,"System downtime for adhoc not valid for id: " + id);
		}
		
		//check whether approval is required in db
		ConfigFunction configFunction = configFunctionRepository.findOne(updateDowntimeAdhocRequestVo.getFunctionId());
		boolean requireApproval = configFunction.isApprovalRequired();
		
		if (requireApproval) { // approval is required
			String lockingId = Integer.toString(id);

			// search for existing approval list by function id and status = "P"
			List<Integer> approvalIds =  approvalRepository.findIdByFunctionIdAndStatus(updateDowntimeAdhocRequestVo.getFunctionId(), 
					ApprovalStatus.PENDING_APPROVAL.getValue());
			
			if (approvalIds != null && !approvalIds.isEmpty()) {
				List<BoSmApprovalDowntime> boSmApprovalDowntimes = boSmApprovalDowntimeRepository.findByApprovalIdAndLockingId(approvalIds, lockingId);
				if(!boSmApprovalDowntimes.isEmpty()){
					throw new PendingApprovalException();
				}
			}
			
			//Write to approval table
			String description = updateDowntimeAdhocRequestVo.getName();
			approvalIdLs =  approvalRepository.insert(updateDowntimeAdhocRequestVo.getFunctionId(), userIdInt, description, MaintenanceActionType.EDIT.getValue(),
					ApprovalStatus.PENDING_APPROVAL.getValue(), now, userName, now, userName);
			if (approvalIdLs.isEmpty()){
				logger.warn("Write to approval repository failed");
			}
			approvalId = approvalIdLs.get(0);
			
			//Payload for before update - "B"
			String startTimeB = new SimpleDateFormat(FORMAT_DATETIME).format(systemDowntimeConfigExisting.getStartTime());
			String endTimeB = new SimpleDateFormat(FORMAT_DATETIME).format(systemDowntimeConfigExisting.getEndTime());
			
			String pushDateB = null;
			if (systemDowntimeConfigExisting.getPushDate() != null) {
				pushDateB = new SimpleDateFormat(FORMAT_DATE).format(systemDowntimeConfigExisting.getPushDate());
			}
			
			String adhocTypeNameExisting = systemDowntimeConfigExisting.getAdhocType();
			
			if(adhocTypeNameExisting==null || adhocTypeNameExisting.isEmpty()) {
			   String errMsg = "Adhoc Type is not valid ";
				logger.error(errMsg);
				throw new CommonException(CommonException.GENERIC_ERROR_CODE, errMsg);
			}
			
			String payloadB = "";
			UpdateApprovalDowntimeAdhoc updateApprovalDowntimeAdhocB = new UpdateApprovalDowntimeAdhoc();
			updateApprovalDowntimeAdhocB.setId(systemDowntimeConfigExisting.getId());
			updateApprovalDowntimeAdhocB.setName(systemDowntimeConfigExisting.getName());
			updateApprovalDowntimeAdhocB.setStartTime(startTimeB);
			updateApprovalDowntimeAdhocB.setEndTime(endTimeB);
			updateApprovalDowntimeAdhocB.setIsPushNotification(systemDowntimeConfigExisting.isPushNotification());
			updateApprovalDowntimeAdhocB.setPushDate(pushDateB);
			updateApprovalDowntimeAdhocB.setType(systemDowntimeConfigExisting.getType());
			updateApprovalDowntimeAdhocB.setAdhocType(adhocTypeNameExisting);
			updateApprovalDowntimeAdhocB.setBankId(systemDowntimeConfigExisting.getBankId()!=null ? String.valueOf(systemDowntimeConfigExisting.getBankId()) : null);
			updateApprovalDowntimeAdhocB.setAdhocCategory(systemDowntimeConfigExisting.getAdhocTypeCategory());
			updateApprovalDowntimeAdhocB.setBankName(getBankNameById(systemDowntimeConfigExisting.getBankId()));
			try {
				payloadB = objectMapper.writeValueAsString(updateApprovalDowntimeAdhocB);
			} catch (JsonProcessingException e) {
				logger.error(e);
			}
			
			//Payload for after update - "A"
			String payloadA = "";
			UpdateApprovalDowntimeAdhoc updateApprovalDowntimeAdhocA = new UpdateApprovalDowntimeAdhoc();
			updateApprovalDowntimeAdhocA.setId(id);
			updateApprovalDowntimeAdhocA.setName(updateDowntimeAdhocRequestVo.getName());
			updateApprovalDowntimeAdhocA.setStartTime(updateDowntimeAdhocRequestVo.getStartTime());
			updateApprovalDowntimeAdhocA.setEndTime(updateDowntimeAdhocRequestVo.getEndTime());
			updateApprovalDowntimeAdhocA.setIsPushNotification(updateDowntimeAdhocRequestVo.isPushNotification());
			updateApprovalDowntimeAdhocA.setPushDate(updateDowntimeAdhocRequestVo.getPushDate());
			updateApprovalDowntimeAdhocA.setType(ADHOC_TYPE);
			updateApprovalDowntimeAdhocA.setAdhocType(updateDowntimeAdhocRequestVo.getAdhocType());
			updateApprovalDowntimeAdhocA.setBankId(updateDowntimeAdhocRequestVo.getBankId());
			updateApprovalDowntimeAdhocA.setAdhocCategory(updateDowntimeAdhocRequestVo.getAdhocCategory());
			updateApprovalDowntimeAdhocA.setBankName(getBankNameById(bankId));
			try {
				payloadA = objectMapper.writeValueAsString(updateApprovalDowntimeAdhocA);
			} catch (JsonProcessingException e) {
				logger.error(e);
			}
			logger.debug(formatter.format("PayloadA: %s" , payloadA));
			
			//insert into approval downtime table
			boSmApprovalDowntimeRepository.insert(approvalId,MaintenanceActionType.DOWNTIME_APPROVAL_STATE_B .getValue(),lockingId,payloadB,now,userName,now,userName);
			boSmApprovalDowntimeRepository.insert(approvalId,MaintenanceActionType.DOWNTIME_APPROVAL_STATE_A .getValue(),lockingId,payloadA,now,userName,now,userName);
			
			downtimeAdhoc.setApprovalId(approvalId);
			responseEntity = new ResponseEntity<>(downtimeAdhoc, HttpStatus.OK);
			
		} else { //approval not required. update into table immediately
			String adhocTyp = updateDowntimeAdhocRequestVo.getAdhocType();
			
			if(adhocTyp==null || adhocTyp.isEmpty()) {
			   String errMsg = "Adhoc Type is not valid ";
				logger.error(errMsg);
				throw new CommonException(CommonException.GENERIC_ERROR_CODE, errMsg);
			}

			systemDowntimeConfigExisting.setName(updateDowntimeAdhocRequestVo.getName());
			systemDowntimeConfigExisting.setStartTime(startTime);
			systemDowntimeConfigExisting.setEndTime(endTime);
			systemDowntimeConfigExisting.setPushNotification(updateDowntimeAdhocRequestVo.isPushNotification());
			systemDowntimeConfigExisting.setPushDate(pushDate);
			systemDowntimeConfigExisting.setType(ADHOC_TYPE);
			systemDowntimeConfigExisting.setAdhocType(adhocTyp);
			systemDowntimeConfigExisting.setUpdatedBy(userName);
			systemDowntimeConfigExisting.setUpdatedTime(now);
			systemDowntimeConfigExisting.setBankId(bankId);
			systemDowntimeConfigExisting.setAdhocTypeCategory(updateDowntimeAdhocRequestVo.getAdhocCategory());
			systemDowntimeConfigRepository.saveAndFlush(systemDowntimeConfigExisting);
			
			responseEntity = new ResponseEntity<>(downtimeAdhoc, HttpStatus.CREATED);
		}
		
		//publish additional data
		HashMap<String, Object> additionalDataMap = new HashMap<>();
		HashMap<String, Object> additionalSubDataMap = new HashMap<>();
		additionalSubDataMap.put("id", id);
		additionalSubDataMap.put("name", updateDowntimeAdhocRequestVo.getName());
		additionalSubDataMap.put(START_TIME, updateDowntimeAdhocRequestVo.getStartTime());
		additionalSubDataMap.put(END_TIME, updateDowntimeAdhocRequestVo.getEndTime());
		additionalSubDataMap.put("isPushNotification", updateDowntimeAdhocRequestVo.isPushNotification());
		additionalSubDataMap.put("pushDate", updateDowntimeAdhocRequestVo.getPushDate());
		additionalSubDataMap.put("type", ADHOC_TYPE);
		additionalSubDataMap.put(ADHOC_TYP, updateDowntimeAdhocRequestVo.getAdhocType());
		additionalSubDataMap.put(BANK_ID, updateDowntimeAdhocRequestVo.getBankId());
		additionalSubDataMap.put(ADHOC_CATEGORY, updateDowntimeAdhocRequestVo.getAdhocCategory());
		additionalSubDataMap.put(BANK_NAME, getBankNameById(bankId));
		additionalDataMap.put("before", additionalSubDataMap);
		
		// Publish additional custom data to audit queue
		HashMap<String, Object> additionalSubDataMap2 = new HashMap<>();
		additionalSubDataMap2.put(REFERENCE_NUMBER, approvalId);
		additionalSubDataMap2.put(ADHOC_TYP, updateDowntimeAdhocRequestVo.getAdhocType());
		additionalSubDataMap2.put(ADHOC_NAME, updateDowntimeAdhocRequestVo.getName());
		additionalSubDataMap2.put(BANK_ID, updateDowntimeAdhocRequestVo.getBankId());
		additionalSubDataMap2.put(ADHOC_CATEGORY, updateDowntimeAdhocRequestVo.getAdhocCategory());
		additionalDataMap.put(CUSTOM_FIELD, additionalSubDataMap2);
	
	    additionalDataHolder.setMap(additionalDataMap);
	    logger.debug(formatter.format("audit log data: %s" , JsonUtil.objectToJson(additionalDataHolder.getMap())));
	
		return responseEntity;
	}
	
	@Override
	public ResponseEntity<BoData> deleteDowntimeAdhoc(DeleteDowntimeAdhocRequestVo deleteDowntimeAdhocRequestVo, int id, String userId) {
		List<Integer> approvalIdLs;
		Integer approvalId = 0;
		
		Timestamp now = new Timestamp(System.currentTimeMillis());
		ObjectMapper objectMapper = new ObjectMapper();
		
		if (id == 0) {
            throw new CommonException(CommonException.GENERIC_ERROR_CODE, "ID cannot be null.");
        }
		
		if (deleteDowntimeAdhocRequestVo.getFunctionId() == 0) {
            throw new CommonException(CommonException.GENERIC_ERROR_CODE, FUNCTION_ID_NOT_VALID);
        }
		
		//retrieve user name
		Integer userIdInt = Integer.valueOf(userId);
		String userName = userRepository.findNameById(userIdInt);
		if (userName == null){
			throw new CommonException(CommonException.GENERIC_ERROR_CODE,USER_NOT_VALID+ userId);
		}
		
		ResponseEntity<BoData> responseEntity;
		DowntimeAdhoc downtimeAdhoc  = new DowntimeAdhoc();
		
		//retrieve existing record from DB
		SystemDowntimeConfig systemDowntimeConfigExisting = systemDowntimeConfigRepository.findOneById(id);
		//check if adhoc downtime is activated (starttime <= server time <= endtime) && is_active = 1)
		if (systemDowntimeConfigExisting == null) {
			throw new CommonException(CommonException.GENERIC_ERROR_CODE,"System downtime for adhoc is not valid for id: " + id);
		} else if ((now.after(systemDowntimeConfigExisting.getStartTime()) || now.equals(systemDowntimeConfigExisting.getStartTime())) 
				&& (now.before(systemDowntimeConfigExisting.getEndTime()) || now.equals(systemDowntimeConfigExisting.getEndTime())) &&
				systemDowntimeConfigExisting.getIsActive().equals(IS_ACTIVE_1)) {
			throw new DeleteAdhocNotAllowedException();			
		} else if (systemDowntimeConfigExisting != null && systemDowntimeConfigExisting.getIsActive().equals(IS_ACTIVE_0)) {
			throw new CommonException(CommonException.GENERIC_ERROR_CODE,"System downtime for adhoc is not active for id: " + id);
		}
		
		//check whether approval is required in db
		ConfigFunction configFunction = configFunctionRepository.findOne(deleteDowntimeAdhocRequestVo.getFunctionId());
		boolean requireApproval = configFunction.isApprovalRequired();
		
		if (requireApproval) { // approval is required
			String lockingId = Integer.toString(id);

			// search for existing approval list by function id and status = "P"
			List<Integer> approvalIds =  approvalRepository.findIdByFunctionIdAndStatus(deleteDowntimeAdhocRequestVo.getFunctionId(), 
					ApprovalStatus.PENDING_APPROVAL.getValue());
			
			//search for existing approval details by approval id and locking id
			if (approvalIds != null && !approvalIds.isEmpty()) {
				List<BoSmApprovalDowntime> boSmApprovalDowntimes = boSmApprovalDowntimeRepository.findByApprovalIdAndLockingId(approvalIds, lockingId);
				if(!boSmApprovalDowntimes.isEmpty()){
					throw new PendingApprovalException();
				}
			}
			
			//Write to approval table
			String description = systemDowntimeConfigExisting.getName();
			approvalIdLs =  approvalRepository.insert(deleteDowntimeAdhocRequestVo.getFunctionId(), userIdInt, description, MaintenanceActionType.DELETE.getValue(),
					ApprovalStatus.PENDING_APPROVAL.getValue(), now, userName, now, userName);
			approvalId = approvalIdLs.get(0);
			
			//Payload for before delete - "B"
			String startTimeB = new SimpleDateFormat(FORMAT_DATETIME).format(systemDowntimeConfigExisting.getStartTime());
			String endTimeB = new SimpleDateFormat(FORMAT_DATETIME).format(systemDowntimeConfigExisting.getEndTime());
			
			String pushDateB = null;
			if (systemDowntimeConfigExisting.getPushDate() != null) {
				pushDateB = new SimpleDateFormat(FORMAT_DATE).format(systemDowntimeConfigExisting.getPushDate());
			}
			
			String payloadB = "";
			UpdateApprovalDowntimeAdhoc updateApprovalDowntimeAdhocB = new UpdateApprovalDowntimeAdhoc();
			updateApprovalDowntimeAdhocB.setId(systemDowntimeConfigExisting.getId());
			updateApprovalDowntimeAdhocB.setName(systemDowntimeConfigExisting.getName());
			updateApprovalDowntimeAdhocB.setStartTime(startTimeB);
			updateApprovalDowntimeAdhocB.setEndTime(endTimeB);
			updateApprovalDowntimeAdhocB.setIsPushNotification(systemDowntimeConfigExisting.isPushNotification());
			updateApprovalDowntimeAdhocB.setPushDate(pushDateB);
			updateApprovalDowntimeAdhocB.setType(systemDowntimeConfigExisting.getType());
			updateApprovalDowntimeAdhocB.setBankId(systemDowntimeConfigExisting.getBankId()!=null?String.valueOf(systemDowntimeConfigExisting.getBankId()):null);
			updateApprovalDowntimeAdhocB.setBankName(getBankNameById(systemDowntimeConfigExisting.getBankId()));
			updateApprovalDowntimeAdhocB.setAdhocType(systemDowntimeConfigExisting.getAdhocType());
			updateApprovalDowntimeAdhocB.setAdhocCategory(systemDowntimeConfigExisting.getAdhocTypeCategory());
			try {
				payloadB = objectMapper.writeValueAsString(updateApprovalDowntimeAdhocB);
			} catch (JsonProcessingException e) {
				logger.error(e);
			}
			logger.debug(formatter.format("PayloadB: %s",  payloadB));
			
			//insert into approval downtime table
			boSmApprovalDowntimeRepository.insert(approvalId,MaintenanceActionType.DOWNTIME_APPROVAL_STATE_B .getValue(),lockingId,payloadB,now,userName,now,userName);
			
			downtimeAdhoc.setApprovalId(approvalId);
			responseEntity = new ResponseEntity<>(downtimeAdhoc, HttpStatus.OK);
			
		} else { //approval not required. update into table immediately
			systemDowntimeConfigExisting.setIsActive(IS_ACTIVE_0);
			systemDowntimeConfigExisting.setUpdatedBy(userName);
			systemDowntimeConfigExisting.setUpdatedTime(now);
			systemDowntimeConfigRepository.saveAndFlush(systemDowntimeConfigExisting);
			
			responseEntity = new ResponseEntity<>(downtimeAdhoc, HttpStatus.CREATED);
		}
		
		//publish additional data
		HashMap<String, Object> additionalDataMap = new HashMap<>();
		HashMap<String, Object> additionalSubDataMap = new HashMap<>();
		additionalSubDataMap.put("id", id);
		additionalSubDataMap.put("name", systemDowntimeConfigExisting.getName());
		additionalSubDataMap.put(START_TIME, systemDowntimeConfigExisting.getStartTime());
		additionalSubDataMap.put(END_TIME, systemDowntimeConfigExisting.getEndTime());
		additionalSubDataMap.put("isPushNotification", systemDowntimeConfigExisting.isPushNotification());
		additionalSubDataMap.put("pushDate", systemDowntimeConfigExisting.getPushDate());
		additionalSubDataMap.put("type", ADHOC_TYPE);
		additionalSubDataMap.put(ADHOC_TYP, systemDowntimeConfigExisting.getAdhocType());
		additionalSubDataMap.put(BANK_ID, systemDowntimeConfigExisting.getBankId());
		additionalSubDataMap.put(BANK_NAME, getBankNameById(systemDowntimeConfigExisting.getBankId()));
		additionalSubDataMap.put(ADHOC_CATEGORY, systemDowntimeConfigExisting.getAdhocTypeCategory());
		additionalDataMap.put("before", additionalSubDataMap);
    
		// Publish additional custom data to audit queue
		HashMap<String, Object> additionalSubDataMap2 = new HashMap<>();
		additionalSubDataMap2.put(REFERENCE_NUMBER, approvalId);
		additionalSubDataMap2.put(ADHOC_TYP, systemDowntimeConfigExisting.getAdhocType());
		additionalSubDataMap2.put(ADHOC_NAME, systemDowntimeConfigExisting.getName());
		additionalSubDataMap2.put(BANK_ID, systemDowntimeConfigExisting.getBankId());
		additionalSubDataMap2.put(BANK_NAME, getBankNameById(systemDowntimeConfigExisting.getBankId()));
		additionalSubDataMap2.put(ADHOC_CATEGORY, systemDowntimeConfigExisting.getAdhocTypeCategory());

		additionalDataMap.put(CUSTOM_FIELD, additionalSubDataMap2);
	
		additionalDataHolder.setMap(additionalDataMap);
        logger.debug(formatter.format("audit log data : {} %s", JsonUtil.objectToJson(additionalDataHolder.getMap())));
        
		return responseEntity;
	}
	
	@Override
	public BoData getDowntimeAdhocs(Integer pageNo, String startTimeStr, String endTimeStr, String adhocCategory,
			String status) {
		logger.debug("Start time: {} " , startTimeStr);
		logger.debug("End time: {} " , endTimeStr);
		logger.debug("adhoc Category:{} ",  adhocCategory);
		logger.debug("status: {} " , status);
		
		AuditPagination auditPagination = new AuditPagination();
	    auditPagination.setPageNum(1);
		Timestamp now = new Timestamp(System.currentTimeMillis());
		DateTimeFormatter dateTimeformatter = DateTimeFormatter.ofPattern(FORMAT_DATETIME);
		Timestamp startTime = null;
		Timestamp endTime = null;
		if (startTimeStr != null && !startTimeStr.isEmpty()) {
			startTime = parseInputDateTime(startTimeStr, dateTimeformatter);
		}
		if (endTimeStr != null && !endTimeStr.isEmpty()) {
			endTime = parseInputDateTime(endTimeStr, dateTimeformatter);
		}
		List<String> categoryList = new ArrayList<>();
		List<String> statusList = new ArrayList<>();
		if (!adhocCategory.equals("ALL")) {
			categoryList = constructListOfString(adhocCategory);
		}
		if (!status.equals("ALL")) {
			statusList = constructListOfString(status);
		}
		HashMap<String, Object> adhocDetails = findByDynamicCriteria(now, startTime, endTime, categoryList, statusList, 
				new PageRequest(pageNo - 1, PAGE_SIZE));
		
		@SuppressWarnings("unchecked")
		List<SystemDowntimeConfig> systemDowntimeConfigs = (List<SystemDowntimeConfig>) adhocDetails.get(ADHOC_LIST);
		
		logger.debug("systemDowntimeConfigs size: {} " , systemDowntimeConfigs.size());
		
		AdhocData adhocData = new AdhocData();
		List<Adhoc> adhoc = systemDowntimeConfigs.stream().filter(k -> k != null)
				.map(k -> new Adhoc(k.getId(), k.getName(), k.getStartTimeString(), k.getEndTimeString(),
						k.isPushNotification(), k.getPushDateString(), k.getStatus(now), k.getAdhocType(),
						k.getAdhocTypeCategory(), getBankNameById(k.getBankId()), k.getBankId()!=null? String.valueOf(k.getBankId()) : null))
				.collect(Collectors.toList());
		adhocData.setAdhoc(adhoc);

         //Constructing pagination
        if(systemDowntimeConfigs.size() < PAGE_SIZE) {
            auditPagination.setPageIndicator("L");
        } else {
            auditPagination.setPageIndicator("N");
        }
        auditPagination.setActivityCount(systemDowntimeConfigs.size());
        auditPagination.setPageNum(pageNo);
        auditPagination.setTotalPageNum((int)adhocDetails.get(TOTAL_PAGES));
        adhocData.setPagination(auditPagination);
 		return adhocData;
	}
	
	public HashMap<String, Object> findByDynamicCriteria(Timestamp now, Timestamp startTime, Timestamp endTime, List<String> categoryList,
			List<String> statusList, Pageable pageable){
		Page page = systemDowntimeConfigRepository.findAll(new Specification<SystemDowntimeConfig>() {
			@Override
			public Predicate toPredicate(Root<SystemDowntimeConfig> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
				List<Predicate> predicates = new ArrayList<>();
				predicates.add(criteriaBuilder.and(criteriaBuilder.equal(root.get("type"), ADHOC_TYPE)));
				predicates.add(criteriaBuilder.and(criteriaBuilder.equal(root.get("isActive"), IS_ACTIVE_1)));
				if(startTime != null) {
					predicates.add(criteriaBuilder.and(criteriaBuilder.greaterThanOrEqualTo(root.get(START_TIME), startTime)));
				}
				if(endTime != null) {
					predicates.add(criteriaBuilder.and(criteriaBuilder.lessThanOrEqualTo(root.get(END_TIME), endTime)));
				}
				if(categoryList != null && !categoryList.isEmpty()) {
					predicates.add(criteriaBuilder.and((root.get("adhocTypeCategory").in(categoryList))));
				}
				if(statusList != null && !statusList.isEmpty()) {
					Predicate predicateForSystemTimeGreaterThanStartTimeAndSystemTimeLessThanEndTime = null;
					Predicate predicateForSystemTimeLessThanStartTime = null;
					for (String status:statusList) {
						if (status.equals(STATUS_ACTIVE)) {// startTime <= system time <= endTime
							Predicate predicateForSystemTimeGreaterThanStartTime = criteriaBuilder.lessThanOrEqualTo(root.get(START_TIME), now);
							Predicate predicateForSystemTimeLessThanEndTime = criteriaBuilder.greaterThanOrEqualTo(root.get(END_TIME), now);
							predicateForSystemTimeGreaterThanStartTimeAndSystemTimeLessThanEndTime =
									criteriaBuilder.and(predicateForSystemTimeGreaterThanStartTime, predicateForSystemTimeLessThanEndTime);
						} else if (status.equals(STATUS_INACTIVE)) { // startTime >= system time
							predicateForSystemTimeLessThanStartTime = criteriaBuilder.greaterThanOrEqualTo(root.get(START_TIME), now);
						}
					}
					if (predicateForSystemTimeGreaterThanStartTimeAndSystemTimeLessThanEndTime != null &&
							predicateForSystemTimeLessThanStartTime != null) {
						predicates.add(criteriaBuilder.or(predicateForSystemTimeGreaterThanStartTimeAndSystemTimeLessThanEndTime,
								predicateForSystemTimeLessThanStartTime));
					} else if (predicateForSystemTimeGreaterThanStartTimeAndSystemTimeLessThanEndTime != null) {
						predicates.add(criteriaBuilder.and(predicateForSystemTimeGreaterThanStartTimeAndSystemTimeLessThanEndTime));
					} else if (predicateForSystemTimeLessThanStartTime != null) {
						predicates.add(criteriaBuilder.and(predicateForSystemTimeLessThanStartTime));
					}
				} else { // query all for active and inactive
					Predicate predicateForSystemTimeGreaterThanStartTime = criteriaBuilder.lessThanOrEqualTo(root.get(START_TIME), now);
					Predicate predicateForSystemTimeLessThanEndTime = criteriaBuilder.greaterThanOrEqualTo(root.get(END_TIME), now);
					Predicate predicateForSystemTimeGreaterThanStartTimeAndSystemTimeLessThanEndTime =
							criteriaBuilder.and(predicateForSystemTimeGreaterThanStartTime, predicateForSystemTimeLessThanEndTime);
					Predicate predicateForSystemTimeLessThanStartTime = criteriaBuilder.greaterThanOrEqualTo(root.get(START_TIME), now);
					predicates.add(criteriaBuilder.or(predicateForSystemTimeGreaterThanStartTimeAndSystemTimeLessThanEndTime,
							predicateForSystemTimeLessThanStartTime));					
				}
				List<Order> orderList = new ArrayList<>();
				orderList.add(criteriaBuilder.asc(root.get(START_TIME)));
				orderList.add(criteriaBuilder.asc(root.get(END_TIME)));
				query.orderBy(orderList);
				return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
			}
		}, pageable);
		
		HashMap<String, Object> adhocResult = new HashMap<>();
		adhocResult.put(TOTAL_COUNT, page.getTotalElements());
		adhocResult.put(TOTAL_PAGES, page.getTotalPages());
		adhocResult.put(ADHOC_LIST, page.getContent());
		
		return adhocResult;
	}

    /**
     * Parse the string of Datetime.
     * @param dateStr
     * @return Timestamp
     */
    public Timestamp parseInputDateTime(String dateStr, DateTimeFormatter formatter) {
      
        LocalDateTime localDateTime = null;
        
        dateStr = dateStr.replaceAll(STRING_DATE_PLUS, STRING_DATE_REPLACEMENT);
        
        if (dateStr != null && !dateStr.isEmpty()) {
        	try {
				localDateTime = LocalDateTime.parse(dateStr, formatter);
			} catch (DateTimeParseException e) {
				throw new CommonException(CommonException.GENERIC_ERROR_CODE,"Invalid datetime format: " + dateStr);
			}
        } 
        
        return Timestamp.valueOf(localDateTime);
    }
    
    /**
     * Parse the string of Date
     * @param dateStr
     * @return Timestamp
     */
    public Timestamp parseInputDate(String dateStr, DateTimeFormatter formatter) {
      
    	LocalDate localDate = null;
        
        if (dateStr != null && !dateStr.isEmpty()) {
        	try {
        		localDate = LocalDate.parse(dateStr, formatter);
			} catch (DateTimeParseException e) {
				throw new CommonException(CommonException.GENERIC_ERROR_CODE,"Invalid date format: " + dateStr);
			}
        } 
        Timestamp timestamp = null;
        if(localDate != null)
        	timestamp= Timestamp.valueOf(localDate.atStartOfDay());
        return timestamp;
    }
    
    public List<String> constructListOfString(String fullString) {
    	String[] fullStrings = fullString.split(",");
        List<String> listOfString = new ArrayList<>();
        for(int i = 0; i < fullStrings.length; i++) {
        	listOfString.add(fullStrings[i]);
        }
        return listOfString;
    }

        @Override
	public ResponseEntity<BoData> getAdhocCategoryList() {
		logger.debug("getAdhocCategoryList()");

		List<String> categoryList = boDowntimeAdhocTypeRepository.getAllAdhocCategoryTypes();

		AdhocCategory adhocCategory = new AdhocCategory();
		adhocCategory.setAdhocCategory(categoryList);

		ResponseEntity<BoData> responseEntity = new ResponseEntity<>(adhocCategory, HttpStatus.OK);
		logger.debug(formatter.format(" responseEntity: %s" , responseEntity));

		return responseEntity;
	}


       	@Override
	public BoData getAdhocTypesList(String category) {
		logger.debug("getAdhocType()");
        AdhocType adhocType = new AdhocType();

		if (category.equals("ALL")) {
			adhocType.getAdhocType().addAll(boDowntimeAdhocTypeRepository.getAllAdhocTypes());
			adhocType.getAdhocTypeNames().addAll(boDowntimeAdhocTypeRepository.getAllAdhocTypeNames());
		} else {
			for (BoDowntimeAdhocType boDowntimeAdhocType:boDowntimeAdhocTypeRepository.getDownTimeAdhocTypeByCategory(category)) {
				adhocType.getAdhocType().add(boDowntimeAdhocType.getAdhocType());
				adhocType.getAdhocTypeNames().add(boDowntimeAdhocType.getAdhocTypeName());
			}
		}

		return adhocType;
	}

	public String getBankNameById(Integer bankId) {
		String bankName = null;
		if(bankId !=null) {
			bankName =  bankRepository.getBankNameById(String.valueOf(bankId));
		}
		
		return bankName;
	}
}
