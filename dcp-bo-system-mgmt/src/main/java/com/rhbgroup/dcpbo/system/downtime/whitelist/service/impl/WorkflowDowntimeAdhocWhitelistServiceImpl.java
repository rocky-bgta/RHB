package com.rhbgroup.dcpbo.system.downtime.whitelist.service.impl;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.rhbgroup.dcp.util.JsonUtil;
import com.rhbgroup.dcpbo.common.audit.AdditionalDataHolder;
import com.rhbgroup.dcpbo.common.exception.CommonException;
import com.rhbgroup.dcpbo.system.common.BoData;
import com.rhbgroup.dcpbo.system.downtime.dto.AdhocApproval;
import com.rhbgroup.dcpbo.system.downtime.repository.ApprovalRepository;
import com.rhbgroup.dcpbo.system.downtime.repository.UserRepository;
import com.rhbgroup.dcpbo.system.downtime.whitelist.dto.ApprovalDowntimeAdhocWhitelistPayload;
import com.rhbgroup.dcpbo.system.downtime.whitelist.repository.BoSmApprovalDowntimeWhitelistRepository;
import com.rhbgroup.dcpbo.system.downtime.whitelist.repository.SystemDowntimeWhitelistConfigRepository;
import com.rhbgroup.dcpbo.system.downtime.whitelist.service.WorkflowDowntimeAdhocWhitelistService;
import com.rhbgroup.dcpbo.system.downtime.whitelist.vo.ApprovalWhitelistResponse;
import com.rhbgroup.dcpbo.system.downtime.whitelist.vo.ApproveAddDowntimeAdhocWhitelistRequest;
import com.rhbgroup.dcpbo.system.downtime.whitelist.vo.ApproveDeleteDowntimeAdhocWhitelistRequest;
import com.rhbgroup.dcpbo.system.enums.ApprovalStatus;
import com.rhbgroup.dcpbo.system.enums.MaintenanceActionType;
import com.rhbgroup.dcpbo.system.model.Approval;
import com.rhbgroup.dcpbo.system.model.BoSmApprovalDowntimeWhitelist;
import com.rhbgroup.dcpbo.system.model.SystemDowntimeWhitelistConfig;
import com.rhbgroup.dcpbo.system.model.User;

@Service
public class WorkflowDowntimeAdhocWhitelistServiceImpl implements WorkflowDowntimeAdhocWhitelistService {
	
	private static final String ADHOC_TYPE = "ADHOC";
    private static final String REQUEST_APPROVED = "APPROVED";
    private static final String REQUEST_REJECTED = "REJECTED";
    private static final String REQUEST_PENDING = "PENDING";
    
    
	@Autowired
	ApprovalRepository approvalRepository;

	@Autowired
	private BoSmApprovalDowntimeWhitelistRepository boSmApprovalDowntimeWhitelistRepository;

	@Autowired
	private SystemDowntimeWhitelistConfigRepository systemDowntimeWhitelistConfigRepository;

	@Autowired
	private UserRepository userRepository;

        @Autowired
        AdditionalDataHolder additionalDataHolder;

	private static Logger logger = LogManager.getLogger(WorkflowDowntimeAdhocWhitelistServiceImpl.class);

