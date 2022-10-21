package com.rhbgroup.dcpbo.system.downtime.whitelist.service.impl;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rhbgroup.dcp.data.entity.profiles.UserProfile;
import com.rhbgroup.dcp.data.repository.ProfileRepository;
import com.rhbgroup.dcp.util.JsonUtil;
import com.rhbgroup.dcpbo.common.audit.AdditionalDataHolder;
import com.rhbgroup.dcpbo.common.exception.CommonException;
import com.rhbgroup.dcpbo.system.common.BoData;
import com.rhbgroup.dcpbo.system.downtime.dto.Pagination;
import com.rhbgroup.dcpbo.system.downtime.dto.Whitelist;
import com.rhbgroup.dcpbo.system.downtime.repository.ApprovalRepository;
import com.rhbgroup.dcpbo.system.downtime.repository.ConfigFunctionRepository;
import com.rhbgroup.dcpbo.system.downtime.repository.UserRepository;
import com.rhbgroup.dcpbo.system.downtime.whitelist.dto.ApprovalDowntimeAdhocWhitelistPayload;
import com.rhbgroup.dcpbo.system.downtime.whitelist.repository.BoSmApprovalDowntimeWhitelistRepository;
import com.rhbgroup.dcpbo.system.downtime.whitelist.repository.DowntimeWhitelistRepository;
import com.rhbgroup.dcpbo.system.downtime.whitelist.repository.SystemDowntimeWhitelistConfigRepository;
import com.rhbgroup.dcpbo.system.downtime.whitelist.service.DowntimeAdhocWhitelistService;
import com.rhbgroup.dcpbo.system.downtime.whitelist.vo.AddDowntimeAdhocWhitelistRequest;
import com.rhbgroup.dcpbo.system.downtime.whitelist.vo.DeleteDowntimeAdhocWhitelistRequest;
import com.rhbgroup.dcpbo.system.downtime.whitelist.vo.DowntimeAdhocWhitelistResponse;
import com.rhbgroup.dcpbo.system.downtime.whitelist.vo.GetDowntimeAdhocWhitelistResponse;
import com.rhbgroup.dcpbo.system.enums.ApprovalStatus;
import com.rhbgroup.dcpbo.system.enums.MaintenanceActionType;
import com.rhbgroup.dcpbo.system.exception.PendingApprovalException;
import com.rhbgroup.dcpbo.system.model.BoSmApprovalDowntimeWhitelist;
import com.rhbgroup.dcpbo.system.model.ConfigFunction;
import com.rhbgroup.dcpbo.system.model.DowntimeWhitelist;
import com.rhbgroup.dcpbo.system.model.SystemDowntimeWhitelistConfig;
import com.rhbgroup.dcpbo.system.model.User;
import java.util.ArrayList;
import java.util.Collections;

import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

@Log4j2
@Service
public class DowntimeAdhocWhitelistServiceImpl implements DowntimeAdhocWhitelistService {
	
        private static Logger logger = LogManager.getLogger(DowntimeAdhocWhitelistServiceImpl.class);
	
	private static final String ADHOC_TYPE = "ADHOC";
        private static final int GET_WHITE_LIST_PAGE_SIZE = 15;
	
	@Autowired
	private ConfigFunctionRepository configFunctionRepository;
	
	@Autowired
	private SystemDowntimeWhitelistConfigRepository systemDowntimeWhitelistConfigRepository;
	
	@Autowired
	private ApprovalRepository approvalRepository;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private BoSmApprovalDowntimeWhitelistRepository boSmApprovalDowntimeWhitelistRepository;
	
        @Autowired
        AdditionalDataHolder additionalDataHolder;

        @Autowired
        ProfileRepository profileRepository;
        
        @Autowired
        DowntimeWhitelistRepository downtimeWhitelistRepository;
	
	public DowntimeAdhocWhitelistServiceImpl() {
		
	}
	
