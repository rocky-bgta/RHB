package com.rhbgroup.dcpbo.system.downtime.service.impl;

import java.sql.Date;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.rhbgroup.dcp.util.JsonUtil;
import com.rhbgroup.dcp.util.range.Range;
import com.rhbgroup.dcp.util.range.Ranges;
import com.rhbgroup.dcpbo.common.audit.AdditionalDataHolder;
import com.rhbgroup.dcpbo.common.exception.CommonException;
import com.rhbgroup.dcpbo.system.common.BoData;
import com.rhbgroup.dcpbo.system.downtime.dto.AdhocApproval;
import com.rhbgroup.dcpbo.system.downtime.dto.ApprovalDowntimeAdhoc;
import com.rhbgroup.dcpbo.system.downtime.dto.BooleanValuePair;
import com.rhbgroup.dcpbo.system.downtime.dto.DowntimeApprovalRequest;
import com.rhbgroup.dcpbo.system.downtime.dto.StringValuePair;
import com.rhbgroup.dcpbo.system.downtime.dto.WorkflowDeleteDowntimeApproval;
import com.rhbgroup.dcpbo.system.downtime.dto.WorkflowDowntimeApproval;
import com.rhbgroup.dcpbo.system.downtime.repository.BoDowntimeAdhocTypeRepository;
import com.rhbgroup.dcpbo.system.downtime.repository.ApprovalRepository;
import com.rhbgroup.dcpbo.system.downtime.repository.BoSmApprovalDowntimeRepository;
import com.rhbgroup.dcpbo.system.downtime.repository.SystemDowntimeConfigRepository;
import com.rhbgroup.dcpbo.system.downtime.repository.UserRepository;
import com.rhbgroup.dcpbo.system.downtime.service.WorkflowDowntimeService;
import com.rhbgroup.dcpbo.system.downtime.vo.ApproveDowntimeAdhocRequestVo;
import com.rhbgroup.dcpbo.system.downtime.vo.DeleteDowntimeAdhocApprovalRequestVo;
import com.rhbgroup.dcpbo.system.downtime.vo.DeleteDowntimeAdhocApprovalResponseVo;
import com.rhbgroup.dcpbo.system.downtime.vo.UpdateDowntimeAdhocApprovalRequestVo;
import com.rhbgroup.dcpbo.system.downtime.vo.UpdateDowntimeAdhocApprovalResponseVo;
import com.rhbgroup.dcpbo.system.enums.ApprovalStatus;
import com.rhbgroup.dcpbo.system.enums.MaintenanceActionType;
import com.rhbgroup.dcpbo.system.exception.AdhocDurationOverlappedException;
import com.rhbgroup.dcpbo.system.exception.DeleteAdhocNotAllowedException;
import com.rhbgroup.dcpbo.system.exception.UpdateAdhocNotAllowedException;
import com.rhbgroup.dcpbo.system.model.Approval;
import com.rhbgroup.dcpbo.system.model.BoSmApprovalDowntime;
import com.rhbgroup.dcpbo.system.model.SystemDowntimeConfig;
import com.rhbgroup.dcpbo.system.model.User;

@Service
public class WorkflowDowntimeServiceImpl implements WorkflowDowntimeService {

	public static final String ADHOC_TYPE = "ADHOC";
	public static final String DATE_FORMAT = "yyyy-MM-dd";
	public static final String TIMESTAMP_FORMAT = "yyyy-MM-dd'T'HH:mm:ssXXX";
	private static final String IS_ACTIVE_1 = "1";
	private static final String IS_ACTIVE_0 = "0";
	public static final String REQUEST_APPROVED = "APPROVED";
	public static final String REQUEST_REJECTED = "REJECTED";
	
	private static final String BANK_ID = "bankId";   
	
	private static final String REFERENCE_NUMBER = "referenceNumber";
	
	private static final String ADHOC_TYP = "adhocType";
	
	private static final String ADHOC_NAME = "adhocName";
	
	private static final String ADHOC_CATEGORY = "adhocCategory";
	
	private static final String BANK_NAME = "bankName";
	
	private static final String START_TIME = "startTime";
	
	private static final String END_TIME = "endTime";
	
	private static final String CUSTOM_FIELD = "customField";

	@Autowired
	ApprovalRepository approvalRepository;

	@Autowired
	BoSmApprovalDowntimeRepository boSmApprovalDowntimeRepository;

	@Autowired
	SystemDowntimeConfigRepository systemDowntimeConfigRepository;
	
	@Autowired
	BoDowntimeAdhocTypeRepository boDowntimeAdhocTypeRepository;

	@Autowired
	UserRepository userRepository;

	@Autowired
	AdditionalDataHolder additionalDataHolder;

	private static Logger logger = LogManager.getLogger(WorkflowDowntimeServiceImpl.class);

