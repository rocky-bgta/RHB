package com.rhbgroup.dcpbo.user.workflow.usergroup;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import com.rhbgroup.dcpbo.user.common.*;
import com.rhbgroup.dcpbo.user.common.model.bo.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.rhbgroup.dcp.util.JsonUtil;
import com.rhbgroup.dcpbo.common.audit.AdditionalDataHolder;
import com.rhbgroup.dcpbo.common.exception.CommonException;
import com.rhbgroup.dcpbo.user.enums.MaintenanceActionType;
import com.rhbgroup.dcpbo.user.info.model.bo.ConfigFunction;
import com.rhbgroup.dcpbo.user.info.model.bo.User;

public class WFUserGroupServiceImpl implements WFUserGroupService {

    private static Logger logger = LogManager.getLogger(WFUserGroupService.class);

    @Autowired
    ApprovalRepository approvalRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    UsergroupAccessRepository usergroupAccessRepository;

    @Autowired
    ConfigFunctionRepository configFunctionRepository;

    @Autowired
    BoUmApprovalUserGroupRepo boUmApprovalUserGroupRepo;

    @Autowired
    UserGroupRepository userGroupRepo;

    @Autowired
    AdditionalDataHolder additionalDataHolder;

    @Autowired
    UserUsergroupRepository userUsergroupRepository;

    public static final String APPROVAL_STATUS_APPROVED = "A";
    public static final String APPROVAL_STATUS_REJECTED = "R";
    public static final String REQUEST_APPROVED = "APPROVED";
    public static final String REQUEST_REJECTED = "REJECTED";

