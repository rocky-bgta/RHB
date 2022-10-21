package com.rhbgroup.dcpbo.user.usergroup;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rhbgroup.dcp.util.JsonUtil;
import com.rhbgroup.dcpbo.common.audit.AdditionalDataHolder;
import com.rhbgroup.dcpbo.common.exception.CommonException;
import com.rhbgroup.dcpbo.user.common.*;
import com.rhbgroup.dcpbo.user.common.model.bo.BoUmApprovalUserGroup;
import com.rhbgroup.dcpbo.user.enums.ApprovalStatus;
import com.rhbgroup.dcpbo.user.enums.MaintenanceActionType;
import com.rhbgroup.dcpbo.user.info.model.bo.ConfigFunction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class UsergroupAddService {

	@Autowired
	UserRepository userRepository;
	@Autowired
	UserGroupRepository userGroupRepository;
	@Autowired
	ConfigFunctionRepository configFunctionRepository;
	@Autowired
	UsergroupAccessRepository userGroupAccessRepository;
	@Autowired
	ApprovalRepository approvalRepository;
	@Autowired
	BoUmApprovalUsergroupRepository boUmApprovalUsergroupRepository;
	@Autowired
	AdditionalDataHolder additionalDataHolder;
	@Autowired
	BoAuditEventConfigRepository boAuditEventConfigRepository;


	private static Logger logger = LogManager.getLogger(UsergroupAddService.class);

	public Usergroup postUsergroupService(UsergroupRequestBody usergroupRequestBody, String userId) {
		String usergroupName, isWritten = ApprovalStatus.WRITTEN_N.getValue();
		Integer approvalId = 0;
		usergroupName = usergroupRequestBody.getGroupName();
		ObjectMapper objectMapper = new ObjectMapper();
		ModelMapper modelMapper = new ModelMapper();
		modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
		Integer userIdInt = Integer.valueOf(userId);
		String name;
		name = userRepository.findNameById(userIdInt);
		if (name == null){
			throw new CommonException(CommonException.GENERIC_ERROR_CODE,"User not valid for id " + userId);
		}

		//Check if usergroup already exist
		if (usergroupName == null){
			logger.error("New user group name cannot be empty");
			throw new CommonException(CommonException.GENERIC_ERROR_CODE,"Empty user group name", HttpStatus.INTERNAL_SERVER_ERROR);
		}
		Integer existingUsergroup = 0;
		existingUsergroup = userGroupRepository.findCountByGroupNameAndGroupStatus(usergroupName,UsergroupStatus.ACTIVE.getValue());
		if (existingUsergroup != 0){
			logger.error("User group name already taken! Error:403, Code: 40003");
			throw new CommonException("40003","User group name already taken! Error:403, Code: 40003", HttpStatus.FORBIDDEN);
		}


		Integer functionId = 0;
		try{
			functionId = Integer.parseInt(usergroupRequestBody.getFunctionId());
		}catch (Exception e){
			logger.warn("Invalid function id!");
		}
		ConfigFunction configFunction = configFunctionRepository.findOne(functionId);
		Boolean requireApproval = configFunction.isApprovalRequired();
		Timestamp now = new Timestamp(new Date().getTime());
		Integer groupId = 0;
		List<Integer> approvalIdLs;
		List<Integer> usergroupIdList;
		String groupName = usergroupRequestBody.getGroupName();
		String accessType, scopeId ="";
		int moduleId, usergroupId;
		accessType = usergroupRequestBody.getAccessType().toUpperCase();
		moduleId = configFunction.getModule().getId();

		List functionList = usergroupRequestBody.getFunction();
		Integer functionListSize = functionList.size();
		if (requireApproval) {
			String lockingId = groupName, payload = "";
			List<Integer> approvalIdList = new ArrayList<>();
			List<BoUmApprovalUserGroup> boUmApprovalUsergroupList = new ArrayList<>();
			approvalIdList = approvalRepository.findIdByFunctionIdAndStatus((int)(functionId),ApprovalStatus.PENDING_APPROVAL.getValue());
			if (approvalIdList.size() != 0) {
				boUmApprovalUsergroupList = boUmApprovalUsergroupRepository.findByApprovalIdAndLockingId(approvalIdList, lockingId);
				if (boUmApprovalUsergroupList.size() != 0) {
					logger.error("Duplicate request create approval");
					throw new CommonException("40002", "Duplicate request create approval", HttpStatus.FORBIDDEN);
				}
			}

			//Creating Payload
			List<ApprovalUsergroupFunction> approvalUsergroupFunctionList = new ArrayList<>();
			for (int i = 0; i < functionListSize; i++){
				ApprovalUsergroupFunction approvalUsergroupFunction = new ApprovalUsergroupFunction();
				approvalUsergroupFunction = modelMapper.map(usergroupRequestBody.getFunction().get(i), ApprovalUsergroupFunction.class);
				approvalUsergroupFunctionList.add(approvalUsergroupFunction);
			}
			ApprovalUsergroup approvalUsergroup = new ApprovalUsergroup();
			approvalUsergroup.setAccessType(accessType);
			approvalUsergroup.setGroupName(groupName);
			approvalUsergroup.setFunction(approvalUsergroupFunctionList);
			try {
				payload = objectMapper.writeValueAsString(approvalUsergroup);
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}

			//Write to approval table
			String description = groupName;

			approvalIdLs =  approvalRepository.insert(functionId, userIdInt, description, MaintenanceActionType.ADD.getValue(),ApprovalStatus.PENDING_APPROVAL.getValue(), now, name, now, name);
			if (approvalIdLs.size() == 0){
				logger.warn("Write to approval repository failed");
			}
			approvalId = approvalIdLs.get(0);
			boUmApprovalUsergroupRepository.insert(approvalId,MaintenanceActionType.USERGROUP_APPROVAL_STATE_A .getValue(),lockingId,payload,now,name,now,name);

		}else{
			usergroupIdList = userGroupRepository.insert(groupName,MaintenanceActionType.USER_GROUP_STATUS_ACTIVE.getValue() , now, name, now, name);
			groupId = usergroupIdList.get(0);
			if (accessType.equals(MaintenanceActionType.ACCESS_TYPE_MAKER.getValue())){
				scopeId = configFunction.getMakerScope();
			}else if (accessType.equals(MaintenanceActionType.ACCESS_TYPE_CHECKER.getValue())){
				scopeId = configFunction.getCheckerScope();
			}else if (accessType.equals(MaintenanceActionType.ACCESS_TYPE_INQUIRER.getValue())){
				scopeId = configFunction.getInquirerScope();
			}
			Integer isSuccessful = 0;
			for (int i = 0; i < functionListSize; i++){
				String accessFunctionId = "";
				accessFunctionId = usergroupRequestBody.getFunction().get(i).getFunctionId();
				userGroupAccessRepository.insert(String.valueOf(groupId),accessFunctionId,scopeId,accessType,ApprovalStatus.APPROVED.getValue(),now,name,now,name);
			}
			isWritten = ApprovalStatus.WRITTEN_Y.getValue();
		}

		Usergroup usergroup = new Usergroup();
		usergroup.setApprovalId(String.valueOf(approvalId));
		usergroup.setIsWritten(isWritten);

		HashMap<String, Object> additionalDataMap = new HashMap<String, Object>();

		additionalDataMap.put("boRefNumber", approvalId);
		additionalDataMap.put("usergroupName", usergroupRequestBody.getGroupName());
		additionalDataMap.put("role", usergroupRequestBody.getAccessType());
		additionalDataMap.put("module", "User Management");

		additionalDataHolder.setMap(additionalDataMap);
		logger.debug("audit log data : {}", JsonUtil.objectToJson(additionalDataHolder.getMap()));

		return usergroup;
	}
}
