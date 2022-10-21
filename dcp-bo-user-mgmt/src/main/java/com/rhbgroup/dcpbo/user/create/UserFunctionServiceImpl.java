package com.rhbgroup.dcpbo.user.create;

import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.rhbgroup.dcpbo.user.usergroupdelete.UsergroupDeleteRequestVo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rhbgroup.dcp.util.JsonUtil;
import com.rhbgroup.dcpbo.common.audit.AdditionalDataHolder;
import com.rhbgroup.dcpbo.common.exception.CommonException;
import com.rhbgroup.dcpbo.user.common.model.bo.BoConfigDepartment;
import com.rhbgroup.dcpbo.user.common.BoConfigDepartmentRepo;
import com.rhbgroup.dcpbo.user.common.BoData;
import com.rhbgroup.dcpbo.user.common.model.bo.BoUmApprovalUser;
import com.rhbgroup.dcpbo.user.common.BoUmApprovalUserRepo;
import com.rhbgroup.dcpbo.user.common.model.bo.BoUserApproval;
import com.rhbgroup.dcpbo.user.common.BoUserApprovalRepo;
import com.rhbgroup.dcpbo.user.common.UserGroupRepository;
import com.rhbgroup.dcpbo.user.common.UserRepository;
import com.rhbgroup.dcpbo.user.common.UserUsergroupRepository;
import com.rhbgroup.dcpbo.user.common.model.bo.UserUsergroup;
import com.rhbgroup.dcpbo.user.common.model.bo.Usergroup;
import com.rhbgroup.dcpbo.user.enums.MaintenanceActionType;
import com.rhbgroup.dcpbo.user.info.model.bo.ConfigFunction;
import com.rhbgroup.dcpbo.user.info.ConfigFunctionRepo;
import com.rhbgroup.dcpbo.user.info.model.bo.User;

public class UserFunctionServiceImpl implements UserFunctionService {

	private static Logger logger = LogManager.getLogger(UserFunctionServiceImpl.class);

	@Autowired
	AdditionalDataHolder additionalDataHolder;

	@Autowired
	UserRepository userRepo;

	@Autowired
	BoConfigDepartmentRepo departmentRepo;

	@Autowired
	ConfigFunctionRepo configFunctionRepo;

	@Autowired
	UserGroupRepository usergroupRepo;

	@Autowired
	UserUsergroupRepository userUserGroupRepo;

	@Autowired
	BoUserApprovalRepo boUserApprovalRepo;

	@Autowired
	BoUmApprovalUserRepo boUmApprovalUserRepo;