	@Override
	public BoData approveAddDowntimeWhitelist(Integer approvalId, ApproveAddDowntimeAdhocWhitelistRequest approveAddDowntimeAdhocWhitelistRequest, String userId) {
		String errMsg;
		Timestamp now = new Timestamp(System.currentTimeMillis());
		
		//retrieve user name of this request
		Integer userIdInt = Integer.valueOf(userId);
		String userName = findUser(userIdInt);
		
		Approval approval = findApprovalInfo(approvalId);
		
		//retrieve creator's user name
		String creatorUserName = userRepository.findNameById(approval.getCreatorId());
		
		//retrieve approval details info
		BoSmApprovalDowntimeWhitelist boSmApprovalDowntimeWhitelist = findApprovalDowntimeWhitelist(approvalId);
		
		ApprovalDowntimeAdhocWhitelistPayload payload = JsonUtil.jsonToObject(boSmApprovalDowntimeWhitelist.getPayload(), ApprovalDowntimeAdhocWhitelistPayload.class);

		logger.debug(String.format("Retrieved payload: %s", payload));
		
		//check whether the user already exists in the whitelist config table
		List<SystemDowntimeWhitelistConfig> systemDowntimeWhitelistConfigList = systemDowntimeWhitelistConfigRepository.findByUserIdAndType(payload.getUserId(), ADHOC_TYPE);
		if(!systemDowntimeWhitelistConfigList.isEmpty())
		{
			throw new CommonException("40003","whitelist user exists! Error:403, Code: 40003", HttpStatus.FORBIDDEN);
		}
		
		// Approve downtime whitelist creation request
		String reason = approveAddDowntimeAdhocWhitelistRequest.getReason();
		String updatedBy = userName;
		Timestamp updatedTime = now;
		approvalRepository.updateStatusById(MaintenanceActionType.STATUS_APPROVED.getValue(), approvalId, reason, updatedBy, updatedTime);

		// Write to downtime whitelist table
		SystemDowntimeWhitelistConfig systemDowntimeWhitelistConfig = new SystemDowntimeWhitelistConfig();
		systemDowntimeWhitelistConfig.setUserId(payload.getUserId());
		systemDowntimeWhitelistConfig.setType(payload.getType());
		systemDowntimeWhitelistConfig.setCreatedBy(creatorUserName);
		systemDowntimeWhitelistConfig.setCreatedTime(now);
		systemDowntimeWhitelistConfig.setUpdatedBy(creatorUserName);
		systemDowntimeWhitelistConfig.setUpdatedTime(now);
		systemDowntimeWhitelistConfigRepository.saveAndFlush(systemDowntimeWhitelistConfig);
		
		// Publish additional data to audit queue
		String state = "after";
		publishAdditionalDate(payload, state);
		
		//construct response
		AdhocApproval adhocApproval = new AdhocApproval();
		adhocApproval.setApprovalId(approvalId);
		logger.debug("    adhocApproval: " + adhocApproval);
		
		return adhocApproval;
	}

	@Override
	public BoData approveDeleteDowntimeWhitelist(Integer approvalId, ApproveDeleteDowntimeAdhocWhitelistRequest approveAddDowntimeAdhocWhitelistRequest, String userId) {
		String errMsg;
		Timestamp now = new Timestamp(System.currentTimeMillis());
		
		//retrieve user name of this request
		Integer userIdInt = Integer.valueOf(userId);
		String userName = findUser(userIdInt);
		
		// retrieve approval info
		Approval approval = findApprovalInfo(approvalId);
		
		//retrieve approval details info
		BoSmApprovalDowntimeWhitelist boSmApprovalDowntimeWhitelist = findApprovalDowntimeWhitelist(approvalId);
		
		ApprovalDowntimeAdhocWhitelistPayload payload = JsonUtil.jsonToObject(boSmApprovalDowntimeWhitelist.getPayload(), ApprovalDowntimeAdhocWhitelistPayload.class);

		logger.debug(String.format("Retrieved payload: %s", payload));
		
		//check whether the user is exists in the whitelist config table
		List<SystemDowntimeWhitelistConfig> systemDowntimeWhitelistConfigList = systemDowntimeWhitelistConfigRepository.findByUserIdAndType(payload.getUserId(), ADHOC_TYPE);
		if(systemDowntimeWhitelistConfigList.isEmpty())
		{
			throw new CommonException(CommonException.GENERIC_ERROR_CODE,"whitelist user not exists! Error:403, Code: 40003", HttpStatus.FORBIDDEN);
		}
		
		// Approve downtime whitelist deletion request
		String reason = approveAddDowntimeAdhocWhitelistRequest.getReason();
		String updatedBy = userName;
		Timestamp updatedTime = now;
		approvalRepository.updateStatusById(MaintenanceActionType.STATUS_APPROVED.getValue(), approvalId, reason, updatedBy, updatedTime);

		// Delete user from downtime whitelist table
		systemDowntimeWhitelistConfigRepository.delete(payload.getId());
		
		// Publish additional data to audit queue
		String state = "before";
		publishAdditionalDate(payload, state);
		
		//construct response
		AdhocApproval adhocApproval = new AdhocApproval();
		adhocApproval.setApprovalId(approvalId);
		logger.debug("    adhocApproval: " + adhocApproval);
		
		return adhocApproval;
	}