	@Override
	public ResponseEntity<BoData> getApproval(Integer approvalId, Integer userId) {
		logger.debug("getApproval()");
		logger.debug("    approvalId: " + approvalId);
		logger.debug("    userId: " + userId);

		Approval approval = approvalRepository.findOne(approvalId);
		if (approval == null) {
			logger.warn("    Cannot find Approval for approvalId: " + approvalId);
			throw new CommonException(CommonException.GENERIC_ERROR_CODE,
					"Cannot find Approval for approvalId: " + approvalId);
		}
		logger.debug("    approval: " + approval);

		String actionType = approval.getActionType();
		String createdBy = approval.getCreatedBy();
		Timestamp createdTime = approval.getCreatedTime();
		String updatedBy = approval.getUpdatedBy();
		Timestamp updatedTime = approval.getUpdatedTime();
		String reason = approval.getReason();
		Integer creatorId = approval.getCreatorId();
		logger.debug("        actionType: " + actionType);
		logger.debug("        createdBy: " + createdBy);
		logger.debug("        createdTime: " + createdTime);
		logger.debug("        updatedBy: " + updatedBy);
		logger.debug("        updatedTime: " + updatedTime);
		logger.debug("        reason: " + reason);
		logger.debug("        creatorId: " + creatorId);

		User user = userRepository.findById(creatorId);
		if (user == null) {
			logger.warn("    Cannot find User for createdBy: " + createdBy);
			throw new CommonException(CommonException.GENERIC_ERROR_CODE,
					"Cannot find User for createdBy: " + createdBy);
		}
		logger.debug("    user: " + user);

		String creatorName = user.getName();
		logger.debug("        creatorName: " + creatorName);

		WorkflowDowntimeApproval workflowDowntimeApproval = new WorkflowDowntimeApproval();

		if (approval.getStatus().equals(MaintenanceActionType.STATUS_APPROVED.getValue())) {
			workflowDowntimeApproval.setApprovalStatus(REQUEST_APPROVED);
		} else if (approval.getStatus().equals(MaintenanceActionType.STATUS_REJECTED.getValue())) {
			workflowDowntimeApproval.setApprovalStatus(REQUEST_REJECTED);
		}
		workflowDowntimeApproval.setApprovalId(approvalId);

		StringValuePair svpName = new StringValuePair();
		StringValuePair svpStartTime = new StringValuePair();
		StringValuePair svpEndTime = new StringValuePair();
		BooleanValuePair bvpPushNotification = new BooleanValuePair();
		StringValuePair svpPushDate = new StringValuePair();
		StringValuePair svpType = new StringValuePair();
		StringValuePair svpAdhocType = new StringValuePair();
		StringValuePair svpAdhocCategory = new StringValuePair();
		StringValuePair svpBankName = new StringValuePair();
		StringValuePair svpBankId = new StringValuePair();
		

		List<BoSmApprovalDowntime> boSmApprovalDowntimeList = boSmApprovalDowntimeRepository
				.findByApprovalId(approvalId);
		if (boSmApprovalDowntimeList == null) {
			logger.warn("    Cannot find BoSmApprovalDowntime list for approvalId: " + approvalId);
			throw new CommonException(CommonException.GENERIC_ERROR_CODE,
					"Cannot find BoSmApprovalDowntime list for approvalId: " + approvalId);
		}
		boSmApprovalDowntimeList.forEach(boSmApprovalDowntime -> {
			logger.debug("    boSmApprovalDowntime: " + boSmApprovalDowntime);
			logger.debug("        state: " + boSmApprovalDowntime.getState());
			logger.debug("        payload: " + boSmApprovalDowntime.getPayload());

			ApprovalDowntimeAdhoc approvalDowntimeAdhoc = JsonUtil.jsonToObject(boSmApprovalDowntime.getPayload(),
					ApprovalDowntimeAdhoc.class);
			logger.debug("    approvalDowntimeAdhoc: " + approvalDowntimeAdhoc);

			if ("A".equals(boSmApprovalDowntime.getState())) { // AFTER
				svpName.setAfter(approvalDowntimeAdhoc.getName());
				svpStartTime.setAfter(approvalDowntimeAdhoc.getStartTime());
				svpEndTime.setAfter(approvalDowntimeAdhoc.getEndTime());
				bvpPushNotification.setAfter(approvalDowntimeAdhoc.getIsPushNotification());
				svpPushDate.setAfter(approvalDowntimeAdhoc.getPushDate());
				svpType.setAfter(approvalDowntimeAdhoc.getType());
				svpAdhocType.setAfter(approvalDowntimeAdhoc.getAdhocType());
				svpAdhocCategory.setAfter(approvalDowntimeAdhoc.getAdhocCategory());
				svpBankName.setAfter(approvalDowntimeAdhoc.getBankName());
				svpBankId.setAfter(approvalDowntimeAdhoc.getBankId());
			} else { // BEFORE
				svpName.setBefore(approvalDowntimeAdhoc.getName());
				svpStartTime.setBefore(approvalDowntimeAdhoc.getStartTime());
				svpEndTime.setBefore(approvalDowntimeAdhoc.getEndTime());
				bvpPushNotification.setBefore(approvalDowntimeAdhoc.getIsPushNotification());
				svpPushDate.setBefore(approvalDowntimeAdhoc.getPushDate());
				svpType.setBefore(approvalDowntimeAdhoc.getType());
				svpAdhocType.setBefore(approvalDowntimeAdhoc.getAdhocType());
				svpAdhocCategory.setBefore(approvalDowntimeAdhoc.getAdhocCategory());
				svpBankName.setBefore(approvalDowntimeAdhoc.getBankName());
				svpBankId.setBefore(approvalDowntimeAdhoc.getBankId());
			}

			workflowDowntimeApproval.setName(svpName);
			workflowDowntimeApproval.setStartTime(svpStartTime);
			workflowDowntimeApproval.setEndTime(svpEndTime);
			workflowDowntimeApproval.setIsPushNotification(bvpPushNotification);
			workflowDowntimeApproval.setPushDate(svpPushDate);
			workflowDowntimeApproval.setType(svpType);
			workflowDowntimeApproval.setAdhocType(svpAdhocType);
			workflowDowntimeApproval.setActionType(actionType);
			workflowDowntimeApproval.setReason(reason);
			workflowDowntimeApproval.setCreatedBy(createdBy);
			workflowDowntimeApproval.setCreatedTime(formatTimestamp(createdTime));
			workflowDowntimeApproval.setUpdatedBy(updatedBy);
			workflowDowntimeApproval.setUpdatedTime(formatTimestamp(updatedTime));
			workflowDowntimeApproval.setCreatorName(creatorName);
			workflowDowntimeApproval.setAdhocCategory(svpAdhocCategory);
			workflowDowntimeApproval.setBankName(svpBankName);
			workflowDowntimeApproval.setBankId(svpBankId);
			if (creatorId == userId)
				workflowDowntimeApproval.setIsCreator("Y");
			else
				workflowDowntimeApproval.setIsCreator("N");
			logger.debug("    workflowDowntimeApproval: " + workflowDowntimeApproval);
		});

		return new ResponseEntity<BoData>(workflowDowntimeApproval, HttpStatus.OK);
	}