	public BoData createBoUser(Integer creatorId, UserCreateRequestVo request) {
		Timestamp actionTime = new Timestamp(new Date().getTime());

		logger.info("INSIDE createBoUser...creatorId: " + creatorId);
		User creator = userRepo.findOne(creatorId);

		if (creator == null) {
			throw new CommonException("40003", "Creator ID not existed.");
		}

		UserCreateResponseVo response = new UserCreateResponseVo();
		logger.info("Data validation...");

		List<User> userList = userRepo.findByUsername(request.getUsername());
		if (userList != null && !userList.isEmpty()) {
			throw new CommonException("40003", "User with same user name existed.");
		}

		if (request.getFunctionId() == null) {
			throw new CommonException(CommonException.GENERIC_ERROR_CODE, "Function ID cannot be null.");
		}

		ConfigFunction configFunction = configFunctionRepo.findOne(request.getFunctionId());
		if (configFunction == null) {
			throw new CommonException(CommonException.TRANSACTION_NOT_FOUND_ERROR, "Function ID not found.");
		}

		if (request.getDepartment() == null) {
			throw new CommonException(CommonException.GENERIC_ERROR_CODE, "Department cannot be null.");
		} else {
			BoConfigDepartment department = departmentRepo.findOne(request.getDepartment().getDepartmentId());
			if (department == null) {
				throw new CommonException(CommonException.GENERIC_ERROR_CODE, "Department id not exist.");
			}
		}

		logger.info("Performing approval process...");
		if (!configFunction.isApprovalRequired()) {
			logger.info("Approve not require...");
			User createUser = new User();
			createUser.setUsername(request.getUsername());
			createUser.setName(request.getName());
			createUser.setEmail(request.getEmail());
			createUser.setUserDepartmentId(request.getDepartment().getDepartmentId());
			createUser.setFailedLoginCount(0);
			createUser.setUserStatusId(MaintenanceActionType.USER_STATUS_ID_ACTIVE.getValue());
			createUser.setCreatedBy(creator.getUsername());
			createUser.setCreatedTime(actionTime);
			createUser.setUpdatedBy(creator.getUsername());
			createUser.setUpdatedTime(actionTime);
			createUser = userRepo.saveAndFlush(createUser);

			logger.info("Finish insert user record...");
			Integer userId = createUser.getId();
			List<UserFunctionUserGroupVo> usergroupList = request.getUsergroup();
			if (usergroupList != null && usergroupList.size() > 0) {
				for (UserFunctionUserGroupVo groupVo : usergroupList) {
					Usergroup usergroup = usergroupRepo.findOne(groupVo.getGroupId());
					if (usergroup == null) {
						throw new CommonException(CommonException.GENERIC_ERROR_CODE, "User Group id not exist.");
					}

					UserUsergroup userUserGroup = new UserUsergroup();
					userUserGroup.setUserId(userId);
					userUserGroup.setUserGroupId(groupVo.getGroupId());
					userUserGroup.setStatus(MaintenanceActionType.STATUS_APPROVED.getValue());
					userUserGroup.setCreatedBy(creator.getUsername());
					userUserGroup.setCreatedTime(actionTime);
					userUserGroup.setUpdatedBy(creator.getUsername());
					userUserGroup.setUpdatedTime(actionTime);
					userUserGroupRepo.saveAndFlush(userUserGroup);
				}
			}

			logger.info("Finish insert user user group record...");
			response.setIsWritten(MaintenanceActionType.YES.getValue());
			response.setApprovalId(0);
			populateAdditionalAuditDataCreateUser(request, response.getApprovalId());

		} else {
			logger.info("Approval require...");
			List<BoUserApproval> approvalList = boUserApprovalRepo.findAllByFunctionIdAndStatus(request.getFunctionId(),
					MaintenanceActionType.STATUS_PENDING_APPROVAL.getValue());
			if (approvalList != null && approvalList.size() > 0) {
				for (BoUserApproval singleRecord : approvalList) {
					List<BoUmApprovalUser> umApproval = boUmApprovalUserRepo
							.findAllByApprovalIdAndLockingId(singleRecord.getId(), request.getUsername());
					if (umApproval != null && umApproval.size() > 0) {
						throw new CommonException("40002", "Same approval request exist.");
					}
				}
			}

			BoUserApproval boUserApproval = new BoUserApproval();
			boUserApproval.setFunctionId(request.getFunctionId());
			boUserApproval.setActionType(MaintenanceActionType.ADD.getValue());
			boUserApproval.setStatus(MaintenanceActionType.STATUS_PENDING_APPROVAL.getValue());
			boUserApproval.setDescription(request.getUsername() + "-" + request.getName());
			boUserApproval.setCreatorId(creatorId);
			boUserApproval.setCreatedBy(creator.getUsername());
			boUserApproval.setCreatedTime(actionTime);
			boUserApproval.setUpdatedBy(creator.getUsername());
			boUserApproval.setUpdatedTime(actionTime);
			boUserApproval = boUserApprovalRepo.saveAndFlush(boUserApproval);

			logger.info("Finish insert user approval record...");

			// Create PAYLOAD
			List<UserFunctionUserGroupVo> usergroupList = request.getUsergroup();

			UserFunctionDepartmentVo departmentVo = new UserFunctionDepartmentVo();
			departmentVo.setDepartmentId(request.getDepartment().getDepartmentId());
			departmentVo.setDepartmentName(request.getDepartment().getDepartmentName());

			UserPayloadVo payload = new UserPayloadVo();
			payload.setUsername(request.getUsername());
			payload.setDepartment(departmentVo);
			payload.setEmail(request.getEmail());
			payload.setName(request.getName());
			payload.setGroup(usergroupList);
			payload.setStatus(MaintenanceActionType.ADD.getValue());

			String payloadInString = "";
			ObjectMapper mapper = new ObjectMapper();
			try {
				payloadInString = mapper.writeValueAsString(payload);
				logger.info("payloadInString");
				logger.info("============================================");
				logger.info(payloadInString);
				logger.info("============================================");
			} catch (JsonProcessingException e) {
				logger.info("Error at JsonProcessingException : " + e.getLocalizedMessage());
			}

			BoUmApprovalUser insertRecord = new BoUmApprovalUser();
			insertRecord.setApprovalId(boUserApproval.getId());
			insertRecord.setPayload(payloadInString);
			insertRecord.setLockingId(request.getUsername());
			insertRecord.setState(MaintenanceActionType.USER_APPROVAL_STATE_A.getValue());
			insertRecord.setCreatedBy(creator.getUsername());
			insertRecord.setCreatedTime(actionTime);
			insertRecord.setUpdatedBy(creator.getUsername());
			insertRecord.setUpdatedTime(actionTime);
			boUmApprovalUserRepo.saveAndFlush(insertRecord);

			logger.info("Finish insert um approval user record...");

			response.setIsWritten(MaintenanceActionType.NO.getValue());
			response.setApprovalId(boUserApproval.getId());
			populateAdditionalAuditDataCreateUser(request, boUserApproval.getId());
		}
		return response;
	}