    public BoData getWorkflowApprovalDetail(Integer userId, int approvalId) {

        WFUserGroupApprovalDetail wfUserGroupApprovalDetail = new WFUserGroupApprovalDetail();
        wfUserGroupApprovalDetail.setApprovalId(approvalId);

        Approval approval = findApproval(approvalId, "Search");

        wfUserGroupApprovalDetail.setActionType(approval.getActionType());

        String sTimestamp = approval.getUpdatedTime().toString();
        wfUserGroupApprovalDetail.setUpdatedTime(sTimestamp);

        wfUserGroupApprovalDetail.setReason(approval.getReason());

        String isCreator = MaintenanceActionType.NO.getValue();
        if (userId == approval.getCreatorId()) {
            isCreator = MaintenanceActionType.YES.getValue();
        }
        wfUserGroupApprovalDetail.setIsCreator(isCreator);
        String creatorName = userRepository.findNameById(approval.getCreatorId());
        logger.debug("creatorId, creatorName : {}, {}", approval.getCreatorId(), creatorName);
        wfUserGroupApprovalDetail.setCreatorName(creatorName);
        wfUserGroupApprovalDetail.setUpdatedBy(approval.getUpdatedBy());

        if (approval.getStatus().equals(APPROVAL_STATUS_APPROVED))
            wfUserGroupApprovalDetail.setApprovalStatus(REQUEST_APPROVED);

        else if (approval.getStatus().equals(APPROVAL_STATUS_REJECTED))
            wfUserGroupApprovalDetail.setApprovalStatus(REQUEST_REJECTED);


        wfUserGroupApprovalDetail.setCreatedTime(approval.getCreatedTime().toString());
        List<BoUmApprovalUserGroup> userGroupApprovalList = findApprovalData(approval.getId(),
                approval.getActionType());
        if (approval.getActionType().equalsIgnoreCase(MaintenanceActionType.EDIT.getValue())) {

            sortBeforeAfterList(userGroupApprovalList);

            WFUserGroupPayload payloadBefore = constructWorkflowPayload(userGroupApprovalList.get(0).getPayload());
            WFUserGroupPayload payloadAfter = constructWorkflowPayload(userGroupApprovalList.get(1).getPayload());

            wfUserGroupApprovalDetail.setGroupName(
                    new WFUserGroupApprovalDetailValue(payloadBefore.getGroupName(), payloadAfter.getGroupName()));

            wfUserGroupApprovalDetail.setAccessType(
                    (new WFUserGroupApprovalDetailValue(payloadBefore.getAccessType(), payloadAfter.getAccessType())));

            List<String> functionListBefore = new ArrayList<String>();
            List<String> functionListAfter = new ArrayList<String>();

            for (Integer wfUserGroupFunctionPayload : payloadBefore.getFunctionId()) {
                String functionName = configFunctionRepository.findFunctionNameById(wfUserGroupFunctionPayload);
                functionListBefore.add(functionName);
            }

            for (Integer wfUserGroupFunctionPayload : payloadAfter.getFunctionId()) {
                String functionName = configFunctionRepository.findFunctionNameById(wfUserGroupFunctionPayload);
                functionListAfter.add(functionName);
            }
            wfUserGroupApprovalDetail.setFunction(new WFUserGroupDetailFunctionValue(
                    new WFFunctionValue(functionListBefore), new WFFunctionValue(functionListAfter)));

        } else if (approval.getActionType().equalsIgnoreCase(MaintenanceActionType.ADD.getValue())) {

            logger.debug("Add payload : {}", userGroupApprovalList.get(0).getPayload());
            WFUserGroupPayload payloadAfter = constructWorkflowPayload(userGroupApprovalList.get(0).getPayload());

            wfUserGroupApprovalDetail.setGroupName(new WFUserGroupApprovalDetailValue("", payloadAfter.getGroupName()));

            wfUserGroupApprovalDetail
                    .setAccessType(new WFUserGroupApprovalDetailValue("", payloadAfter.getAccessType()));

            List<String> functionListBefore = new ArrayList<String>();
            List<String> functionListAfter = new ArrayList<String>();
            for (WFUserGroupFunctionPayload wfUserGroupFunctionPayload : payloadAfter.getFunction()) {
                functionListAfter.add(wfUserGroupFunctionPayload.getFunctionName());
            }
            wfUserGroupApprovalDetail.setFunction(new WFUserGroupDetailFunctionValue(
                    new WFFunctionValue(functionListBefore), new WFFunctionValue(functionListAfter)));

        } else if (approval.getActionType().equalsIgnoreCase(MaintenanceActionType.DELETE.getValue())) {

            WFUserGroupPayload payload = constructWorkflowPayload(userGroupApprovalList.get(0).getPayload());

            Integer payloadGroupId = payload.getGroupId();
            String payloadGroupName = payload.getGroupName();
            String accessType = payload.getAccessType();
            logger.debug("Delete Action Type payload [payloadGroupId, payloadGroupName, accessType : {},{},{}]",
                    payloadGroupId, payloadGroupName, accessType);

            wfUserGroupApprovalDetail.setGroupName(new WFUserGroupApprovalDetailValue(payloadGroupName, ""));
            wfUserGroupApprovalDetail.setAccessType(new WFUserGroupApprovalDetailValue(accessType, ""));

            List<String> functionNameList = new ArrayList<String>();

            List<UsergroupAccess> userGroupAccessList = findUserGroupAccessList(payloadGroupId);
            for (UsergroupAccess userGroupAccess : userGroupAccessList) {

                ConfigFunction configFunction = configFunctionRepository.findOneById(userGroupAccess.getFunctionId());
                functionNameList.add(configFunction.getFunctionName());
            }

            wfUserGroupApprovalDetail.setFunction(new WFUserGroupDetailFunctionValue(
                    new WFFunctionValue(functionNameList), new WFFunctionValue(new ArrayList<String>())));
        }
        return wfUserGroupApprovalDetail;
    }

