package com.rhbgroup.dcpbo.user.workflow.user;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.rhbgroup.dcp.util.JsonUtil;
import com.rhbgroup.dcpbo.common.audit.AdditionalDataHolder;
import com.rhbgroup.dcpbo.common.exception.CommonException;
import com.rhbgroup.dcpbo.user.common.ApprovalRepository;
import com.rhbgroup.dcpbo.user.common.BoConfigDepartmentRepo;
import com.rhbgroup.dcpbo.user.common.BoData;
import com.rhbgroup.dcpbo.user.common.BoUmApprovalUserRepo;
import com.rhbgroup.dcpbo.user.common.UserGroupRepository;
import com.rhbgroup.dcpbo.user.common.UserRepository;
import com.rhbgroup.dcpbo.user.common.UserUsergroupRepository;
import com.rhbgroup.dcpbo.user.common.model.bo.Approval;
import com.rhbgroup.dcpbo.user.common.model.bo.BoConfigDepartment;
import com.rhbgroup.dcpbo.user.common.model.bo.BoUmApprovalUser;
import com.rhbgroup.dcpbo.user.common.model.bo.UserUsergroup;
import com.rhbgroup.dcpbo.user.common.model.bo.Usergroup;
import com.rhbgroup.dcpbo.user.enums.BoUserStatus;
import com.rhbgroup.dcpbo.user.enums.MaintenanceActionType;
import com.rhbgroup.dcpbo.user.info.model.bo.User;

public class WFUserApprovalServiceImpl implements WFUserApprovalService {

	private static Logger logger = LogManager.getLogger(WFUserApprovalService.class);
	@Autowired
	ApprovalRepository approvalRepository;

	@Autowired
	UserRepository userRepository;

	@Autowired
	BoConfigDepartmentRepo boConfigDepartmentRepo;

	@Autowired
	UserUsergroupRepository userUserGroupRepo;

	@Autowired
	UserGroupRepository userGroupRepo;

	@Autowired
	BoUmApprovalUserRepo boUmApprovalUserRepo;

	@Autowired
	AdditionalDataHolder additionalDataHolder;

    public static final String APPROVAL_STATUS_APPROVED = "A";
    public static final String APPROVAL_STATUS_REJECTED = "R";
    public static final String REQUEST_APPROVED = "APPROVED";
    public static final String REQUEST_REJECTED = "REJECTED";