	private void populateAdditionalAuditDataCreateUser(UserCreateRequestVo request, Integer approvalId) {
		HashMap<String, Object> additionalDataMap = new HashMap<String, Object>();
		additionalDataMap.put("boRefNumber", approvalId);
		additionalDataMap.put("usergroups", request.getUsergroup());
		additionalDataMap.put("module", "User Management");

		additionalDataHolder.setMap(additionalDataMap);
		logger.debug("audit log data : {}", JsonUtil.objectToJson(additionalDataHolder.getMap()));
	}

	public BoData updateBoUser(Integer creatorId, UserUpdateRequestVo request, String updateUserId) {

		Timestamp actionTime = new Timestamp(new Date().getTime());

		logger.info("INSIDE updateBoUser...creatorId: " + creatorId);

		User creator = userRepo.findOne(creatorId);

		if (creator == null) {
			throw new CommonException("40003", "Creator ID not existed.");
		}

		UserCreateResponseVo response = new UserCreateResponseVo();
		logger.info("Data validation...");

		User updateUser = userRepo.findOne(Integer.parseInt(updateUserId));
		if (updateUser == null) {
			throw new CommonException("40003", "User not exist.");
		}

		if (request.getFunctionId() == null) {
			throw new CommonException(CommonException.GENERIC_ERROR_CODE, "Function ID cannot be null.");
		}

		ConfigFunction configFunction = configFunctionRepo.findOne(request.getFunctionId());
		if (configFunction == null) {
			throw new CommonException(CommonException.TRANSACTION_NOT_FOUND_ERROR, "Function ID not found.");
		}

		if (request.getInput() == null) {
			throw new CommonException(CommonException.GENERIC_ERROR_CODE, "Input cannot be null.");
		}

		if (request.getInput().getDepartmentId() != null) {
			BoConfigDepartment department = departmentRepo.findOne(request.getInput().getDepartmentId());
			if (department == null) {
				throw new CommonException(CommonException.GENERIC_ERROR_CODE, "Department id not exist.");
			}
		}

		logger.info("Performing approval process...");
		if (!configFunction.isApprovalRequired()) {
			logger.info("Approve not require...");
			updateUser.setName(
					request.getInput().getName() == null ? request.getCache().getName() : request.getInput().getName());
			updateUser.setEmail(request.getInput().getEmail() == null ? request.getCache().getEmail()
					: request.getInput().getEmail());
			updateUser.setUserDepartmentId(
					request.getInput().getDepartmentId() == null ? request.getCache().getDepartmentId()
							: request.getInput().getDepartmentId());
			updateUser.setUserStatusId(request.getInput().getStatus() == null ? request.getCache().getStatus()
					: request.getInput().getStatus());
			// Reset failed login count when user status ID is changed to "A"
			if(request.getInput().getStatus() != null || 
					request.getCache().getStatus() != null) {
				logger.info("Reset failed login count when user status ID is changed to A");
				String statusId = request.getInput().getStatus() != null ? request.getInput().getStatus() : request.getCache().getStatus();
				logger.info("statusId: " + statusId);
				if((MaintenanceActionType.USER_STATUS_ID_ACTIVE.getValue()).equals(statusId)) {
					logger.info("Reset failed login count to 0");
					updateUser.setFailedLoginCount(0);
				}
			}
			updateUser.setUpdatedBy(creator.getUsername());
			updateUser.setUpdatedTime(actionTime);
			updateUser = userRepo.saveAndFlush(updateUser);

			logger.info("Finish update user record...");

			Integer userId = updateUser.getId();
			List<UserFunctionUserGroupVo> usergroupList = request.getInput().getGroup();
			if (usergroupList != null && usergroupList.size() > 0) {
				for (UserFunctionUserGroupVo groupVo : usergroupList) {
					Usergroup usergroup = usergroupRepo.findOne(groupVo.getGroupId());
					if (usergroup == null) {
						throw new CommonException(CommonException.GENERIC_ERROR_CODE, "User Group id not exist.");
					}

					UserUsergroup userUserGroup = userUserGroupRepo.findOneByUserIdAndUserGroupId(userId,
							groupVo.getGroupId());
					if (userUserGroup == null) {
						userUserGroup = new UserUsergroup();
						userUserGroup.setStatus(MaintenanceActionType.USER_USER_GROUP_STATUS_ACTIVE.getValue());
						userUserGroup.setCreatedBy(creator.getUsername());
						userUserGroup.setCreatedTime(actionTime);
					} else {
						if (userUserGroup.getStatus()
								.equals(MaintenanceActionType.USER_USER_GROUP_STATUS_DELETED.getValue())) {
							userUserGroup.setStatus(MaintenanceActionType.STATUS_APPROVED.getValue());
						}
					}

					userUserGroup.setUserId(userId);
					userUserGroup.setUserGroupId(groupVo.getGroupId());
					userUserGroup.setUpdatedBy(creator.getUsername());
					userUserGroup.setUpdatedTime(actionTime);
					userUserGroupRepo.saveAndFlush(userUserGroup);
				}

				List<UserUsergroup> userUserGroupInDBList = userUserGroupRepo.findAllByUserId(userId);
				if (userUserGroupInDBList != null && userUserGroupInDBList.size() > 0) {
					for (UserUsergroup userUserGroupInDB : userUserGroupInDBList) {
						Boolean needDelete = Boolean.TRUE;
						for (UserFunctionUserGroupVo groupVo : usergroupList) {
							if (userUserGroupInDB.getUserGroupId() == groupVo.getGroupId()) {
								needDelete = Boolean.FALSE;
							}
						}
						if (needDelete) {
							logger.info("Change status to DELETE for record userID=" + userUserGroupInDB.getUserId()
									+ ", groupId=" + userUserGroupInDB.getUserGroupId());
							userUserGroupInDB
									.setStatus(MaintenanceActionType.USER_USER_GROUP_STATUS_DELETED.getValue());
							userUserGroupInDB.setUpdatedBy(creator.getUsername());
							userUserGroupInDB.setUpdatedTime(actionTime);
							userUserGroupRepo.saveAndFlush(userUserGroupInDB);
						}
					}
				}
			}

			logger.info("Finish Maintenance for user user group record...");
			response.setApprovalId(0);
			response.setIsWritten(MaintenanceActionType.YES.getValue());

			populateAdditionalAuditData(request, response.getApprovalId());
		} else {

			logger.info("Approval require...");
			List<BoUserApproval> approvalList = boUserApprovalRepo.findAllByFunctionIdAndStatus(request.getFunctionId(),
					MaintenanceActionType.STATUS_PENDING_APPROVAL.getValue());
			if (approvalList != null && approvalList.size() > 0) {
				for (BoUserApproval singleRecord : approvalList) {
					List<BoUmApprovalUser> umApproval = boUmApprovalUserRepo
							.findAllByApprovalIdAndLockingId(singleRecord.getId(), updateUser.getUsername());
					if (umApproval != null && umApproval.size() > 0) {
						throw new CommonException("40002", "Same approval request exist.");
					}
				}
			}

			BoUserApproval boUserApproval = new BoUserApproval();
			boUserApproval.setFunctionId(request.getFunctionId());
			boUserApproval.setActionType(MaintenanceActionType.EDIT.getValue());
			boUserApproval.setStatus(MaintenanceActionType.STATUS_PENDING_APPROVAL.getValue());
			boUserApproval.setDescription(updateUser.getUsername() + "-" + updateUser.getName());
			boUserApproval.setCreatorId(creatorId);
			boUserApproval.setCreatedBy(creator.getUsername());
			boUserApproval.setCreatedTime(actionTime);
			boUserApproval.setUpdatedBy(creator.getUsername());
			boUserApproval.setUpdatedTime(actionTime);
			boUserApproval = boUserApprovalRepo.saveAndFlush(boUserApproval);

			logger.info("Finish insert user approval record...");

			Integer approvalId = boUserApproval.getId();
			Boolean success = performInsertToBoUmApprovalUser(request, approvalId, creator, updateUser);

			if (success) {
				logger.info("Finish insert um approval user record...");
				response.setIsWritten(MaintenanceActionType.NO.getValue());
				response.setApprovalId(boUserApproval.getId());
			}
			populateAdditionalAuditData(request, boUserApproval.getId());
		}

		return response;
	}

