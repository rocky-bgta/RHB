
package com.rhbgroup.dcpbo.system.downtime.whitelist.service.impl;

import com.rhbgroup.dcp.util.JsonUtil;
import com.rhbgroup.dcpbo.common.exception.CommonException;
import com.rhbgroup.dcpbo.system.common.BoData;
import com.rhbgroup.dcpbo.system.downtime.repository.ApprovalRepository;
import com.rhbgroup.dcpbo.system.downtime.repository.UserRepository;
import com.rhbgroup.dcpbo.system.downtime.service.impl.WorkflowDowntimeServiceImpl;
import com.rhbgroup.dcpbo.system.downtime.whitelist.dto.ApprovalDowntimeAdhocWhitelistPayload;
import com.rhbgroup.dcpbo.system.downtime.whitelist.repository.BoSmApprovalDowntimeWhitelistRepository;
import com.rhbgroup.dcpbo.system.downtime.whitelist.service.WorkflowDowntimeWhitelistService;
import com.rhbgroup.dcpbo.system.downtime.whitelist.vo.ApprovalWhitelistResponse;
import com.rhbgroup.dcpbo.system.model.Approval;
import com.rhbgroup.dcpbo.system.model.BoSmApprovalDowntimeWhitelist;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

/**
 *
 * @author faizal.musa
 */
@Service
@Deprecated
public class WorkflowDowntimeWhitelistServiceImpl implements WorkflowDowntimeWhitelistService{

    private static Logger logger = LogManager.getLogger(WorkflowDowntimeServiceImpl.class);
    
    private final UserRepository userRepository;
    
    private final ApprovalRepository approvalRepository;
    
    private final BoSmApprovalDowntimeWhitelistRepository smApprovalRepository; 
    
    @Autowired
    public WorkflowDowntimeWhitelistServiceImpl(UserRepository userRepository, ApprovalRepository approvalRepository, 
    BoSmApprovalDowntimeWhitelistRepository smApprovalRepository){
        
        this.userRepository = userRepository;
        this.approvalRepository = approvalRepository;
        this.smApprovalRepository = smApprovalRepository;
    }
    
    @Override
    public ResponseEntity<BoData> getWhitelistApproval(Integer approvalId, Integer boUserId) {
        
        String boUsername = retrieveBOUserUsername(boUserId);
        
        //actionType, createdBy, createdTime, reason
        Approval whitelistApproval = retrieveApprovalValidate(approvalId);
        Integer creatorId = whitelistApproval.getCreatorId();
        String creatorName = retrieveBOUserUsername(creatorId);
        
        //state payload
        BoSmApprovalDowntimeWhitelist smApprovalWhitelist = retrieveSmApprovalWhitelist(whitelistApproval.getId());
        
        ApprovalDowntimeAdhocWhitelistPayload payload = JsonUtil.jsonToObject(smApprovalWhitelist.getPayload(), ApprovalDowntimeAdhocWhitelistPayload.class);

        ApprovalWhitelistResponse awr = ApprovalWhitelistResponse.of(whitelistApproval.getId())
                .userId(payload.getUserId())
                .name(payload.getName())
                .mobileNo(payload.getMobileNo())
                .username(payload.getUsername())
                .idNo(payload.getIdNo())
                .idType(payload.getIdType())
                .type(payload.getType())
                .cisNo(payload.getCisNo())
                .build();
        
        return ResponseEntity.ok(awr);
    }
    
    BoSmApprovalDowntimeWhitelist retrieveSmApprovalWhitelist(Integer approvalId){
        
        BoSmApprovalDowntimeWhitelist smApprovalWhitelist = smApprovalRepository.findOneByApprovalId(approvalId);
        
        if(smApprovalWhitelist == null){
            String errMsg = "System management approval is not valid for approvalId: " + approvalId;
            logger.error(errMsg);
            throw new CommonException(CommonException.GENERIC_ERROR_CODE, errMsg);
        }

        return smApprovalWhitelist;
    }
    
    Approval retrieveApprovalValidate(Integer approvalId){
        
        Approval approval = approvalRepository.findOne(approvalId);

        if (approval == null) {
            String errMsg = "Approval is not valid for approvalId: " + approvalId;
            logger.error(errMsg);
            throw new CommonException(CommonException.GENERIC_ERROR_CODE, errMsg);
        }

        return approval;
    }
        
    String retrieveBOUserUsername(Integer userId) {
            
        String username = userRepository.findNameById(userId);
        if (username == null){
                String errMsg = "User not valid for id: " + userId;
                logger.error(errMsg);
                throw new CommonException(CommonException.GENERIC_ERROR_CODE, errMsg);
        }

        return username;
    }
}