    public BoData getWorkflowApprovalDetail(Integer userId, int approvalId) {

		WFUserApprovalDetail wfUserApprovalDetail = new WFUserApprovalDetail();
		wfUserApprovalDetail.setApprovalId(approvalId);

		Approval approval = findApproval(approvalId, "Search");

		wfUserApprovalDetail.setActionType(approval.getActionType());

		String sTimestamp = approval.getUpdatedTime().toString();
		wfUserApprovalDetail.setUpdatedTime(sTimestamp);

		wfUserApprovalDetail.setReason(approval.getReason());

		String isCreator = "N";
		if (userId == approval.getCreatorId()) {
			isCreator = "Y";
		}
		wfUserApprovalDetail.setIsCreator(isCreator);

		logger.debug("creatorId : {}", approval.getCreatorId());
		String creatorName = userRepository.findNameById(approval.getCreatorId());
		logger.debug("creatorName : {}", creatorName);
		wfUserApprovalDetail.setCreatorName(creatorName);
		wfUserApprovalDetail.setCreatedTime(approval.getCreatedTime().toString());
		wfUserApprovalDetail.setUpdatedBy(approval.getUpdatedBy());

        if(approval.getStatus().equals(APPROVAL_STATUS_APPROVED)) wfUserApprovalDetail.setApprovalStatus(REQUEST_APPROVED);
        else if (approval.getStatus().equals(APPROVAL_STATUS_REJECTED)) wfUserApprovalDetail.setApprovalStatus(REQUEST_REJECTED);

        List<BoUmApprovalUser> userManagementApprovalUserList = findApprovalData(approvalId, approval.getActionType());
		if (approval.getActionType().equalsIgnoreCase(MaintenanceActionType.EDIT.getValue())) {
			Collections.sort(userManagementApprovalUserList, new Comparator<BoUmApprovalUser>() {
				public int compare(BoUmApprovalUser contentVo1, BoUmApprovalUser contentVo2) {
					return contentVo2.getState().compareTo(contentVo1.getState());
				}
			});

			WFUserPayload payloadBefore = JsonUtil.jsonToObject(userManagementApprovalUserList.get(0).getPayload(),
					WFUserPayload.class);
			WFUserPayload payloadAfter = JsonUtil.jsonToObject(userManagementApprovalUserList.get(1).getPayload(),
					WFUserPayload.class);

			wfUserApprovalDetail.setUsername(
					new WFUserApprovalDetailValue(payloadBefore.getUsername(), payloadAfter.getUsername()));

			wfUserApprovalDetail
					.setEmail(new WFUserApprovalDetailValue(payloadBefore.getEmail(), payloadAfter.getEmail()));

			wfUserApprovalDetail
					.setName(new WFUserApprovalDetailValue(payloadBefore.getName(), payloadAfter.getName()));

			String departmentNameBefore = "";
			String departmentNameAfter = "";
			departmentNameBefore = payloadBefore.getDepartment().getDepartmentName();
			departmentNameAfter = payloadAfter.getDepartment().getDepartmentName();
			wfUserApprovalDetail
					.setDepartmentName(new WFUserApprovalDetailValue(departmentNameBefore, departmentNameAfter));

			wfUserApprovalDetail
					.setStatus(new WFUserApprovalDetailValue(payloadBefore.getStatus(), payloadAfter.getStatus()));

			List<String> usergroupListBefore = new ArrayList<String>();
			List<String> usergroupListAfter = new ArrayList<String>();
			for (WFUserGroupPayload wfUserGroupPayload : payloadBefore.getGroup()) {
				usergroupListBefore.add(wfUserGroupPayload.getGroupName());

			}
			for (WFUserGroupPayload wfUserGroupPayload : payloadAfter.getGroup()) {
				usergroupListAfter.add(wfUserGroupPayload.getGroupName());
			}
			wfUserApprovalDetail.setUsergroup(new WFUserApprovalDetailUserGroupValue(
					new WFUserGroupValue(usergroupListBefore), new WFUserGroupValue(usergroupListAfter)));

		} else if (approval.getActionType().equalsIgnoreCase(MaintenanceActionType.ADD.getValue())) {
			logger.debug("Add payload : {}", userManagementApprovalUserList.get(0).getPayload());
			WFUserPayload payloadAfter = JsonUtil.jsonToObject(userManagementApprovalUserList.get(0).getPayload(),
					WFUserPayload.class);

			wfUserApprovalDetail.setUsername(new WFUserApprovalDetailValue("", payloadAfter.getUsername()));

			wfUserApprovalDetail.setEmail(new WFUserApprovalDetailValue("", payloadAfter.getEmail()));

			wfUserApprovalDetail.setName(new WFUserApprovalDetailValue("", payloadAfter.getName()));

			String departmentNameBefore = "";
			String departmentNameAfter = "";
			departmentNameAfter = payloadAfter.getDepartment().getDepartmentName();
			wfUserApprovalDetail
					.setDepartmentName(new WFUserApprovalDetailValue(departmentNameBefore, departmentNameAfter));

			wfUserApprovalDetail.setStatus(new WFUserApprovalDetailValue("", payloadAfter.getStatus()));

			List<String> usergroupListBefore = new ArrayList<String>();
			List<String> usergroupListAfter = new ArrayList<String>();
			for (WFUserGroupPayload wfUserGroupPayload : payloadAfter.getGroup()) {
				usergroupListAfter.add(wfUserGroupPayload.getGroupName());
			}
			wfUserApprovalDetail.setUsergroup(new WFUserApprovalDetailUserGroupValue(
					new WFUserGroupValue(usergroupListBefore), new WFUserGroupValue(usergroupListAfter)));

		} else if (approval.getActionType().equalsIgnoreCase(MaintenanceActionType.DELETE.getValue())) {
			WFUserPayload payload = JsonUtil.jsonToObject(userManagementApprovalUserList.get(0).getPayload(),
					WFUserPayload.class);
			Integer payloadUserId = payload.getUserId();
			String payloadUsername = payload.getUsername();
			String payloadName = payload.getName();
			logger.debug("Delete Action Type payload [payloadUserId, payloadUsername, payloadName : {},{},{}]",
					payloadUserId, payloadUsername, payloadName);

			User user = userRepository.findOne(payloadUserId);

			wfUserApprovalDetail.setUsername(new WFUserApprovalDetailValue(payloadUsername, ""));

			wfUserApprovalDetail.setEmail(new WFUserApprovalDetailValue(user.getEmail(), ""));

			wfUserApprovalDetail.setName(new WFUserApprovalDetailValue(payloadName, ""));

			BoConfigDepartment boConfigDepartment = boConfigDepartmentRepo.findOne(user.getUserDepartmentId());
			String departmentName = "";
			departmentName = boConfigDepartment.getDepartmentName();
			wfUserApprovalDetail.setDepartmentName(new WFUserApprovalDetailValue(departmentName, ""));

			List<String> userGroupNameList = new ArrayList<String>();
			List<UserUsergroup> userUserGroupList = userUserGroupRepo.findAllByUserId(user.getId());
			if (!userUserGroupList.isEmpty()) {
				for (UserUsergroup userUserGroup : userUserGroupList) {

					Usergroup userGroup = userGroupRepo.findOneById(userUserGroup.getUserGroupId());
					userGroupNameList.add(userGroup.getGroupName());
				}
			}

			wfUserApprovalDetail.setStatus(new WFUserApprovalDetailValue(user.getUserStatusId(), ""));

			wfUserApprovalDetail.setUsergroup(new WFUserApprovalDetailUserGroupValue(
					new WFUserGroupValue(userGroupNameList), new WFUserGroupValue(new ArrayList<String>())));
		}
		return wfUserApprovalDetail;
	}