    @Override
    public BoData approveCreate(Integer userId, WFUserGroupApprovalActionDetail wfUserGroupApprovalActionDetail) {

        User requestUser = getRequestUser(userId);

        logger.debug("getting aproval id : {}", wfUserGroupApprovalActionDetail.getApprovalId());
        Approval approval = findApproval(wfUserGroupApprovalActionDetail.getApprovalId(),
                MaintenanceActionType.ADD.getValue());
        List<BoUmApprovalUserGroup> pendingApprovalList = findApprovalData(approval.getId(), approval.getActionType());
        BoUmApprovalUserGroup boUmApprovalUserGroup = pendingApprovalList.get(0);
        logger.debug("Add payload : {}", boUmApprovalUserGroup.getPayload());
        WFUserGroupPayload wfUserGroupPayload = constructWorkflowPayload(boUmApprovalUserGroup.getPayload());
        populateAdditionalAuditData(wfUserGroupPayload, wfUserGroupApprovalActionDetail);
        String groupName = wfUserGroupPayload.getGroupName();
        String accessType = wfUserGroupPayload.getAccessType();
        List<Usergroup> userGroupList = userGroupRepo.findByGroupName(groupName);
        if (!userGroupList.isEmpty()) {
            logger.error("found user group with groupname : {}", groupName);
            throw new CommonException("40003", "This record already exists.");
        }
        approval.setStatus(MaintenanceActionType.STATUS_APPROVED.getValue());
        approval.setReason(wfUserGroupApprovalActionDetail.getReason());
        approval.setUpdatedBy(requestUser.getUsername());
        approval.setUpdatedTime(new Timestamp(new Date().getTime()));
        approvalRepository.save(approval);

        Usergroup userGroup = new Usergroup();
        userGroup.setGroupName(wfUserGroupPayload.getGroupName());
        userGroup.setGroupStatus(MaintenanceActionType.USER_USER_GROUP_STATUS_ACTIVE.getValue());
        userGroup.setCreatedBy(approval.getUpdatedBy());
        userGroup.setCreatedTime(approval.getUpdatedTime());
        userGroup.setUpdatedBy(approval.getUpdatedBy());
        userGroup.setUpdatedTime(approval.getUpdatedTime());
        userGroupRepo.saveAndFlush(userGroup);
        for (WFUserGroupFunctionPayload wfUserGroupFunctionPayload : wfUserGroupPayload.getFunction()) {
            Integer functionId = wfUserGroupFunctionPayload.getFunctionId();
            ConfigFunction configFunction = configFunctionRepository.findOneById(functionId);
            String scopeId = getScopeId(accessType, configFunction);

            logger.debug("functionId, scopeId, groupId : {}, {}, {}", functionId, scopeId,
                    userGroup.getId());

            UsergroupAccess userGroupAccess = new UsergroupAccess();
            userGroupAccess.setUserGroupId(userGroup.getId());
            userGroupAccess.setAccessType(accessType);
            userGroupAccess.setFunctionId(functionId);
            userGroupAccess.setScopeId(scopeId);
            userGroupAccess.setStatus(MaintenanceActionType.USER_USER_GROUP_STATUS_ACTIVE.getValue());
            userGroupAccess.setCreatedBy(approval.getUpdatedBy());
            userGroupAccess.setCreatedTime(approval.getUpdatedTime());
            userGroupAccess.setUpdatedBy(approval.getUpdatedBy());
            userGroupAccess.setUpdatedTime(approval.getUpdatedTime());
            usergroupAccessRepository.save(userGroupAccess);
        }

        return wfUserGroupApprovalActionDetail;
    }