	private void populateAdditionalAuditData(UserUpdateRequestVo request, Integer approvalId) {
		HashMap<String, Object> additionalDataMap = new HashMap<String, Object>();

		additionalDataMap.put("boRefNumber", approvalId);
		additionalDataMap.put("usergroups", request.getInput().getGroup());
		additionalDataMap.put("module", "User Management");

		additionalDataHolder.setMap(additionalDataMap);
		logger.debug("audit log data : {}", JsonUtil.objectToJson(additionalDataHolder.getMap()));

	}

	public BoData deleteBoUser(Integer creatorId, UserDeleteRequestVo request, Integer deleteUserId) {

		UserCreateResponseVo response = new UserCreateResponseVo();

		Timestamp actionTime = new Timestamp(new Date().getTime());

		logger.info("INSIDE deleteBoUser...creatorId: " + creatorId);
		logger.info("Data validation...");
		User creator = userRepo.findOne(creatorId);
		if (creator == null) {
			throw new CommonException("40003", "Creator ID not existed.");
		}

		User deleteUser = userRepo.findOne(deleteUserId);
		if (deleteUser == null) {
			throw new CommonException("40003", "User not exist.");
		}

		if (request.getFunctionId() == null) {
			throw new CommonException(CommonException.GENERIC_ERROR_CODE, "Function ID cannot be null.");
		}

		ConfigFunction configFunction = configFunctionRepo.findOne(request.getFunctionId());
		if (configFunction == null) {
			throw new CommonException(CommonException.TRANSACTION_NOT_FOUND_ERROR, "Function ID not found.");
		}

		logger.info("Performing approval process...");
		if (!configFunction.isApprovalRequired()) {
			deleteUser.setUserStatusId(MaintenanceActionType.USER_STATUS_ID_DELETED.getValue());
			deleteUser.setUpdatedBy(creator.getUsername());
			deleteUser.setUpdatedTime(actionTime);
			userRepo.saveAndFlush(deleteUser);

			List<UserUsergroup> uugroupList = userUserGroupRepo.findAllByUserId(deleteUserId);
			if (uugroupList != null && uugroupList.size() > 0) {
				for (UserUsergroup uugroup : uugroupList) {
					uugroup.setStatus(MaintenanceActionType.USER_STATUS_ID_DELETED.getValue());
					uugroup.setUpdatedBy(creator.getUsername());
					uugroup.setUpdatedTime(actionTime);
					userUserGroupRepo.saveAndFlush(uugroup);
				}
			}

			response.setApprovalId(0);
			response.setIsWritten(MaintenanceActionType.YES.getValue());
			populateAdditionalAuditDataDelUser(deleteUser, response.getApprovalId());
		} else {

			logger.info("Approval require...");
			List<BoUserApproval> approvalList = boUserApprovalRepo.findAllByFunctionIdAndStatus(request.getFunctionId(),
					MaintenanceActionType.STATUS_PENDING_APPROVAL.getValue());
			if (approvalList != null && approvalList.size() > 0) {
				for (BoUserApproval singleRecord : approvalList) {
					List<BoUmApprovalUser> umApproval = boUmApprovalUserRepo
							.findAllByApprovalIdAndLockingId(singleRecord.getId(), deleteUser.getUsername());
					if (umApproval != null && umApproval.size() > 0) {
						throw new CommonException("40002", "Same approval request exist.");
					}
				}
			}

			logger.info("Perform insert user approval record...");
			BoUserApproval boUserApproval = new BoUserApproval();
			boUserApproval.setFunctionId(request.getFunctionId());
			boUserApproval.setActionType(MaintenanceActionType.DELETE.getValue());
			boUserApproval.setStatus(MaintenanceActionType.STATUS_PENDING_APPROVAL.getValue());
			boUserApproval.setDescription(deleteUser.getUsername() + "-" + deleteUser.getName());
			boUserApproval.setCreatorId(creatorId);
			boUserApproval.setCreatedBy(creator.getUsername());
			boUserApproval.setCreatedTime(actionTime);
			boUserApproval.setUpdatedBy(creator.getUsername());
			boUserApproval.setUpdatedTime(actionTime);
			boUserApproval = boUserApprovalRepo.saveAndFlush(boUserApproval);

			logger.info("Finish insert user approval record...");

			UserPayloadDeleteVo payloadDelete = new UserPayloadDeleteVo();
			payloadDelete.setUserId(deleteUser.getId());
			payloadDelete.setUsername(deleteUser.getUsername());
			payloadDelete.setName(deleteUser.getName());
			payloadDelete.setStatus(deleteUser.getUserStatusId());
			payloadDelete.setEmail(deleteUser.getEmail());

			String payloadInString = convertObjectToJsonString(payloadDelete);

			logger.info("Perform insert UM user approval record...");
			BoUmApprovalUser insertRecord = new BoUmApprovalUser();
			insertRecord.setApprovalId(boUserApproval.getId());
			insertRecord.setPayload(payloadInString);
			insertRecord.setLockingId(deleteUser.getUsername());
			insertRecord.setState(MaintenanceActionType.USER_APPROVAL_STATE_B.getValue());
			insertRecord.setCreatedBy(creator.getUsername());
			insertRecord.setCreatedTime(actionTime);
			insertRecord.setUpdatedBy(creator.getUsername());
			insertRecord.setUpdatedTime(actionTime);
			boUmApprovalUserRepo.saveAndFlush(insertRecord);

			logger.info("Finish insert UM user approval record...");
			response.setIsWritten(MaintenanceActionType.NO.getValue());
			response.setApprovalId(boUserApproval.getId());
			populateAdditionalAuditDataDelUser(deleteUser, boUserApproval.getId());
		}
		return response;
	}