	private String formatTimestamp(Timestamp timestamp) {
		if (timestamp == null)
			return "";

		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(User.TIMESTAMP_FORMAT);
		return simpleDateFormat.format(timestamp);
	}

	@Override
	public ResponseEntity<BoData> workflowApproveAdhocDowntime(Integer approvalId,
			ApproveDowntimeAdhocRequestVo request, String userId) {
		logger.debug("createAdhocApproval()");
		logger.debug("    approvalId: " + approvalId);

		Timestamp now = new Timestamp(System.currentTimeMillis());

		// Retrieve name of the current user
		Integer userIdInt = Integer.valueOf(userId);
		String userName = userRepository.findNameById(userIdInt);
		if (userName == null) {
			String errMsg = "Cannot find User for userId: " + userId;
			logger.error(errMsg);
			throw new CommonException(CommonException.GENERIC_ERROR_CODE, errMsg);
		}
		logger.debug("    userName: " + userName);

		// Retrieve Approval
		Approval approval = approvalRepository.findOne(approvalId);
		if (approval == null) {
			String errMsg = "Cannot find Approval for approvalId: " + approvalId;
			logger.error(errMsg);
			throw new CommonException(CommonException.GENERIC_ERROR_CODE, errMsg);
		}
		logger.debug("    approval: " + approval);

		// Retrieve creator's name
		String creatorUserName = userRepository.findNameById(approval.getCreatorId());
		if (creatorUserName == null) {
			String errMsg = "Cannot get creatorUserName for creatorId: " + approval.getCreatorId();
			logger.error(errMsg);
			throw new CommonException(CommonException.GENERIC_ERROR_CODE, errMsg);
		}
		logger.debug("    creatorUserName: " + creatorUserName);

		// Retrieve a list of BoSmApprovalDowntime, and make sure there is at least one
		// item in it
		List<BoSmApprovalDowntime> boSmApprovalDowntimeList = boSmApprovalDowntimeRepository
				.findByApprovalId(approvalId);
		if (boSmApprovalDowntimeList == null || boSmApprovalDowntimeList.size() < 1) {
			String errMsg = "Cannot find BoSmApprovalDowntime for approvalId: " + approvalId;
			logger.error(errMsg);
			throw new CommonException(CommonException.GENERIC_ERROR_CODE, errMsg);
		}
		logger.debug("    boSmApprovalDowntimeList: " + boSmApprovalDowntimeList);

		// Get the first item in the above list
		BoSmApprovalDowntime boSmApprovalDowntime = boSmApprovalDowntimeList.get(0);
		if (boSmApprovalDowntime == null) {
			String errMsg = "Cannot find BoSmApprovalDowntime for approvalId: " + approvalId;
			logger.error(errMsg);
			throw new CommonException(CommonException.GENERIC_ERROR_CODE, errMsg);
		}
		String payload = boSmApprovalDowntime.getPayload();
		if (payload == null) {
			String errMsg = "Null value for payload for BoSmApprovalDowntime where id = "
					+ boSmApprovalDowntime.getId();
			logger.error(errMsg);
			throw new CommonException(CommonException.GENERIC_ERROR_CODE, errMsg);
		}
		logger.debug("    payload:  {} ", payload);

		ApprovalDowntimeAdhoc approvalDowntimeAdhoc = JsonUtil.jsonToObject(payload, ApprovalDowntimeAdhoc.class);
		logger.debug("    approvalDowntimeAdhoc: {} ", approvalDowntimeAdhoc);

		String name = approvalDowntimeAdhoc.getName();
		String type = approvalDowntimeAdhoc.getType();
		String adhocType = approvalDowntimeAdhoc.getAdhocType();
		String adhocCategory = approvalDowntimeAdhoc.getAdhocCategory();
		
		Integer bankId = null;
		if(approvalDowntimeAdhoc.getBankId() != null)
		 bankId = Integer.parseInt(approvalDowntimeAdhoc.getBankId());

		if (adhocType == null || adhocType.equalsIgnoreCase("")) {
			String errMsg = "Invalid Payload Adhoc Type -" + boSmApprovalDowntime.getId();
			logger.error(errMsg);
			throw new CommonException(CommonException.GENERIC_ERROR_CODE, errMsg);
		}

		Timestamp startTime = toTimestamp(approvalDowntimeAdhoc.getStartTime());
		Timestamp endTime = toTimestamp(approvalDowntimeAdhoc.getEndTime());
		boolean isPushNotification = approvalDowntimeAdhoc.getIsPushNotification();
		Date pushDate = null;
		if (approvalDowntimeAdhoc.getPushDate() != null)
			pushDate = toDate(approvalDowntimeAdhoc.getPushDate());
		logger.debug("        name: " + name);
		logger.debug("        type: " + type);
		logger.debug("        adhocType: " + adhocType);
		logger.debug("        startTime: " + startTime);
		logger.debug("        endTime: " + endTime);
		logger.debug("        isPushNotification: " + isPushNotification);
		logger.debug("        pushDate: " + pushDate);

		// Check for overlapping downtime, i.e. existing SystemDowntimeConfig list
		// within DCP database
		logger.debug("    Checking for overlapping downtime");
		List<SystemDowntimeConfig> systemDowntimeConfigList = findByStartTimeEndTimeAdhocTypeAndBankId(startTime, endTime,adhocType,adhocCategory,bankId);
		systemDowntimeConfigList.forEach(systemDowntimeConfig -> {
			logger.debug("        systemDowntimeConfig: " + systemDowntimeConfig);
			if ("1".equals(systemDowntimeConfig.getIsActive())) {
				Timestamp dbStartTime = systemDowntimeConfig.getStartTime();
				Timestamp dbEndTime = systemDowntimeConfig.getEndTime();
				String dbAdhocType = systemDowntimeConfig.getAdhocType();
				
				logger.debug("            dbStartTime: " + dbStartTime);
				logger.debug("            dbEndTime: " + dbEndTime);

				if ((startTime.before(dbEndTime) || startTime.equals(dbEndTime)) && endTime.after(dbStartTime)
						|| endTime.equals(dbStartTime) || adhocType.equals(dbAdhocType)) {
					logger.error("Overlapping downtime");
					throw new AdhocDurationOverlappedException();
				}
			}
		});

		// Approve downtime creation request
		String status = "A";
		String reason = request.getReason();
		String updatedBy = userName;
		Timestamp updatedTime = now;
		logger.debug("    Updating Approval in database");
		logger.debug("        status: " + status);
		logger.debug("        approvalId: " + approvalId);
		logger.debug("        reason: " + reason);
		logger.debug("        updatedBy: " + updatedBy);
		logger.debug("        updatedTime: " + updatedTime);
		approvalRepository.updateStatusById(status, approvalId, reason, updatedBy, updatedTime);

		// Write to downtime table
		SystemDowntimeConfig systemDowntimeConfig = new SystemDowntimeConfig();
		systemDowntimeConfig.setName(name);
		systemDowntimeConfig.setType(type);
		systemDowntimeConfig.setAdhocType(adhocType);
		systemDowntimeConfig.setStartTime(startTime);
		systemDowntimeConfig.setIsActive(IS_ACTIVE_1);
		systemDowntimeConfig.setEndTime(endTime);
		systemDowntimeConfig.setPushNotification(isPushNotification);
		systemDowntimeConfig.setPushDate(pushDate);
		systemDowntimeConfig.setCreatedBy(creatorUserName);
		systemDowntimeConfig.setCreatedTime(approval.getCreatedTime());
		systemDowntimeConfig.setUpdatedBy(updatedBy);
		systemDowntimeConfig.setUpdatedTime(updatedTime);
		systemDowntimeConfig.setAdhocTypeCategory(adhocCategory);
		systemDowntimeConfig.setBankId(bankId);
		
		logger.debug("    Inserting SystemDowntimeConfig into DCP database");
		logger.debug("        systemDowntimeConfig: " + systemDowntimeConfig);
		systemDowntimeConfigRepository.save(systemDowntimeConfig);

		// Publish additional data to audit queue
		HashMap<String, Object> additionalDataMap = new HashMap<String, Object>();
		HashMap<String, Object> additionalSubDataMap = new HashMap<String, Object>();
		additionalSubDataMap.put("id", approvalId);
		additionalSubDataMap.put("name", name);
		additionalSubDataMap.put(START_TIME, approvalDowntimeAdhoc.getStartTime());
		additionalSubDataMap.put(END_TIME, approvalDowntimeAdhoc.getEndTime());
		additionalSubDataMap.put("isPushNotification", isPushNotification);
		additionalSubDataMap.put("pushDate", approvalDowntimeAdhoc.getPushDate());
		additionalSubDataMap.put("type", ADHOC_TYPE);
		additionalSubDataMap.put(ADHOC_TYP, adhocType);
		additionalSubDataMap.put(ADHOC_CATEGORY, adhocCategory);
		additionalSubDataMap.put(BANK_NAME, approvalDowntimeAdhoc.getBankName());
		additionalSubDataMap.put(BANK_ID, bankId);
		additionalDataMap.put("after", additionalSubDataMap);

		// Publish additional custom data to audit queue
		HashMap<String, Object> additionalSubDataMap2 = new HashMap<String, Object>();
		additionalSubDataMap2.put(REFERENCE_NUMBER, approvalId);
		additionalSubDataMap2.put(ADHOC_TYP, approvalDowntimeAdhoc.getAdhocType());
		additionalSubDataMap2.put(ADHOC_NAME, approvalDowntimeAdhoc.getName());
		additionalSubDataMap2.put(ADHOC_CATEGORY, adhocCategory);
		additionalSubDataMap2.put(BANK_NAME, approvalDowntimeAdhoc.getBankName());
		additionalSubDataMap2.put(BANK_ID, bankId);
		additionalDataMap.put(CUSTOM_FIELD, additionalSubDataMap2);

		additionalDataHolder.setMap(additionalDataMap);

		logger.debug("audit log data: " + JsonUtil.objectToJson(additionalDataHolder.getMap()));

		AdhocApproval adhocApproval = new AdhocApproval();
		adhocApproval.setApprovalId(approvalId);
		logger.debug("    adhocApproval: " + adhocApproval);

		return new ResponseEntity<BoData>(adhocApproval, HttpStatus.OK);
	}