    @Override
    public BoData approveUpdate(Integer userId, WFUserGroupApprovalActionDetail wfUserGroupApprovalActionDetail) {

        User requestUser = getRequestUser(userId);

        logger.debug("getting aproval id : {}", wfUserGroupApprovalActionDetail.getApprovalId());
        Approval approval = findApproval(wfUserGroupApprovalActionDetail.getApprovalId(),
                MaintenanceActionType.EDIT.getValue());
        List<BoUmApprovalUserGroup> pendingApprovalList = findApprovalData(
                wfUserGroupApprovalActionDetail.getApprovalId(), MaintenanceActionType.EDIT.getValue());
        sortBeforeAfterList(pendingApprovalList);
        BoUmApprovalUserGroup boUmApprovalUserGroup = pendingApprovalList.get(1);
        logger.debug("Update payload : {}", boUmApprovalUserGroup.getPayload());
        WFUserGroupPayload wfUserGroupPayload = constructWorkflowPayload(boUmApprovalUserGroup.getPayload());
        populateAdditionalAuditData(wfUserGroupPayload, wfUserGroupApprovalActionDetail);
        String accessType = wfUserGroupPayload.getAccessType();

        approval.setStatus(MaintenanceActionType.STATUS_APPROVED.getValue());
        approval.setReason(wfUserGroupApprovalActionDetail.getReason());
        approval.setUpdatedBy(requestUser.getUsername());
        approval.setUpdatedTime(new Timestamp(new Date().getTime()));
        approvalRepository.save(approval);

        Usergroup userGroup = userGroupRepo.findOneById(wfUserGroupPayload.getGroupId());
        userGroup.setGroupName(wfUserGroupPayload.getGroupName());
        userGroup.setGroupStatus(MaintenanceActionType.USER_USER_GROUP_STATUS_ACTIVE.getValue());
        userGroup.setUpdatedBy(approval.getUpdatedBy());
        userGroup.setUpdatedTime(approval.getUpdatedTime());
        userGroupRepo.save(userGroup);

        List<UsergroupAccess> userGroupAccessList = findUserGroupAccessList(wfUserGroupPayload.getGroupId());
        List<String> existingFunctionList = userGroupAccessList.stream().map(
                        (userGroupAccess) -> String.valueOf(userGroupAccess.getFunctionId()) + userGroupAccess.getAccessType())
                .collect(Collectors.toList());

        List<String> wfFunctionList = wfUserGroupPayload.getFunctionId().stream()
                .map((i) -> String.valueOf(i) + wfUserGroupPayload.getAccessType())
                .collect(Collectors.toList());
        List<String> updateList = existingFunctionList.stream().filter(s -> wfFunctionList.contains(s))
                .collect(Collectors.toList());
        List<String> deleteList = existingFunctionList.stream().filter(s -> !wfFunctionList.contains(s))
                .collect(Collectors.toList());
        List<String> newList = wfFunctionList.stream().filter(s -> !existingFunctionList.contains(s))
                .collect(Collectors.toList());

        logger.debug("newList function id : {}", newList);
        logger.debug("updateList function id : {}", updateList);
        logger.debug("deleteList function id : {}", deleteList);

        for (Integer wfUserGroupFunctionPayload : wfUserGroupPayload.getFunctionId()) {

            if (newList.contains(
                    String.valueOf(wfUserGroupFunctionPayload) + wfUserGroupPayload.getAccessType())) {
                Integer functionId = wfUserGroupFunctionPayload;
                ConfigFunction configFunction = configFunctionRepository.findOneById(functionId);
                String scopeId = getScopeId(accessType, configFunction);

                logger.debug("add new functionId, accessType : {}, {}", functionId, wfUserGroupPayload.getAccessType());

                UsergroupAccess userGroupAccess = new UsergroupAccess();
                userGroupAccess.setUserGroupId(userGroup.getId());
                userGroupAccess.setAccessType(accessType);
                userGroupAccess.setFunctionId(functionId);
                userGroupAccess.setScopeId(scopeId);

                userGroupAccess.setStatus(MaintenanceActionType.USER_USER_GROUP_STATUS_ACTIVE.getValue());
                userGroupAccess.setCreatedBy(approval.getCreatedBy());
                userGroupAccess.setCreatedTime(approval.getUpdatedTime());
                userGroupAccess.setUpdatedBy(approval.getCreatedBy());
                userGroupAccess.setUpdatedTime(approval.getUpdatedTime());
                usergroupAccessRepository.save(userGroupAccess);
            }
        }

        for (UsergroupAccess userGroupAccess : userGroupAccessList) {
            if (updateList
                    .contains(String.valueOf(userGroupAccess.getFunctionId()) + userGroupAccess.getAccessType())) {
                logger.debug("update functionId, accessType : {}, {}", userGroupAccess.getFunctionId(),
                        userGroupAccess.getAccessType());
                userGroupAccess.setUpdatedBy(approval.getCreatedBy());
                userGroupAccess.setUpdatedTime(approval.getUpdatedTime());
                userGroupAccess.setStatus(MaintenanceActionType.USER_USER_GROUP_STATUS_ACTIVE.getValue());
                usergroupAccessRepository.save(userGroupAccess);
            } else if (deleteList
                    .contains(String.valueOf(userGroupAccess.getFunctionId()) + userGroupAccess.getAccessType())) {

                logger.debug("deleting functionId, accessType : {}, {}",
                        userGroupAccess.getFunctionId(), userGroupAccess.getAccessType());

                userGroupAccess.setUpdatedBy(approval.getCreatedBy());
                userGroupAccess.setUpdatedTime(approval.getUpdatedTime());
                userGroupAccess.setStatus(MaintenanceActionType.USER_USER_GROUP_STATUS_DELETED.getValue());
                usergroupAccessRepository.save(userGroupAccess);
            }
        }
        return wfUserGroupApprovalActionDetail;
    }