	private void populateAdditionalAuditDataDelUser(User deleteUser, Integer approvalId) {
		HashMap<String, Object> additionalDataMap = new HashMap<String, Object>();
		additionalDataMap.put("email", deleteUser.getUsername());
		additionalDataMap.put("department", deleteUser.getUserDepartmentId());
		additionalDataMap.put("boRefNumber", approvalId);
		additionalDataMap.put("username", deleteUser.getUsername() );
		additionalDataMap.put("module", "User Management");

		additionalDataHolder.setMap(additionalDataMap);
		logger.debug("audit log data : {}", JsonUtil.objectToJson(additionalDataHolder.getMap()));
	}

	public UserUpdateUMPayloadVo updatePayloadGenerate(UserUpdateRequestVo request, String state, User updateUser) {

		UserUpdateUMPayloadVo payload = new UserUpdateUMPayloadVo();
		payload.setUsername(updateUser.getUsername());
		UserFunctionDepartmentVo department = new UserFunctionDepartmentVo();
		payload.setUserId(updateUser.getId());

		if (state.equals(MaintenanceActionType.USER_APPROVAL_STATE_A.getValue())) {
			payload.setName(request.getInput().getName());
			payload.setEmail(request.getInput().getEmail());
			department.setDepartmentId(request.getInput().getDepartmentId());
			department.setDepartmentName(request.getInput().getDepartmentName());
			payload.setDepartment(department);
			payload.setStatus(request.getInput().getStatus());
			payload.setGroup(request.getInput().getGroup());
		} else {
			if (state.equals(MaintenanceActionType.USER_APPROVAL_STATE_B.getValue())) {
				payload.setName(request.getCache().getName());
				payload.setEmail(request.getCache().getEmail());
				department.setDepartmentId(request.getCache().getDepartmentId());
				department.setDepartmentName(request.getCache().getDepartmentName());
				payload.setDepartment(department);
				payload.setStatus(request.getCache().getStatus());
				payload.setGroup(request.getCache().getGroup());
			}
		}
		return payload;
	}