	private List<SystemDowntimeConfig> findByStartTimeEndTimeAdhocTypeAndBankId(Timestamp startTime, Timestamp endTime,
			String adhocType, String adhocCategory, Integer bankId) {
		List<SystemDowntimeConfig> systemDowntimeConfigs;
		if(bankId!=null)
			systemDowntimeConfigs= systemDowntimeConfigRepository.findByStartTimeAndEndTime(startTime, endTime,adhocType,adhocCategory,bankId);
		else
			systemDowntimeConfigs= systemDowntimeConfigRepository.findByStartTimeAndEndTime(startTime, endTime,adhocType,adhocCategory);
		return systemDowntimeConfigs;
	}

	private Timestamp toTimestamp(String value) {
		Timestamp timestamp = null;

		try {
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat(TIMESTAMP_FORMAT);
			timestamp = new Timestamp(simpleDateFormat.parse(value).getTime());
		} catch (ParseException e) {
			logger.warn(e);
			logger.warn("Failed to parse Timestamp for value: " + value);
			throw new CommonException(CommonException.GENERIC_ERROR_CODE,
					"Failed to parse Timestamp for value: " + value);
		}

		return timestamp;
	}

	private Date toDate(String value) {
		Date date = null;

		try {
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATE_FORMAT);
			date = new Date(simpleDateFormat.parse(value).getTime());
		} catch (ParseException e) {
			logger.warn(e);
			logger.warn("Failed to parse Date for value: " + value);
			throw new CommonException(CommonException.GENERIC_ERROR_CODE, "Failed to parse Date for value: " + value);
		}