	@Override
	public ResponseEntity<BoData> addDowntimeAdhocWhitelist(AddDowntimeAdhocWhitelistRequest request, Integer userId) {
		List<Integer> approvalIdLs;
		Integer approvalId = 0;
		
		Timestamp now = new Timestamp(System.currentTimeMillis());
		ObjectMapper objectMapper = new ObjectMapper();
		
		log.debug("request body userId : {}", request.getUserId());
		log.debug("maker id : {}", userId);
		
		//retrieve user name
		String userName = userRepository.findNameById(userId);
		if (userName == null){
			throw new CommonException(CommonException.GENERIC_ERROR_CODE,"User not valid for id " + userId);
		}
		
		List<SystemDowntimeWhitelistConfig> systemDowntimeWhitelistConfigList = systemDowntimeWhitelistConfigRepository.findByUserIdAndType(request.getUserId(), ADHOC_TYPE);
		if(!systemDowntimeWhitelistConfigList.isEmpty())
		{
			throw new CommonException("40003","whitelist user exists! Error:403, Code: 40003", HttpStatus.FORBIDDEN);
		}
		UserProfile userProfile = profileRepository.getUserProfile(request.getUserId());
		
		if (userProfile == null){
			throw new CommonException(CommonException.GENERIC_ERROR_CODE, "Whitelist user not valid for id " + request.getUserId());
		}
		
		ResponseEntity<BoData> responseEntity;
		DowntimeAdhocWhitelistResponse response  = new DowntimeAdhocWhitelistResponse();
		
		//check whether approval is required in db
		ConfigFunction configFunction = configFunctionRepository.findOne(request.getFunctionId());
		Boolean requireApproval = configFunction.isApprovalRequired();
		
		if (requireApproval) { // approval is required
			
			List<Integer> approvalList = approvalRepository.findIdByFunctionIdAndStatus(request.getFunctionId(),
					MaintenanceActionType.STATUS_PENDING_APPROVAL.getValue());
			if(!approvalList.isEmpty())
			{
				List<BoSmApprovalDowntimeWhitelist> duplicatePendingApprovalList = boSmApprovalDowntimeWhitelistRepository.findByApprovalIdAndLockingId(approvalList, String.valueOf(request.getUserId()));
				if (!duplicatePendingApprovalList.isEmpty()) {
					List<Integer> duplicateApprovalId = duplicatePendingApprovalList.stream().map(BoSmApprovalDowntimeWhitelist::getApprovalId).collect(Collectors.toList());
					log.error("Found duplicate downtime whitelist on approval ids : {}", duplicateApprovalId);
					throw new PendingApprovalException();
				}
			}

			String payloadString = "";
			
	
			ApprovalDowntimeAdhocWhitelistPayload payload = new ApprovalDowntimeAdhocWhitelistPayload();
			payload.setType(ADHOC_TYPE);
			payload.setUserId(request.getUserId());
			payload.setName(userProfile.getName());
			payload.setMobileNo(userProfile.getMobileNo());
			payload.setUsername(userProfile.getUsername());
			payload.setIdNo(userProfile.getIdNo());
			payload.setIdType(userProfile.getIdType());;
			payload.setCisNo(userProfile.getCisNo());
			try {
				payloadString = objectMapper.writeValueAsString(payload);
			} catch (JsonProcessingException e) {
				log.error("payload error!",e);
			}
			log.debug("Payload: {}", payloadString);
			
			//Write to approval table
			String description = String.valueOf(request.getUserId());

			approvalIdLs =  approvalRepository.insert(request.getFunctionId(), userId, description, MaintenanceActionType.ADD.getValue(),ApprovalStatus.PENDING_APPROVAL.getValue(), now, userName, now, userName);
			if (approvalIdLs.isEmpty()){
				 throw new CommonException(CommonException.GENERIC_ERROR_CODE, "Failed to write to approval repository.");
			}
			approvalId = approvalIdLs.get(0);
			boSmApprovalDowntimeWhitelistRepository.insert(approvalId,MaintenanceActionType.DOWNTIME_APPROVAL_STATE_A .getValue(),String.valueOf(request.getUserId()),payloadString,now,userName,now,userName);
			
			response.setApprovalId(approvalId);
			responseEntity = new ResponseEntity<BoData>(response, HttpStatus.OK);
			
		} else { //approval not required. insert into table immediately
			SystemDowntimeWhitelistConfig systemDowntimeWhitelistConfig = new SystemDowntimeWhitelistConfig();
			systemDowntimeWhitelistConfig.setUserId(request.getUserId());
			systemDowntimeWhitelistConfig.setType(ADHOC_TYPE);
			systemDowntimeWhitelistConfig.setCreatedBy(userName);
			systemDowntimeWhitelistConfig.setCreatedTime(now);
			systemDowntimeWhitelistConfig.setUpdatedBy(userName);
			systemDowntimeWhitelistConfig.setUpdatedTime(now);
			systemDowntimeWhitelistConfigRepository.saveAndFlush(systemDowntimeWhitelistConfig);
			responseEntity = new ResponseEntity<BoData>(response, HttpStatus.CREATED);
		}
		//publish additional data
		HashMap<String, Object> additionalDataMap = new HashMap<String, Object>();
		HashMap<String, Object> additionalSubDataMap = new HashMap<String, Object>();
		additionalSubDataMap.put("type", ADHOC_TYPE);
		additionalSubDataMap.put("userId", userProfile.getId());
		additionalSubDataMap.put("name", userProfile.getName());
		additionalSubDataMap.put("mobileNo", userProfile.getMobileNo());
		additionalSubDataMap.put("username", userProfile.getUsername());
		additionalSubDataMap.put("idNo", userProfile.getIdNo());
		additionalSubDataMap.put("idType", userProfile.getIdType());
		additionalSubDataMap.put("cisNo", userProfile.getCisNo());
		additionalDataMap.put("after", additionalSubDataMap);
		additionalDataHolder.setMap(additionalDataMap);
		log.debug("audit log data : {}", JsonUtil.objectToJson(additionalDataHolder.getMap()));
		        
		return responseEntity;
	}
        