    @Override
    public BoData approveDelete(Integer userId, WFUserGroupApprovalActionDetail wfUserGroupApprovalActionDetail) {

        User requestUser = getRequestUser(userId);

        logger.debug("getting aproval id : {}", wfUserGroupApprovalActionDetail.getApprovalId());
        Approval approval = findApproval(wfUserGroupApprovalActionDetail.getApprovalId(),
                MaintenanceActionType.DELETE.getValue());
        List<BoUmApprovalUserGroup> pendingApprovalList = findApprovalData(
                wfUserGroupApprovalActionDetail.getApprovalId(), MaintenanceActionType.DELETE.getValue());

        BoUmApprovalUserGroup boUmApprovalUserGroup = pendingApprovalList.get(0);
        logger.debug("Delete payload : {}", boUmApprovalUserGroup.getPayload());
        WFUserGroupPayload wfUserGroupPayload = constructWorkflowPayload(boUmApprovalUserGroup.getPayload());
        populateAdditionalAuditData(wfUserGroupPayload, wfUserGroupApprovalActionDetail);
        approval.setStatus(MaintenanceActionType.STATUS_APPROVED.getValue());
        approval.setReason(wfUserGroupApprovalActionDetail.getReason());
        approval.setUpdatedBy(requestUser.getUsername());
        approval.setUpdatedTime(new Timestamp(new Date().getTime()));
        approvalRepository.save(approval);

        Usergroup userGroup = userGroupRepo.findOneById(wfUserGroupPayload.getGroupId());
        userGroup.setGroupStatus(MaintenanceActionType.USER_USER_GROUP_STATUS_DELETED.getValue());
        userGroup.setUpdatedBy(approval.getUpdatedBy());
        userGroup.setUpdatedTime(approval.getUpdatedTime());
        userGroupRepo.save(userGroup);

        List<UsergroupAccess> userGroupAccessList = findUserGroupAccessList(wfUserGroupPayload.getGroupId());
        for (UsergroupAccess userGroupAccess : userGroupAccessList) {
            userGroupAccess.setStatus(MaintenanceActionType.USER_USER_GROUP_STATUS_DELETED.getValue());
            userGroupAccess.setUpdatedBy(approval.getUpdatedBy());
            userGroupAccess.setUpdatedTime(approval.getUpdatedTime());
            usergroupAccessRepository.save(userGroupAccess);
        }

        List<UserUsergroup> userUsergroupList = findByUserGroupId(wfUserGroupPayload.getGroupId());
        for (UserUsergroup userUsergroup : userUsergroupList) {
            userUsergroup.setStatus(MaintenanceActionType.USER_USER_GROUP_STATUS_DELETED.getValue());
            userUsergroup.setUpdatedBy(approval.getUpdatedBy());
            userUsergroup.setUpdatedTime(approval.getUpdatedTime());
            userUsergroupRepository.save(userUsergroup);
        }

        return wfUserGroupApprovalActionDetail;
    }

    private User getRequestUser(Integer userId) {
        User requestUser = userRepository.findOne(userId);
        if (requestUser == null) {
            throw new CommonException(CommonException.GENERIC_ERROR_CODE, "Cannot find request user : " + userId);
        }
        return requestUser;
    }