	public Boolean performInsertToBoUmApprovalUser(UserUpdateRequestVo request, Integer approvalId, User creator,
			User updateUser) {

		// Create PAYLOAD
		UserUpdateUMPayloadVo payloadStateA = updatePayloadGenerate(request,
				MaintenanceActionType.USER_APPROVAL_STATE_A.getValue(), updateUser);
		UserUpdateUMPayloadVo payloadStateB = updatePayloadGenerate(request,
				MaintenanceActionType.USER_APPROVAL_STATE_B.getValue(), updateUser);

		Timestamp actionTime = new Timestamp(new Date().getTime());

		String payloadInStringStateA = convertObjectToJsonString(payloadStateA);
		BoUmApprovalUser insertRecordA = new BoUmApprovalUser();
		insertRecordA.setApprovalId(approvalId);
		insertRecordA.setPayload(payloadInStringStateA);
		insertRecordA.setLockingId(updateUser.getUsername());
		insertRecordA.setState(MaintenanceActionType.USER_APPROVAL_STATE_A.getValue());
		insertRecordA.setCreatedBy(creator.getUsername());
		insertRecordA.setCreatedTime(actionTime);
		insertRecordA.setUpdatedBy(creator.getUsername());
		insertRecordA.setUpdatedTime(actionTime);
		boUmApprovalUserRepo.saveAndFlush(insertRecordA);

		String payloadInStringStateB = convertObjectToJsonString(payloadStateB);
		BoUmApprovalUser insertRecordB = new BoUmApprovalUser();
		insertRecordB.setApprovalId(approvalId);
		insertRecordB.setPayload(payloadInStringStateB);
		insertRecordB.setLockingId(updateUser.getUsername());
		insertRecordB.setState(MaintenanceActionType.USER_APPROVAL_STATE_B.getValue());
		insertRecordB.setCreatedBy(creator.getUsername());
		insertRecordB.setCreatedTime(actionTime);
		insertRecordB.setUpdatedBy(creator.getUsername());
		insertRecordB.setUpdatedTime(actionTime);
		boUmApprovalUserRepo.saveAndFlush(insertRecordB);

		return true;
	}

	public String convertObjectToJsonString(Object obj) {
		ObjectMapper mapper = new ObjectMapper();
		String payloadInString = "";
		try {
			payloadInString = mapper.writeValueAsString(obj);
			logger.info("converting object ");
			logger.info("============================================");
			logger.info(payloadInString);
			logger.info("============================================");
		} catch (JsonProcessingException e) {
			logger.info("Error at JsonProcessingException : " + e.getLocalizedMessage());
		}

		return payloadInString;
	}
}