	@Override
	public ResponseEntity<BoData> deleteDowntimeAdhocWhitelist(DeleteDowntimeAdhocWhitelistRequest request, Integer userId, Integer id) {
		List<Integer> approvalIdLs;
		Integer approvalId = 0;
		
		Timestamp now = new Timestamp(System.currentTimeMillis());
		ObjectMapper objectMapper = new ObjectMapper();
		
		//retrieve user name
		String userName = userRepository.findNameById(userId);
		if (userName == null){
			throw new CommonException(CommonException.GENERIC_ERROR_CODE,"User not valid for id " + userId);
		}
		
		ResponseEntity<BoData> responseEntity;
		DowntimeAdhocWhitelistResponse response  = new DowntimeAdhocWhitelistResponse();
		
		//check whether approval is required in db
		ConfigFunction configFunction = configFunctionRepository.findOne(request.getFunctionId());
		Boolean requireApproval = configFunction.isApprovalRequired();
		
		SystemDowntimeWhitelistConfig tobeDeleted = systemDowntimeWhitelistConfigRepository.findOne(id);
		if(tobeDeleted == null)
		{
			log.error("User :[{}] currently not whitelisted.", id);
			throw new CommonException(CommonException.GENERIC_ERROR_CODE,"User currently not whitelisted. " + id);
		}
		
		UserProfile userProfile = profileRepository.getUserProfile(tobeDeleted.getUserId());
		
		if (userProfile == null){
			throw new CommonException(CommonException.GENERIC_ERROR_CODE, "Whitelist user not valid for id " + tobeDeleted.getUserId());
		}
		
		if (requireApproval) { // approval is required
			
			List<Integer> approvalList = approvalRepository.findIdByFunctionIdAndStatus(request.getFunctionId(),
					MaintenanceActionType.STATUS_PENDING_APPROVAL.getValue());
			
			if(!approvalList.isEmpty())
			{
				List<BoSmApprovalDowntimeWhitelist> duplicatePendingApprovalList = boSmApprovalDowntimeWhitelistRepository.findByApprovalIdAndLockingId(approvalList, String.valueOf(tobeDeleted.getUserId()));
				if (!duplicatePendingApprovalList.isEmpty()) {
					List<Integer> duplicateApprovalId = duplicatePendingApprovalList.stream().map(BoSmApprovalDowntimeWhitelist::getApprovalId).collect(Collectors.toList());
					log.error("Found duplicate downtime whitelist on approval ids : [{}]", duplicateApprovalId);
					throw new PendingApprovalException();
				}
			}
			
			
			String payloadString = "";
			ApprovalDowntimeAdhocWhitelistPayload payload = new ApprovalDowntimeAdhocWhitelistPayload();
			payload.setId(tobeDeleted.getId());
			payload.setType(ADHOC_TYPE);
			payload.setUserId(tobeDeleted.getUserId());
			payload.setName(userProfile.getName());
			payload.setMobileNo(userProfile.getMobileNo());
			payload.setUsername(userProfile.getUsername());
			payload.setIdNo(userProfile.getIdNo());
			payload.setIdType(userProfile.getIdType());;
			payload.setCisNo(userProfile.getCisNo());
			try {
				payloadString = objectMapper.writeValueAsString(payload);
			} catch (JsonProcessingException e) {
				log.error("payload error!",e);
			}
			log.debug("Payload: {}", payloadString);
			
			//Write to approval table
			String description = String.valueOf(tobeDeleted.getUserId());

			approvalIdLs =  approvalRepository.insert(request.getFunctionId(), userId, description, MaintenanceActionType.DELETE.getValue(),ApprovalStatus.PENDING_APPROVAL.getValue(), now, userName, now, userName);
			if (approvalIdLs.isEmpty()){
				 throw new CommonException(CommonException.GENERIC_ERROR_CODE, "Failed to write to approval repository.");
			}
			approvalId = approvalIdLs.get(0);
			
			//insert into approval downtime table
			boSmApprovalDowntimeWhitelistRepository.insert(approvalId,MaintenanceActionType.DOWNTIME_APPROVAL_STATE_B .getValue(),String.valueOf(tobeDeleted.getUserId()),payloadString,now,userName,now,userName);
			
			response.setApprovalId(approvalId);
			responseEntity = new ResponseEntity<BoData>(response, HttpStatus.OK);
			
		} else { //approval not required. update into table immediately
			
			systemDowntimeWhitelistConfigRepository.delete(tobeDeleted);
			responseEntity = new ResponseEntity<BoData>(response, HttpStatus.CREATED);
		}
		
		//publish additional data
		HashMap<String, Object> additionalDataMap = new HashMap<String, Object>();
		HashMap<String, Object> additionalSubDataMap = new HashMap<String, Object>();
		additionalSubDataMap.put("id", tobeDeleted.getId());
		additionalSubDataMap.put("type", ADHOC_TYPE);
		additionalSubDataMap.put("userId", tobeDeleted.getUserId());
		additionalSubDataMap.put("name", userProfile.getName());
		additionalSubDataMap.put("mobileNo", userProfile.getMobileNo());
		additionalSubDataMap.put("username", userProfile.getUsername());
		additionalSubDataMap.put("idNo", userProfile.getIdNo());
		additionalSubDataMap.put("idType", userProfile.getIdType());
		additionalSubDataMap.put("cisNo", userProfile.getCisNo());
		additionalDataMap.put("before", additionalSubDataMap);
        additionalDataHolder.setMap(additionalDataMap);
        log.debug("audit log data : {}", JsonUtil.objectToJson(additionalDataHolder.getMap()));
        
		return responseEntity;
	}

