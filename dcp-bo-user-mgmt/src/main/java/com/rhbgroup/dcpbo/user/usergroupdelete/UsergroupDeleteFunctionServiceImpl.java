package com.rhbgroup.dcpbo.user.usergroupdelete;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rhbgroup.dcp.util.JsonUtil;
import com.rhbgroup.dcpbo.common.audit.AdditionalDataHolder;
import com.rhbgroup.dcpbo.common.exception.CommonException;
import com.rhbgroup.dcpbo.user.common.*;
import com.rhbgroup.dcpbo.user.common.model.bo.*;
import com.rhbgroup.dcpbo.user.enums.MaintenanceActionType;
import com.rhbgroup.dcpbo.user.info.model.bo.ConfigFunction;
import com.rhbgroup.dcpbo.user.info.ConfigFunctionRepo;
import com.rhbgroup.dcpbo.user.info.model.bo.User;
import com.rhbgroup.dcpbo.user.workflow.usergroup.WFUserGroupApprovalActionDetail;
import com.rhbgroup.dcpbo.user.workflow.usergroup.WFUserGroupPayload;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class UsergroupDeleteFunctionServiceImpl implements UsergroupDeleteFunctionService {

    private static Logger logger = LogManager.getLogger(UsergroupDeleteFunctionServiceImpl.class);

    @Autowired
    AdditionalDataHolder additionalDataHolder;

    @Autowired
    UserRepository userRepo;

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

    @Autowired
    UsergroupAccessRepository usergroupAccessRepo;

    @Autowired
    BoUmApprovalUserGroupRepo boUmApprovalUserGroupRepo;

    @Override
    public BoData deleteBoUsergroup(Integer creatorId, UsergroupDeleteRequestVo request, Integer deleteUsergroupId) {

        UsergroupDeleteResponseVo response = new UsergroupDeleteResponseVo();
        Timestamp dateNow = new Timestamp(new Date().getTime());

        logger.info("INSIDE deleteBoUsergroup...usergroupId: " + deleteUsergroupId);
        logger.info("Data validation...");

        User creator = userRepo.findOne(creatorId);
        if (creator == null) {
            throw new CommonException("40003", "Creator ID not existed.");
        }

        Usergroup deleteUsergroup= usergroupRepo.findOne(deleteUsergroupId);
        if (deleteUsergroup == null) {
            throw new CommonException("40003", "Usergroup ID not existed.");
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
            deleteUsergroup.setGroupStatus(MaintenanceActionType.USER_STATUS_ID_DELETED.getValue());
            deleteUsergroup.setUpdatedBy(creator.getUsername());
            deleteUsergroup.setUpdatedTime(dateNow);
            usergroupRepo.saveAndFlush(deleteUsergroup);

            List<UsergroupAccess> uugroupList = usergroupAccessRepo.findByUserGroupId(deleteUsergroupId);
            if (uugroupList != null && uugroupList.size() > 0) {
                for (UsergroupAccess uugroup : uugroupList) {
                    uugroup.setStatus(MaintenanceActionType.USER_STATUS_ID_DELETED.getValue());
                    uugroup.setUpdatedBy(creator.getUsername());
                    uugroup.setUpdatedTime(dateNow);
                    usergroupAccessRepo.saveAndFlush(uugroup);
                }
            }

            List<UserUsergroup> userUsergroupList = userUserGroupRepo.findByUserGroupId(deleteUsergroupId);
            if (userUsergroupList != null && userUsergroupList.size() > 0) {
                for (UserUsergroup userUsergroup : userUsergroupList) {
                    userUsergroup.setStatus(MaintenanceActionType.USER_STATUS_ID_DELETED.getValue());
                    userUsergroup.setUpdatedBy(creator.getUsername());
                    userUsergroup.setUpdatedTime(dateNow);
                    userUserGroupRepo.saveAndFlush(userUsergroup);
                }
            }

            response.setApprovalId(0);
            response.setIsWritten(MaintenanceActionType.YES.getValue());
            populateAdditionalAuditData(request,deleteUsergroup, response.getApprovalId());
        } else {

            logger.info("Approval require...");
            List<BoUserApproval> approvalList = boUserApprovalRepo.findAllByFunctionIdAndStatus(request.getFunctionId(),
                    MaintenanceActionType.STATUS_PENDING_APPROVAL.getValue());
            if (approvalList != null && approvalList.size() > 0) {
                for (BoUserApproval singleRecord : approvalList) {
                    List<BoUmApprovalUserGroup> umApprovalUsergroup = boUmApprovalUserGroupRepo
                            .findAllByApprovalIdAndLockingId(singleRecord.getId(), deleteUsergroup.getGroupName());
                    if (umApprovalUsergroup != null && umApprovalUsergroup.size() > 0) {
                        throw new CommonException("40002", "Same approval request exist.");
                    }
                }
            }

            logger.info("Perform insert user approval record...");
            BoUserApproval boUserApproval = new BoUserApproval();
            boUserApproval.setFunctionId(request.getFunctionId());
            boUserApproval.setActionType(MaintenanceActionType.DELETE.getValue());
            boUserApproval.setStatus(MaintenanceActionType.STATUS_PENDING_APPROVAL.getValue());
            boUserApproval.setDescription(deleteUsergroup.getGroupName());
            boUserApproval.setCreatorId(creatorId);
            boUserApproval.setCreatedBy(creator.getUsername());
            boUserApproval.setCreatedTime(dateNow);
            boUserApproval.setUpdatedBy(creator.getUsername());
            boUserApproval.setUpdatedTime(dateNow);
            boUserApproval = boUserApprovalRepo.saveAndFlush(boUserApproval);

            logger.info("Finish insert user approval record...");

            UsergroupPayloadDeleteVo payloadDelete = new UsergroupPayloadDeleteVo();
            payloadDelete.setGroupId(deleteUsergroup.getId());
            payloadDelete.setGroupname(deleteUsergroup.getGroupName());
            payloadDelete.setAccessType(request.getAccessType());

            String payloadInString = convertObjectToJsonString(payloadDelete);

            logger.info("Perform insert UM user approval record...");
            BoUmApprovalUserGroup insertRecord = new BoUmApprovalUserGroup();
            insertRecord.setApprovalId(boUserApproval.getId());
            insertRecord.setPayload(payloadInString);
            insertRecord.setLockingId(deleteUsergroup.getGroupName());
            insertRecord.setState(MaintenanceActionType.USER_APPROVAL_STATE_B.getValue());
            insertRecord.setCreatedBy(creator.getUsername());
            insertRecord.setCreatedTime(dateNow);
            insertRecord.setUpdatedBy(creator.getUsername());
            insertRecord.setUpdatedTime(dateNow);
            boUmApprovalUserGroupRepo.saveAndFlush(insertRecord);

            logger.info("Finish insert UM approval usergroup record...");
            response.setIsWritten(MaintenanceActionType.NO.getValue());
            response.setApprovalId(boUserApproval.getId());
            populateAdditionalAuditData(request, deleteUsergroup, boUserApproval.getId());
        }

        return response;
    }

    private void populateAdditionalAuditData(UsergroupDeleteRequestVo request, Usergroup deleteUsergroup, Integer approvalId) {
        HashMap<String, Object> additionalDataMap = new HashMap<String, Object>();
        additionalDataMap.put("boRefNumber", approvalId);
        additionalDataMap.put("usergroupName", deleteUsergroup.getGroupName());
        additionalDataMap.put("role", request.getAccessType());
        additionalDataMap.put("module", "User Management");

        additionalDataHolder.setMap(additionalDataMap);
        logger.debug("audit log data : {}", JsonUtil.objectToJson(additionalDataHolder.getMap()));
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