	public BoData approveCreate(Integer userId, WFUserApprovalActionDetail wfUserApprovalActionDetail) {

		Timestamp actionTime = new Timestamp(new Date().getTime());

		User requestUser = userRepository.findOne(userId);
		if (requestUser == null) {
			throw new CommonException(CommonException.GENERIC_ERROR_CODE, "Cannot find request user : " + userId);
		}

		Approval approval = findApproval(wfUserApprovalActionDetail.getApprovalId(),
				MaintenanceActionType.ADD.getValue());

		List<BoUmApprovalUser> pendingApprovalList = boUmApprovalUserRepo
				.findAllByApprovalId(wfUserApprovalActionDetail.getApprovalId());

		if (pendingApprovalList.isEmpty()) {
			throw new CommonException(CommonException.GENERIC_ERROR_CODE,
					"Cannot find Approval detail for id : " + wfUserApprovalActionDetail.getApprovalId());
		}
		if (pendingApprovalList.size() != 1) {
			throw new CommonException(CommonException.GENERIC_ERROR_CODE,
					"Add Action type expect only one approval detail but found " + pendingApprovalList.size());
		}
		BoUmApprovalUser boUmApprovalUser = pendingApprovalList.get(0);
		logger.debug("Add payload : {}", boUmApprovalUser.getPayload());
		WFUserPayload wfUserPayload = JsonUtil.jsonToObject(boUmApprovalUser.getPayload(), WFUserPayload.class);
		populateAuditAdditionalData(wfUserPayload, approval, wfUserApprovalActionDetail);
		String username = wfUserPayload.getUsername();
		List<User> userList = userRepository.findByUsername(username);
		if (userList != null && !userList.isEmpty()) {
			logger.error("found user with username : {}", username);
			throw new CommonException("40003", "This record already exists.");
		}

		approval.setStatus(MaintenanceActionType.STATUS_APPROVED.getValue());
		approval.setReason(wfUserApprovalActionDetail.getReason());
		approval.setUpdatedBy(requestUser.getUsername());
		approval.setUpdatedTime(new Timestamp(new Date().getTime()));
		approvalRepository.save(approval);

		User user = new User();
		user.setUsername(wfUserPayload.getUsername()); 
		user.setEmail(wfUserPayload.getEmail());
		user.setName(wfUserPayload.getName());
		user.setUserDepartmentId(wfUserPayload.getDepartment().getDepartmentId());
		user.setFailedLoginCount(0);
		user.setUserStatusId(MaintenanceActionType.USER_STATUS_ID_ACTIVE.getValue());
		user.setCreatedBy(approval.getUpdatedBy());
		user.setCreatedTime(new Timestamp(new Date().getTime()));
		user.setUpdatedBy(approval.getUpdatedBy());
		user.setUpdatedTime(new Timestamp(new Date().getTime()));
		userRepository.saveAndFlush(user);

		for (WFUserGroupPayload wfUserGroupPayload : wfUserPayload.getGroup()) {
			UserUsergroup userUserGroup = new UserUsergroup();
			userUserGroup.setUserId(user.getId());
			userUserGroup.setUserGroupId(wfUserGroupPayload.getGroupId());
			userUserGroup.setStatus(MaintenanceActionType.USER_STATUS_ID_ACTIVE.getValue());
			userUserGroup.setCreatedBy(approval.getUpdatedBy());
			userUserGroup.setCreatedTime(approval.getUpdatedTime());
			userUserGroup.setUpdatedBy(approval.getUpdatedBy());
			userUserGroup.setUpdatedTime(new Timestamp(new Date().getTime()));
			userUserGroupRepo.save(userUserGroup);
		}
		return wfUserApprovalActionDetail;
	}