        @Override
        public ResponseEntity<BoData> getDowntimeAdhocWhitelist(Integer pageNo, Integer userId) {
            
            logger.debug("Page No: " + pageNo);
            logger.debug("User Id: " + userId);

            //validate and retrieve BO's userId and return name
            String username = retrieveValidateUsername(userId);
            logger.debug("Valid user found: " + username);
            
            //retrieve all whitelist UserProfile by page and sort by
            int pageNumber = validateConvert(pageNo);
            Page<DowntimeWhitelist> whitelistPage = retrieveSortWhitelist(pageNumber, GET_WHITE_LIST_PAGE_SIZE, ADHOC_TYPE);
            
            GetDowntimeAdhocWhitelistResponse getResponse = prepareResponse(whitelistPage);
            
            return new ResponseEntity<>(getResponse, HttpStatus.OK);
        }
        
        int validateConvert(int pageNo){
        
            if(pageNo<=0){
                log.debug("Page number not valid " +pageNo);
                throw new CommonException(CommonException.GENERIC_ERROR_CODE, "Page number is not valid " + pageNo);
            }
            
            return pageNo - 1;
        }
        
        /**
         * File BO's user by boUserId and retrieve its username. If user not exist or userStatusId
         * not active.
         * @param boUserId BO user's id;
         * @return Name of BO user.
         */
        String retrieveValidateUsername(final Integer boUserId){
            
            //retrieve user name
            User user = userRepository.findById(boUserId);
            
            if(user == null || !"A".equals(user.getUserStatusId())){
                
                log.debug("User not valid for id " + boUserId);
                throw new CommonException(CommonException.GENERIC_ERROR_CODE,"User not valid for id " + boUserId);
            }

            return user.getName();
        }
        
