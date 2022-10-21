package com.rhbgroup.dcpbo.user.usergroupupdate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rhbgroup.dcp.util.JsonUtil;
import com.rhbgroup.dcpbo.common.audit.AdditionalDataHolder;
import com.rhbgroup.dcpbo.common.exception.CommonException;
import com.rhbgroup.dcpbo.user.common.*;
import com.rhbgroup.dcpbo.user.common.model.bo.*;
import com.rhbgroup.dcpbo.user.create.*;
import com.rhbgroup.dcpbo.user.enums.MaintenanceActionType;
import com.rhbgroup.dcpbo.user.info.model.bo.ConfigFunction;
import com.rhbgroup.dcpbo.user.info.ConfigFunctionRepo;
import com.rhbgroup.dcpbo.user.info.model.bo.User;
import com.rhbgroup.dcpbo.user.usergroupdelete.UsergroupDeleteRequestVo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

    public class UsergroupUpdateFunctionServiceImpl implements UsergroupUpdateFunctionService {

    private static Logger logger = LogManager.getLogger(UserFunctionServiceImpl.class);

    @Autowired
    AdditionalDataHolder additionalDataHolder;

    @Autowired
    UserRepository userRepo;

    @Autowired
    ConfigFunctionRepo configFunctionRepo;

    @Autowired
    ConfigFunctionRepository configFunctionRepository;

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
    public BoData updateBoUsergroup(Integer creatorId, UsergroupUpdateRequestVo request, Integer updateUsergroupId) {
        logger.info("INSIDE updateBoUsergroup...usergroupId: ");
        logger.info("INSIDE updateBoUsergroup...usergroupId: " + updateUsergroupId);

        Timestamp dateNow = new Timestamp(new Date().getTime());

        User user= userRepo.findOne(creatorId);
        if (user == null){
            throw new CommonException(CommonException.GENERIC_ERROR_CODE, "Invalid creator id not found for " + creatorId);
        }

        Usergroup usergroup= usergroupRepo.findOne(updateUsergroupId);
        if (usergroup == null) {
            throw new CommonException("40003", "Usergroup ID not existed.");
        }

        UsergroupUpdateResponseVo response = new UsergroupUpdateResponseVo();
        logger.info("Data validation...");

        Usergroup updateUsergroup = usergroupRepo.findOne(updateUsergroupId);

        if (request.getFunctionId() == null) {
            throw new CommonException(CommonException.GENERIC_ERROR_CODE, "Function ID cannot be null.");
        }

        ConfigFunction configFunction = configFunctionRepository.findOne(request.getFunctionId());
        if (configFunction == null) {
            throw new CommonException(CommonException.TRANSACTION_NOT_FOUND_ERROR, "Function ID not found.");
        }

        if (request.getInput() == null) {
            throw new CommonException(CommonException.GENERIC_ERROR_CODE, "Input cannot be null.");
        }

        String accessType = request.getInput().getAccessType();

        logger.info("Performing approval process...");
        if (!configFunction.isApprovalRequired()) {
            logger.info("Approve not require...");
            updateUsergroup.setId(request.getGroupId());
            updateUsergroup.setGroupName(request.getInput().getGroupName() == null ? request.getCache().getGroupName()
                    : request.getInput().getGroupName());
            updateUsergroup.setUpdatedBy(user.getUsername());
            updateUsergroup.setUpdatedTime(dateNow);
            updateUsergroup = usergroupRepo.saveAndFlush(updateUsergroup);

            logger.info("Finish update user record...");

            Integer usergroupId = updateUsergroup.getId();
            List<Integer> functionIds = request.getInput().getFunctionId();
            if(functionIds != null && functionIds.size()>0){
                for(int funcId : functionIds){
                    ConfigFunction configFunctionN = configFunctionRepository.findOneById(funcId);
                    Integer moduleId = configFunction.getModule().getId();
                    String scopeId = getScopeId(accessType, configFunctionN);

                    UsergroupAccess usergroupAccess
                            = usergroupAccessRepo.findByUserGroupIdAndFunctionIdAndAccessType(request.getGroupId(), funcId, accessType);

                    if(usergroupAccess == null){
                        usergroupAccess = new UsergroupAccess();
                        usergroupAccess.setStatus("A");
                        usergroupAccess.setCreatedBy(user.getUsername());
                        usergroupAccess.setCreatedTime(dateNow);
                        usergroupAccess.setUpdatedBy(user.getUsername());
                        usergroupAccess.setUpdatedTime(dateNow);
                    }else{
                        if(usergroupAccess.getStatus().equals("D")){
                            usergroupAccess.setStatus("A");
                            usergroupAccess.setUpdatedBy(user.getUsername());
                            usergroupAccess.setUpdatedTime(dateNow);
                        }
                    }
                    usergroupAccess.setUserGroupId(usergroupId);
                    usergroupAccess.setFunctionId(funcId);
                    usergroupAccess.setScopeId(scopeId);
                    usergroupAccess.setAccessType(accessType);

                    usergroupAccessRepo.saveAndFlush(usergroupAccess);
                }

                List<UsergroupAccess> userGroupInDBList = usergroupAccessRepo.findByUserGroupIdAndAccessType(request.getGroupId(), accessType);
                List<Integer> funcIds = request.getInput().getFunctionId();

                if (userGroupInDBList != null && userGroupInDBList.size() > 0) {
                    for (UsergroupAccess userGroupInDB : userGroupInDBList) {
                        if(!funcIds.contains(userGroupInDB.getFunctionId())) {
                            userGroupInDB
                                    .setStatus(MaintenanceActionType.USER_USER_GROUP_STATUS_DELETED.getValue());
                            userGroupInDB.setUpdatedBy(user.getUsername());
                            userGroupInDB.setUpdatedTime(dateNow);
                            usergroupAccessRepo.saveAndFlush(userGroupInDB);
                        }
                    }
                }

            }

            logger.info("Finish Maintenance for user group record...");
            response.setApprovalId(0);
            response.setIsWritten(MaintenanceActionType.YES.getValue());
            populateAdditionalAuditData(request, updateUsergroup, response.getApprovalId());
        } else {
            logger.info("Approval require...");
            List<BoUserApproval> approvalList = boUserApprovalRepo.findAllByFunctionIdAndStatus(request.getFunctionId(),
                    MaintenanceActionType.STATUS_PENDING_APPROVAL.getValue());
            if (approvalList != null && approvalList.size() > 0) {
                for (BoUserApproval singleRecord : approvalList) {
                    List<BoUmApprovalUserGroup> umApprovalUserGroup = boUmApprovalUserGroupRepo.findAllByApprovalIdAndLockingId(
                            singleRecord.getId(), updateUsergroup.getGroupName());

                    if(!umApprovalUserGroup.isEmpty() && umApprovalUserGroup.size()>0){
                        throw new CommonException("40002", "Same approval request exist.");
                    }
                }
            }

            BoUserApproval boUserApproval = new BoUserApproval();
            boUserApproval.setFunctionId(request.getFunctionId());
            boUserApproval.setActionType(MaintenanceActionType.EDIT.getValue());
            boUserApproval.setStatus(MaintenanceActionType.STATUS_PENDING_APPROVAL.getValue());
            boUserApproval.setDescription(updateUsergroup.getGroupName());
            boUserApproval.setCreatorId(creatorId);
            boUserApproval.setCreatedBy(user.getUsername());
            boUserApproval.setCreatedTime(dateNow);
            boUserApproval.setUpdatedBy(user.getUsername());
            boUserApproval.setUpdatedTime(dateNow);
            boUserApproval = boUserApprovalRepo.saveAndFlush(boUserApproval);

            logger.info("Finish insert user approval record...");

            Integer approvalId = boUserApproval.getId();

            Boolean success = performInsertToBoUmApprovalUsergroup(request, approvalId, user, updateUsergroup);

            if (success) {
                logger.info("Finish insert um approval user group record...");
                response.setIsWritten(MaintenanceActionType.NO.getValue());
                response.setApprovalId(boUserApproval.getId());
            }
            populateAdditionalAuditData(request, updateUsergroup, boUserApproval.getId());
        }
        return response;
    }

        private void populateAdditionalAuditData(UsergroupUpdateRequestVo request, Usergroup updateUsergroup, Integer approvalId) {
            HashMap<String, Object> additionalDataMap = new HashMap<String, Object>();
            additionalDataMap.put("usergroupName", updateUsergroup.getGroupName());
            additionalDataMap.put("boRefNumber", approvalId);
            additionalDataMap.put("role", request.getInput().getAccessType());
            additionalDataMap.put("module", "User Management");

            additionalDataHolder.setMap(additionalDataMap);
            logger.debug("audit log data : {}", JsonUtil.objectToJson(additionalDataHolder.getMap()));
        }


        public Boolean performInsertToBoUmApprovalUsergroup(UsergroupUpdateRequestVo request, Integer approvalId, User creator,
                                                        Usergroup updateUsergroup) {

        // Create PAYLOAD
        UsergroupPayloadUpdateVo payloadStateA = updatePayloadGenerate(request,
                MaintenanceActionType.USER_APPROVAL_STATE_A.getValue(), updateUsergroup);

        UsergroupPayloadUpdateVo payloadStateB = updatePayloadGenerate(request,
                MaintenanceActionType.USER_APPROVAL_STATE_B.getValue(), updateUsergroup);

        Timestamp dateNow = new Timestamp(new Date().getTime());

        String payloadInStringStateA = convertObjectToJsonString(payloadStateA);
        BoUmApprovalUserGroup insertRecordA = new BoUmApprovalUserGroup();
        insertRecordA.setApprovalId(approvalId);
        insertRecordA.setPayload(payloadInStringStateA);
        insertRecordA.setLockingId(updateUsergroup.getGroupName());
        insertRecordA.setState(MaintenanceActionType.USER_APPROVAL_STATE_A.getValue());
        insertRecordA.setCreatedBy(creator.getUsername());
        insertRecordA.setCreatedTime(dateNow);
        insertRecordA.setUpdatedBy(creator.getUsername());
        insertRecordA.setUpdatedTime(dateNow);
        boUmApprovalUserGroupRepo.saveAndFlush(insertRecordA);

        String payloadInStringStateB = convertObjectToJsonString(payloadStateB);
        BoUmApprovalUserGroup insertRecordB = new BoUmApprovalUserGroup();
        insertRecordB.setApprovalId(approvalId);
        insertRecordB.setPayload(payloadInStringStateB);
        insertRecordB.setLockingId(updateUsergroup.getGroupName());
        insertRecordB.setState(MaintenanceActionType.USER_APPROVAL_STATE_B.getValue());
        insertRecordB.setCreatedBy(creator.getUsername());
        insertRecordB.setCreatedTime(dateNow);
        insertRecordB.setUpdatedBy(creator.getUsername());
        insertRecordB.setUpdatedTime(dateNow);
        boUmApprovalUserGroupRepo.saveAndFlush(insertRecordB);

        return true;
    }

    public UsergroupPayloadUpdateVo updatePayloadGenerate(UsergroupUpdateRequestVo request, String state, Usergroup updateUsergroup) {

        UsergroupPayloadUpdateVo payload = new UsergroupPayloadUpdateVo();
        payload.setGroupId(updateUsergroup.getId());

        if (state.equals(MaintenanceActionType.USER_APPROVAL_STATE_A.getValue())) {
            payload.setGroupName(request.getInput().getGroupName());
            payload.setAccessType(request.getInput().getAccessType());
            payload.setFunctionId( request.getInput().getFunctionId());
        } else {
            if (state.equals(MaintenanceActionType.USER_APPROVAL_STATE_B.getValue())) {
                payload.setGroupName(request.getCache().getGroupName());
                payload.setAccessType(request.getCache().getAccessType());
                payload.setFunctionId( request.getCache().getFunctionId());
            }
        }
        return payload;
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