	private void populateAuditAdditionalData(WFUserPayload wfUserPayload, Approval approval, WFUserApprovalActionDetail wfUserApprovalActionDetail) {
		HashMap<String, Object> additionalDataMap = new HashMap<String, Object>();
		additionalDataMap.put("username", wfUserPayload.getUsername());
		additionalDataMap.put("email", wfUserPayload.getEmail());
		additionalDataMap.put("name", wfUserPayload.getName());
		additionalDataMap.put("department", wfUserPayload.getDepartment());
		additionalDataMap.put("usergroup", wfUserPayload.getGroup());
		additionalDataMap.put("status", wfUserPayload.getStatus());

		additionalDataMap.put("boRefNumber", approval.getId());
		additionalDataMap.put("module", "User Management");
		additionalDataMap.put("reason", wfUserApprovalActionDetail.getReason());

		additionalDataHolder.setMap(additionalDataMap);
		logger.debug("audit log data : {}", JsonUtil.objectToJson(additionalDataHolder.getMap()));
	}

	@Override
	public BoData approveUpdate(Integer userId, WFUserApprovalActionDetail wfUserApprovalActionDetail) {

		User requestUser = userRepository.findOne(userId);
		if (requestUser == null) {
			throw new CommonException(CommonException.GENERIC_ERROR_CODE, "Cannot find request user : " + userId);
		}

		Approval approval = findApproval(wfUserApprovalActionDetail.getApprovalId(),
				MaintenanceActionType.EDIT.getValue());

		List<BoUmApprovalUser> pendingApprovalList = boUmApprovalUserRepo
				.findAllByApprovalId(wfUserApprovalActionDetail.getApprovalId());

		if (pendingApprovalList.isEmpty()) {
			throw new CommonException(CommonException.GENERIC_ERROR_CODE,
					"Cannot find Approval detail for id : " + wfUserApprovalActionDetail.getApprovalId());
		}
		if (pendingApprovalList.size() != 2) {
			throw new CommonException(CommonException.GENERIC_ERROR_CODE,
					"Add Action type expect two approval detail which is before and after but found "
							+ pendingApprovalList.size());
		}
		Collections.sort(pendingApprovalList, new Comparator<BoUmApprovalUser>() {
			public int compare(BoUmApprovalUser contentVo1, BoUmApprovalUser contentVo2) {
				return contentVo2.getState().compareTo(contentVo1.getState());
			}
		});

		approval.setStatus(MaintenanceActionType.STATUS_APPROVED.getValue());
		approval.setReason(wfUserApprovalActionDetail.getReason());
		approval.setUpdatedBy(requestUser.getUsername());
		approval.setUpdatedTime(new Timestamp(new Date().getTime()));
		approvalRepository.save(approval);

		WFUserPayload payload = JsonUtil.jsonToObject(pendingApprovalList.get(1).getPayload(), WFUserPayload.class);
		populateAuditAdditionalData(payload, approval, wfUserApprovalActionDetail);
		User user = userRepository.findOne(payload.getUserId());
		user.setUsername(payload.getUsername());
		user.setName(payload.getName());
		user.setEmail(payload.getEmail());
		user.setUserDepartmentId(payload.getDepartment().getDepartmentId());
		user.setUserStatusId(payload.getStatus());
		if(payload.getStatus().equalsIgnoreCase(BoUserStatus.ACTIVE.getValue())) {
			user.setFailedLoginCount(0);
		}
		user.setUpdatedBy(approval.getUpdatedBy());
		user.setUpdatedTime(approval.getUpdatedTime());
		userRepository.save(user);

		List<UserUsergroup> userUserGroupList = userUserGroupRepo.findAllByUserId(user.getId());
		userUserGroupList = userUserGroupList == null ? new ArrayList<UserUsergroup>() : userUserGroupList;
		List<Integer> existingUserGroupList = userUserGroupList.stream()
				.map((userUserGroup) -> userUserGroup.getUserGroupId()).collect(Collectors.toList());
		List<Integer> wfUserGroupList = payload.getGroup().stream().map((userUserGroup) -> userUserGroup.getGroupId())
				.collect(Collectors.toList());
		List<Integer> updateList = existingUserGroupList.stream().filter(s -> wfUserGroupList.contains(s))
				.collect(Collectors.toList());

		List<Integer> deleteList = existingUserGroupList.stream().filter(s -> !wfUserGroupList.contains(s))
				.collect(Collectors.toList());

		List<Integer> newList = wfUserGroupList.stream().filter(s -> !existingUserGroupList.contains(s))
				.collect(Collectors.toList());

		for (WFUserGroupPayload wfUserGroupPayload : payload.getGroup()) {

			if (newList.contains(wfUserGroupPayload.getGroupId())) {
				UserUsergroup userUserGroup = new UserUsergroup();
				userUserGroup.setUserId(user.getId());
				userUserGroup.setUserGroupId(wfUserGroupPayload.getGroupId());
				userUserGroup.setCreatedBy(approval.getUpdatedBy());
				userUserGroup.setCreatedTime(approval.getUpdatedTime());
				userUserGroup.setUpdatedBy(approval.getUpdatedBy());
				userUserGroup.setUpdatedTime(approval.getUpdatedTime());
				userUserGroup.setStatus(MaintenanceActionType.USER_STATUS_ID_ACTIVE.getValue());
				userUserGroupRepo.save(userUserGroup);
			}
		}

		for (UserUsergroup userUserGroup : userUserGroupList) {
			if (updateList.contains(userUserGroup.getUserGroupId())) {
				userUserGroup.setUpdatedBy(approval.getUpdatedBy());
				userUserGroup.setUpdatedTime(approval.getUpdatedTime());
				userUserGroup.setStatus(MaintenanceActionType.USER_STATUS_ID_ACTIVE.getValue());
				userUserGroupRepo.save(userUserGroup);
			} else if (deleteList.contains(userUserGroup.getUserGroupId())) {
				userUserGroup.setUpdatedBy(approval.getUpdatedBy());
				userUserGroup.setUpdatedTime(approval.getUpdatedTime());
				userUserGroup.setStatus(MaintenanceActionType.USER_STATUS_ID_DELETED.getValue());
				userUserGroupRepo.save(userUserGroup);
			}
		}
		return wfUserApprovalActionDetail;
	}