        Page<DowntimeWhitelist> retrieveSortWhitelist(int pageNo, int pageSize, String adhocType){
        
            Sort sortByName =  new Sort(Sort.Direction.ASC, "userId.name");
            PageRequest page = new PageRequest(pageNo, pageSize, sortByName);
            
            return downtimeWhitelistRepository.findByType(ADHOC_TYPE, page);
        }

        GetDowntimeAdhocWhitelistResponse prepareResponse(Page<DowntimeWhitelist> whitelistPages){
        
            //fail fast
            if(whitelistPages == null || !whitelistPages.hasContent()){
                
                GetDowntimeAdhocWhitelistResponse emptyResponse = new GetDowntimeAdhocWhitelistResponse();
                emptyResponse.setWhitelist(Collections.emptyList());
                emptyResponse.setPagination(Pagination.empty().pageNo(whitelistPages.getNumber()+1).build());
                
                return emptyResponse;
            }
        
            List<DowntimeWhitelist> whitelists = whitelistPages.getContent();
            List<Whitelist> lists = new ArrayList<>();
            
            for(DowntimeWhitelist downtimeWhitelist : whitelists){
            
                Whitelist whitelist = new Whitelist.Builder(downtimeWhitelist.getId())
                        .userId(downtimeWhitelist.getUserId().getId())
                        .name(downtimeWhitelist.getUserId().getName())
                        .mobileNo(downtimeWhitelist.getUserId().getMobileNo())
                        .username(downtimeWhitelist.getUserId().getUsername())
                        .idNo(downtimeWhitelist.getUserId().getIdNo())
                        .idType(downtimeWhitelist.getUserId().getIdType())
                        .cisNo(downtimeWhitelist.getUserId().getCisNo())
                        .build();
            
                lists.add(whitelist);
            }
            
            GetDowntimeAdhocWhitelistResponse response = new GetDowntimeAdhocWhitelistResponse();
            response.setWhitelist(lists);
            
            String pageIndicator = whitelistPages.hasNext() ? "N" : "L";
            int pageNumber = whitelistPages.getNumber() + 1;
            int rowCount = downtimeWhitelistRepository.getTotalByType(ADHOC_TYPE);
            
            Pagination page = new Pagination.Builder(whitelistPages.getTotalPages())
                    .pageIndicator(pageIndicator)
                    .pageNo(pageNumber)
                    .recordCount(rowCount)
                    .build();
            
            response.setPagination(page);
            
            return response;
        }
}