	private BoSmApprovalDowntimeWhitelist findApprovalDowntimeWhitelist(Integer approvalId) {
		String errMsg;
		BoSmApprovalDowntimeWhitelist boSmApprovalDowntimeWhitelist = boSmApprovalDowntimeWhitelistRepository.findOneByApprovalId(approvalId);
		if (boSmApprovalDowntimeWhitelist == null) {
			errMsg = "BoSmApprovalDowntimeWhitelist is not valid for approvalId: " + approvalId;
			logger.error(errMsg);
            throw new CommonException(CommonException.GENERIC_ERROR_CODE, errMsg, HttpStatus.BAD_REQUEST);
		}
		return boSmApprovalDowntimeWhitelist;
	}

	private Approval findApprovalInfo(Integer approvalId) {
		String errMsg;
		Approval approval = approvalRepository.findOne(approvalId);
		if (approval == null) {
			errMsg = "Approval is not valid for approvalId: " + approvalId;
			logger.error(errMsg);
			throw new CommonException(CommonException.GENERIC_ERROR_CODE, errMsg, HttpStatus.BAD_REQUEST);
		}
		return approval;
	}

	private String findUser(Integer userIdInt) {
		String errMsg;
		String userName = userRepository.findNameById(userIdInt);
		if (userName == null){
			errMsg = "User not valid for id: " + userIdInt;
			logger.error(errMsg);
			throw new CommonException(CommonException.GENERIC_ERROR_CODE, errMsg, HttpStatus.BAD_REQUEST);
		}
		return userName;
	}
	
	private void publishAdditionalDate(ApprovalDowntimeAdhocWhitelistPayload payload, String state) {
		HashMap<String, Object> additionalDataMap = new HashMap<String, Object>();
		HashMap<String, Object> additionalSubDataMap = new HashMap<String, Object>();
		additionalSubDataMap.put("userId", payload.getUserId());
		additionalSubDataMap.put("name", payload.getName());
		additionalSubDataMap.put("mobileNo", payload.getMobileNo());
		additionalSubDataMap.put("username", payload.getUsername());
		additionalSubDataMap.put("idNo", payload.getIdNo());
		additionalSubDataMap.put("idType", payload.getIdType());
		additionalSubDataMap.put("cisNo", payload.getCisNo());
		additionalSubDataMap.put("type", payload.getType());
		additionalDataMap.put(state, additionalSubDataMap);
        additionalDataHolder.setMap(additionalDataMap);
        logger.debug("audit log data: " + JsonUtil.objectToJson(additionalDataHolder.getMap()));
	}