		return date;
	}

	@Override
	public BoData deleteApproval(Integer approvalId,
			DeleteDowntimeAdhocApprovalRequestVo deleteDowntimeApprovalRequestVo, String userId) {

		logger.debug(String.format("approval ID: %s", approvalId));
		logger.debug(String.format("reason: %s", deleteDowntimeApprovalRequestVo.getReason()));
		logger.debug(String.format("user ID: %s", userId));

		String errMsg;
		Timestamp now = new Timestamp(System.currentTimeMillis());

		// retrieve user name
		Integer userIdInt = Integer.valueOf(userId);
		String userName = userRepository.findNameById(userIdInt);
		if (userName == null) {
			errMsg = "User not valid for id: " + userId;
			logger.error(errMsg);
			throw new CommonException(CommonException.GENERIC_ERROR_CODE, errMsg);
		}

		// retrieve approval info
		Approval approval = approvalRepository.findOne(approvalId);
		if (approval == null) {
			errMsg = "Approval is not valid for approvalId:  " + approvalId;
			logger.error(errMsg);
			throw new CommonException(CommonException.GENERIC_ERROR_CODE, errMsg);
		}

		// retrieve creator's user name
		String creatorUserName = userRepository.findNameById(approval.getCreatorId());
		if (creatorUserName == null) {
			throw new CommonException(CommonException.GENERIC_ERROR_CODE, "User not valid for id " + creatorUserName);
		}

		// check whether approval is action type = DELETE and still pending
		if (!approval.getActionType().equals(MaintenanceActionType.DELETE.getValue())
				|| !approval.getStatus().equals(MaintenanceActionType.STATUS_PENDING_APPROVAL.getValue())) {
			errMsg = "Approval is not valid for approvalId: " + approvalId
					+ " due to action type is not DELETE or status is not pending";
			logger.error(errMsg);
			throw new CommonException(CommonException.GENERIC_ERROR_CODE, errMsg);
		}

		// retrieve approval details info
		BoSmApprovalDowntime boSmApprovalDowntime = boSmApprovalDowntimeRepository.findOneByApprovalId(approvalId);
		if (boSmApprovalDowntime == null) {
			errMsg = "BoSmApprovalDowntime is not valid for approvalId: " + approvalId;
			logger.error(errMsg);
			throw new CommonException(CommonException.GENERIC_ERROR_CODE, errMsg);
		}

		WorkflowDeleteDowntimeApproval payload = JsonUtil.jsonToObject(boSmApprovalDowntime.getPayload(),
				WorkflowDeleteDowntimeApproval.class);

		logger.debug(String.format("Retrieved payload: %s", payload));

		// retrieve existing record from DB
		SystemDowntimeConfig systemDowntimeConfigExisting = systemDowntimeConfigRepository.findOneById(payload.getId());

		// check if adhoc downtime is activated (starttime <= server time <= endtime) &&
		// is_active = 1)
		if (systemDowntimeConfigExisting == null) {
			throw new CommonException(CommonException.GENERIC_ERROR_CODE,
					"System downtime for adhoc is not valid for id: " + payload.getId());
		} else if ((now.after(systemDowntimeConfigExisting.getStartTime())
				|| now.equals(systemDowntimeConfigExisting.getStartTime()))
				&& (now.before(systemDowntimeConfigExisting.getEndTime())
						|| now.equals(systemDowntimeConfigExisting.getEndTime()))
				&& systemDowntimeConfigExisting.getIsActive().equals(IS_ACTIVE_1)) {
			throw new DeleteAdhocNotAllowedException();
		}

		// update approval status
		approval.setStatus(MaintenanceActionType.STATUS_APPROVED.getValue());
		approval.setReason(deleteDowntimeApprovalRequestVo.getReason());
		approval.setUpdatedTime(now);
		approval.setUpdatedBy(userName);
		approvalRepository.saveAndFlush(approval);

		// update system downtime config
		systemDowntimeConfigExisting.setIsActive(IS_ACTIVE_0);
		systemDowntimeConfigExisting.setUpdatedBy(creatorUserName);
		systemDowntimeConfigExisting.setUpdatedTime(now);
		systemDowntimeConfigRepository.saveAndFlush(systemDowntimeConfigExisting);

		// publish additional data
		HashMap<String, Object> additionalDataMap = new HashMap<String, Object>();
		HashMap<String, Object> additionalSubDataMap = new HashMap<String, Object>();
		additionalSubDataMap.put("id", systemDowntimeConfigExisting.getId());
		additionalSubDataMap.put("name", systemDowntimeConfigExisting.getName());
		additionalSubDataMap.put(START_TIME, systemDowntimeConfigExisting.getStartTimeString());
		additionalSubDataMap.put(END_TIME, systemDowntimeConfigExisting.getEndTimeString());
		additionalSubDataMap.put("isPushNotification", systemDowntimeConfigExisting.isPushNotification());
		additionalSubDataMap.put("pushDate", systemDowntimeConfigExisting.getPushDateString());
		additionalSubDataMap.put("type", ADHOC_TYPE);
		additionalSubDataMap.put(ADHOC_TYP, systemDowntimeConfigExisting.getAdhocType());
		additionalSubDataMap.put(ADHOC_CATEGORY, systemDowntimeConfigExisting.getAdhocTypeCategory());
		additionalSubDataMap.put(BANK_NAME, payload.getBankName());
		additionalSubDataMap.put(BANK_ID, systemDowntimeConfigExisting.getBankId());
		additionalDataMap.put("before", additionalSubDataMap);

		// Publish additional custom data to audit queue
		HashMap<String, Object> additionalSubDataMap2 = new HashMap<String, Object>();
		additionalSubDataMap2.put(REFERENCE_NUMBER, approvalId);
		additionalSubDataMap2.put(ADHOC_TYP, systemDowntimeConfigExisting.getAdhocType());
		additionalSubDataMap2.put(ADHOC_NAME, systemDowntimeConfigExisting.getName());
		additionalSubDataMap2.put("reason", deleteDowntimeApprovalRequestVo.getReason());
		additionalDataMap.put(CUSTOM_FIELD, additionalSubDataMap2);

		additionalDataHolder.setMap(additionalDataMap);

		logger.debug("audit log data : {}", JsonUtil.objectToJson(additionalDataHolder.getMap()));

		// response
		DeleteDowntimeAdhocApprovalResponseVo deleteDowntimeAdhocApprovalResponseVo = new DeleteDowntimeAdhocApprovalResponseVo();
		deleteDowntimeAdhocApprovalResponseVo.setApprovalId(approvalId);

		return deleteDowntimeAdhocApprovalResponseVo;
	}

	@Override
	public BoData updateApproval(Integer approvalId,
			UpdateDowntimeAdhocApprovalRequestVo updateDowntimeAdhocApprovalRequest, String userId) {

		logger.debug(String.format("approval ID: %s", approvalId));
		logger.debug(String.format("reason: %s", updateDowntimeAdhocApprovalRequest.getReason()));
		logger.debug(String.format("user ID: %s", userId));

		String errMsg;

		// Fail fast
		if (userId == null) {
			errMsg = "User not valid for id: " + userId;
			logger.error(errMsg);
			throw new CommonException(CommonException.GENERIC_ERROR_CODE, errMsg);
		}
		// retrieve user's username
		String username = retrieveBOUserUsername(Integer.valueOf(userId));
		// retrieve approval info
		Approval approval = retrieveApprovalValidate(approvalId);
		// retrieve approval creator name
		String creatorName = retrieveCreatorNameValidate(approval);
		// retrieve downtime approval details
		List<BoSmApprovalDowntime> boSmApprovalDowntimes = retrieveApprovalDowntimeDetailsValidate(approval);

		Map<String, Object> before = null;
		Map<String, Object> after = null;
		HashMap<String, Object> additionalSubDataMap2 = null;
		for (BoSmApprovalDowntime downtimeApprovalDetail : boSmApprovalDowntimes) {

			if ("A".equals(downtimeApprovalDetail.getState())) {

				DowntimeApprovalRequest approvalRequestPayload = JsonUtil
						.jsonToObject(downtimeApprovalDetail.getPayload(), DowntimeApprovalRequest.class);
				logger.debug(String.format("Retrieved payload: %s", approvalRequestPayload));

				// validate downtime approval request with DCP system downtime table
				validateDowntimeApprovalRequest(approvalRequestPayload);

				// approve request
				approvalRepository.updateStatusById(ApprovalStatus.APPROVED.getValue(), approval.getId(), ADHOC_TYPE,
						username, new Timestamp(System.currentTimeMillis()));

				// id, name ,type, adhocType, startTime, endTime, isPushnotificaiton, pushDate
				// retrieve existing record from DB
				String adhocType = approvalRequestPayload.getAdhocType();

				if (adhocType == null || adhocType.equalsIgnoreCase("")) {
				
					logger.error("Invalid Payload Adhoc Type " );
					throw new CommonException(CommonException.GENERIC_ERROR_CODE, "Invalid Payload Adhoc Type " );
				}

				SystemDowntimeConfig systemDowntimeConfigExisting = systemDowntimeConfigRepository
						.findOneById(approvalRequestPayload.getId());
				systemDowntimeConfigExisting.setName(approvalRequestPayload.getName());
				systemDowntimeConfigExisting.setType(approvalRequestPayload.getType());
				systemDowntimeConfigExisting.setAdhocType(adhocType);
				systemDowntimeConfigExisting.setAdhocTypeCategory(approvalRequestPayload.getAdhocCategory());
				systemDowntimeConfigExisting.setBankId(approvalRequestPayload.getBankId()!=null?Integer.valueOf(approvalRequestPayload.getBankId()):null);
				Timestamp startTime = toTimestamp(approvalRequestPayload.getStartTime());
				systemDowntimeConfigExisting.setStartTime(startTime);
				Timestamp endTime = toTimestamp(approvalRequestPayload.getEndTime());
				systemDowntimeConfigExisting.setEndTime(endTime);
				systemDowntimeConfigExisting.setPushNotification(approvalRequestPayload.getIsPushNotification());
				Date pushDate = null;
				if (approvalRequestPayload.getPushDate() != null)
					pushDate = toDate(approvalRequestPayload.getPushDate());
				systemDowntimeConfigExisting.setPushDate(pushDate);
				systemDowntimeConfigExisting.setUpdatedBy(creatorName);
				systemDowntimeConfigExisting.setUpdatedTime(new Timestamp(System.currentTimeMillis()));

				// update dcp downtime table
				systemDowntimeConfigExisting = systemDowntimeConfigRepository
						.saveAndFlush(systemDowntimeConfigExisting);

				after = new HashMap<>();
				after.put("id", systemDowntimeConfigExisting.getId());
				after.put("name", systemDowntimeConfigExisting.getName());
				after.put(START_TIME, systemDowntimeConfigExisting.getStartTimeString());
				after.put(END_TIME, systemDowntimeConfigExisting.getEndTimeString());
				after.put("isPushNotification", systemDowntimeConfigExisting.isPushNotification());
				after.put("pushDate", systemDowntimeConfigExisting.getPushDateString());
				after.put("type", ADHOC_TYPE);
				after.put(ADHOC_TYP, systemDowntimeConfigExisting.getAdhocType());
				after.put(ADHOC_CATEGORY, systemDowntimeConfigExisting.getAdhocTypeCategory());
				after.put(BANK_NAME, approvalRequestPayload.getBankName());
				after.put(BANK_ID, systemDowntimeConfigExisting.getBankId());

				// Publish additional custom data to audit queue
				additionalSubDataMap2 = new HashMap<>();
				additionalSubDataMap2.put(REFERENCE_NUMBER, approvalId);
				additionalSubDataMap2.put(ADHOC_TYP, approvalRequestPayload.getAdhocType());
				additionalSubDataMap2.put(ADHOC_NAME, systemDowntimeConfigExisting.getName());

			} else if ("B".equals(downtimeApprovalDetail.getState())) {

				before = new HashMap<>();

				DowntimeApprovalRequest existingRequestPayload = JsonUtil
						.jsonToObject(downtimeApprovalDetail.getPayload(), DowntimeApprovalRequest.class);

				before.put("id", existingRequestPayload.getId());
				before.put("name", existingRequestPayload.getName());
				before.put(START_TIME, existingRequestPayload.getStartTime());
				before.put(END_TIME, existingRequestPayload.getEndTime());
				before.put("isPushNotification", existingRequestPayload.getIsPushNotification());
				before.put("pushDate", existingRequestPayload.getPushDate());
				before.put("type", ADHOC_TYPE);
				before.put(ADHOC_TYP, existingRequestPayload.getAdhocType());
				before.put(ADHOC_CATEGORY, existingRequestPayload.getAdhocCategory());
				before.put(BANK_NAME, existingRequestPayload.getBankName());
				before.put(BANK_ID, existingRequestPayload.getBankId());
			}
		}

		// publish additional data
		Map<String, Object> additionalDataMap = new HashMap<>();
		additionalDataMap.put("before", before);
		additionalDataMap.put("after", after);
		additionalDataMap.put(CUSTOM_FIELD, additionalSubDataMap2);

		additionalDataHolder.setMap(additionalDataMap);

		logger.debug("audit log data : {}", JsonUtil.objectToJson(additionalDataHolder.getMap()));

		// response
		UpdateDowntimeAdhocApprovalResponseVo updateDowntimeAdhocApprovalResponseVo = new UpdateDowntimeAdhocApprovalResponseVo();
		updateDowntimeAdhocApprovalResponseVo.setApprovalId(approvalId);

		return updateDowntimeAdhocApprovalResponseVo;
	}

	final boolean isRequestDowntimeOverlap(SystemDowntimeConfig downtimeConfig,
			DowntimeApprovalRequest approvalRequest) {

		Range<Timestamp> currentDowntime = Ranges
				.<Timestamp>of(downtimeConfig.getStartTime(), downtimeConfig.getEndTime()).closed();

		Timestamp start = toTimestamp(approvalRequest.getStartTime());
		Timestamp endTime = toTimestamp(approvalRequest.getEndTime());

		Range<Timestamp> requestDowntime = Ranges.<Timestamp>of(start, endTime).closed();

		return currentDowntime.confines(requestDowntime) || currentDowntime.confines(start)
				|| currentDowntime.confines(endTime) || requestDowntime.confines(currentDowntime);
	}

	Approval retrieveApprovalValidate(Integer approvalId) {

		Approval approval = approvalRepository.findOne(approvalId);

		if (approval == null) {
			String errMsg = "Approval is not valid for approvalId: " + approvalId;
			logger.error(errMsg);
			throw new CommonException(CommonException.GENERIC_ERROR_CODE, errMsg);
		}

		return approval;
	}

	String retrieveCreatorNameValidate(Approval approval) {

		Integer creatorId = approval.getCreatorId();
		String creatorName = userRepository.findNameById(creatorId);

		if (creatorName == null) {
			String errMsg = "Creator name not found by creatorId: " + creatorId;
			logger.error(errMsg);
			throw new CommonException(CommonException.GENERIC_ERROR_CODE, errMsg);
		}

		return creatorName;
	}

	List<BoSmApprovalDowntime> retrieveApprovalDowntimeDetailsValidate(Approval approval) {

		List<BoSmApprovalDowntime> boSmApprovalDowntimes = boSmApprovalDowntimeRepository
				.findByApprovalId(approval.getId());

		if (boSmApprovalDowntimes == null || boSmApprovalDowntimes.isEmpty()) {
			String errMsg = "BoSmApprovalDowntime is not valid for approvalId: " + approval.getId();
			logger.error(errMsg);
			throw new CommonException(CommonException.GENERIC_ERROR_CODE, errMsg);
		}

		return boSmApprovalDowntimes;
	}

	void validateDowntimeApprovalRequest(DowntimeApprovalRequest downtimeRequest) {
		// adhocType, startTime, endTime, type
		// SystemDowntimeConfig systemDowntimeConfigExisting =
		// systemDowntimeConfigRepository.findOneById(downtimeRequest.getId());

		List<SystemDowntimeConfig> systemDowntimeConfigExisting = systemDowntimeConfigRepository
				.findByStartTimeAndEndTimeForUpdate(toTimestamp(downtimeRequest.getStartTime()),
						toTimestamp(downtimeRequest.getEndTime()), downtimeRequest.getId(),
						downtimeRequest.getAdhocType(), downtimeRequest.getAdhocCategory(),Integer.valueOf(downtimeRequest.getBankId()));

		// If overlapped
		if (!systemDowntimeConfigExisting.isEmpty()) {
			throw new UpdateAdhocNotAllowedException();
		}
	}

	private String retrieveBOUserUsername(Integer userId) {

		String username = userRepository.findNameById(userId);
		if (username == null) {
			String errMsg = "User not valid for id: " + userId;
			logger.error(errMsg);
			throw new CommonException(CommonException.GENERIC_ERROR_CODE, errMsg);
		}

		return username;
	}
}