	private Approval findApproval(int approvalId, String actionType) {
		logger.debug("getting aproval id : {}", approvalId);
		Approval approval = approvalRepository.findOne(approvalId);
		if (approval == null) {
			throw new CommonException(CommonException.GENERIC_ERROR_CODE,
					"Cannot find Approval record for id : " + approvalId);
		}
		if (actionType.equals("Search")) {
			return approval;
		}
		if (!approval.getActionType().equalsIgnoreCase(actionType)) {
			throw new CommonException(CommonException.GENERIC_ERROR_CODE,
					"unmatched action type : " + approval.getActionType());
		}
		return approval;
	}

	private List<BoUmApprovalUser> findApprovalData(int approvalId, String actionType) {
		List<BoUmApprovalUser> pendingApprovalList = boUmApprovalUserRepo.findAllByApprovalId(approvalId);
		if (pendingApprovalList.isEmpty()) {
			throw new CommonException(CommonException.GENERIC_ERROR_CODE,
					"Cannot find Approval detail for id : " + approvalId);
		}
		if (actionType.equals(MaintenanceActionType.ADD.getValue())
				|| actionType.equals(MaintenanceActionType.DELETE.getValue())) {
			if (pendingApprovalList.size() != 1) {
				throw new CommonException(CommonException.GENERIC_ERROR_CODE,
						"Add Action type expect only one approval detail but found " + pendingApprovalList.size());
			}
		} else if (actionType.equals(MaintenanceActionType.EDIT.getValue())) {
			if (pendingApprovalList.size() != 2) {
				throw new CommonException(CommonException.GENERIC_ERROR_CODE,
						"expects before and after approval detail for id : " + approvalId);
			}
		}

		return pendingApprovalList;
	}

}