        @Override
        public ResponseEntity<BoData> getWhitelistApproval(Integer approvalId, Integer boUserId, String actionType) {
            
            String boUsername = findUser(boUserId);
            String action = trimToUppercase(actionType);
            
            logger.debug("Find valid BO's username: "+ boUsername);
            
            //actionType, createdBy, createdTime, reason
            Approval whitelistApproval = retrieveApprovalValidate(approvalId, action);
            Integer creatorId = whitelistApproval.getCreatorId();
            String creatorName = findUser(creatorId);
            
            logger.debug("Find downtime whitelist approval creatorName :"+ creatorName);
            
            //state payload
            BoSmApprovalDowntimeWhitelist smApprovalWhitelist = retrieveSmApprovalWhitelist(whitelistApproval.getId());

            ApprovalDowntimeAdhocWhitelistPayload payload = JsonUtil.jsonToObject(smApprovalWhitelist.getPayload(), ApprovalDowntimeAdhocWhitelistPayload.class);

            String approvalStatus = null;
            if(whitelistApproval.getStatus().equals(ApprovalStatus.APPROVED.getValue())) {
            	approvalStatus = REQUEST_APPROVED;
            } else if(whitelistApproval.getStatus().equals(ApprovalStatus.PENDING_APPROVAL.getValue())) {
            	approvalStatus = REQUEST_PENDING;
            } else if(whitelistApproval.getStatus().equals(ApprovalStatus.REJECTED.getValue())) {
            	approvalStatus = REQUEST_REJECTED;
            }
            ApprovalWhitelistResponse awr = ApprovalWhitelistResponse.of(whitelistApproval.getId())
                    .creatorName(creatorName)
                    .isCreator(creatorId.equals(boUserId)? "Y" : "N")
                    .actionType(whitelistApproval.getActionType())
                    .reason(whitelistApproval.getReason())
                    .createdTime(formatTimestamp(whitelistApproval.getCreatedTime()))
                    .createdBy(whitelistApproval.getCreatedBy())
                    .updatedTime(formatTimestamp(whitelistApproval.getUpdatedTime()))
                    .updatedBy(whitelistApproval.getUpdatedBy())
                    .userId(payload.getUserId())
                    .name(payload.getName())
                    .mobileNo(payload.getMobileNo())
                    .username(payload.getUsername())
                    .idNo(payload.getIdNo())
                    .idType(payload.getIdType())
                    .type(payload.getType())
                    .cisNo(payload.getCisNo())
                    .approvalStatus(approvalStatus)
                    .build();

            return ResponseEntity.ok(awr);
        }
        
        String formatTimestamp(Timestamp timestamp) {
		if (timestamp == null)
			return "";
		
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(User.TIMESTAMP_FORMAT);
		return simpleDateFormat.format(timestamp);
	}
    
        String trimToUppercase(String actionType){
            
            if(StringUtils.isEmpty(actionType)){
                String errMsg = "Action type is empty or null: " + actionType;
                logger.error(errMsg);
                throw new CommonException(CommonException.GENERIC_ERROR_CODE, errMsg);
            }
            
            return actionType.trim().toUpperCase();
        }
        
        BoSmApprovalDowntimeWhitelist retrieveSmApprovalWhitelist(Integer approvalId){
        
            BoSmApprovalDowntimeWhitelist smApprovalWhitelist = boSmApprovalDowntimeWhitelistRepository.findOneByApprovalId(approvalId);
        
            if(smApprovalWhitelist == null){
                String errMsg = "System management approval is not valid for approvalId: " + approvalId;
                logger.error(errMsg);
                throw new CommonException(CommonException.GENERIC_ERROR_CODE, errMsg);
            }

            return smApprovalWhitelist;
        }
    
        Approval retrieveApprovalValidate(Integer approvalId, String actionType){
            
            logger.debug("approvalId: "+ approvalId + " actionType: "+ actionType);
            
            Approval approval = approvalRepository.findByIdAndActionType(approvalId, actionType);
            
            if (approval == null) {
                String errMsg = "Approval is not valid for approvalId: " + approvalId;
                logger.error(errMsg);
                throw new CommonException(CommonException.GENERIC_ERROR_CODE, errMsg);
            }
            
            return approval;
        }
        
        boolean isAddDeleteActionType(String actionType){
            
            return MaintenanceActionType.ADD.getValue().equals(actionType) || 
                    MaintenanceActionType.DELETE.getValue().equals(actionType);
        }
}