    private void populateAdditionalAuditData(WFUserGroupPayload wfUserGroupPayload, WFUserGroupApprovalActionDetail wfUserGroupApprovalActionDetail) {
        HashMap<String, Object> additionalDataMap = new HashMap<String, Object>();
        // creation payload doesn't contains group id
        if (wfUserGroupPayload.getGroupId() != null) {
            additionalDataMap.put("groupId", wfUserGroupPayload.getGroupId());
        }
        additionalDataMap.put("groupName", wfUserGroupPayload.getGroupName());
        additionalDataMap.put("function", wfUserGroupPayload.getFunction());
        additionalDataMap.put("accessType", wfUserGroupPayload.getAccessType());

        additionalDataMap.put("boRefNumber", wfUserGroupApprovalActionDetail.getApprovalId());
        additionalDataMap.put("module", "User Management");
        additionalDataMap.put("reason", wfUserGroupApprovalActionDetail.getReason());

        additionalDataHolder.setMap(additionalDataMap);
        logger.debug("audit log data : {}", JsonUtil.objectToJson(additionalDataHolder.getMap()));
    }

    private void sortBeforeAfterList(List<BoUmApprovalUserGroup> pendingApprovalList) {
        Collections.sort(pendingApprovalList, new Comparator<BoUmApprovalUserGroup>() {
            public int compare(BoUmApprovalUserGroup contentVo1, BoUmApprovalUserGroup contentVo2) {
                return contentVo2.getState().compareTo(contentVo1.getState());
            }
        });
    }

    private WFUserGroupPayload constructWorkflowPayload(String payload) {
        return JsonUtil.jsonToObject(payload, WFUserGroupPayload.class);
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

    private List<UsergroupAccess> findUserGroupAccessList(Integer groupId) {
        List<UsergroupAccess> userGroupAccessList = usergroupAccessRepository.findByUserGroupId(groupId);
//		userGroupAccessList = userGroupAccessList == null ? new ArrayList<UsergroupAccess>() : userGroupAccessList;
        return userGroupAccessList;
    }

    private List<UserUsergroup> findByUserGroupId(Integer groupId) {
        List<UserUsergroup> userUsergroupList = userUsergroupRepository.findByUserGroupId(groupId);
        return userUsergroupList;
    }

    private List<BoUmApprovalUserGroup> findApprovalData(int approvalId, String actionType) {

        List<BoUmApprovalUserGroup> userGroupApprovalList = boUmApprovalUserGroupRepo.findAllByApprovalId(approvalId);
        if (userGroupApprovalList.isEmpty()) {
            throw new CommonException(CommonException.GENERIC_ERROR_CODE,
                    "Cannot find Approval detail for id : " + approvalId);
        }
        if (actionType.equals(MaintenanceActionType.ADD.getValue())
                || actionType.equals(MaintenanceActionType.DELETE.getValue())) {
            if (userGroupApprovalList.size() != 1) {
                throw new CommonException(CommonException.GENERIC_ERROR_CODE,
                        "Add Action type expect only one approval detail but found " + userGroupApprovalList.size());
            }
        } else if (actionType.equals(MaintenanceActionType.EDIT.getValue())) {
            if (userGroupApprovalList.size() != 2) {
                throw new CommonException(CommonException.GENERIC_ERROR_CODE,
                        "expects before and after approval detail for id : " + approvalId);
            }
        }

        return userGroupApprovalList;
    }

    private String getScopeId(String accessType, ConfigFunction configFunction) {
        String scopeId = null;
        if (accessType.equals(MaintenanceActionType.ACCESS_TYPE_MAKER.getValue())) {
            scopeId = configFunction.getMakerScope();
        } else if (accessType.equals(MaintenanceActionType.ACCESS_TYPE_CHECKER.getValue())) {
            scopeId = configFunction.getCheckerScope();
        } else if (accessType.equals(MaintenanceActionType.ACCESS_TYPE_INQUIRER.getValue())) {
            scopeId = configFunction.getInquirerScope();
        }
        return scopeId;
    }
}
