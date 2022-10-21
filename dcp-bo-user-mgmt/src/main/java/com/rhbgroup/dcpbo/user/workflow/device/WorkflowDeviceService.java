package com.rhbgroup.dcpbo.user.workflow.device;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rhbgroup.dcpbo.common.audit.AdditionalDataHolder;
import com.rhbgroup.dcpbo.common.exception.CommonException;
import com.rhbgroup.dcpbo.user.common.BoApprovalDeviceRepository;
import com.rhbgroup.dcpbo.user.common.BoUserApprovalRepo;
import com.rhbgroup.dcpbo.user.common.DeviceProfileRepository;
import com.rhbgroup.dcpbo.user.common.KonySubscriptionService;
import com.rhbgroup.dcpbo.user.common.UserProfileRepository;
import com.rhbgroup.dcpbo.user.common.UserRepository;
import com.rhbgroup.dcpbo.user.common.model.bo.BoApprovalDevice;
import com.rhbgroup.dcpbo.user.common.model.dcp.DeviceProfile;
import com.rhbgroup.dcpbo.user.common.model.dcp.UserProfile;
import com.rhbgroup.dcpbo.user.info.model.bo.User;

public class WorkflowDeviceService {

    @Autowired
    BoUserApprovalRepo boUserApprovalRepo;

    @Autowired
    BoApprovalDeviceRepository boApprovalDeviceRepository;

    @Autowired
    DeviceProfileRepository deviceProfileRepository;

    @Autowired
    AdditionalDataHolder additionalDataHolder;

    @Autowired
    UserProfileRepository userProfileRepository;

    @Autowired
    KonySubscriptionService konySubscriptionService;

    @Autowired
    UserRepository userRepository;

    private static Logger logger = LogManager.getLogger(WorkflowDeviceService.class);

    public WorkflowDeviceResponse approveDeletion(String reason, Integer approvalId, Integer userId) {

        User user = userRepository.findById(userId);
        Date now = new Date();

        boUserApprovalRepo.updateDeviceStatus("A", reason, approvalId, user.getUsername(), now);

        BoApprovalDevice boApprovalDevice = boApprovalDeviceRepository.findByApprovalId(approvalId);
        String payload = boApprovalDevice.getPayload();

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> payloadMap = new HashMap<String, Object>();

        try{
            payloadMap = objectMapper.readValue(payload, new TypeReference<Map<String, String>>(){});
        } catch (Exception ex){
            throw new CommonException("80000", "Invalid payload", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        String deviceId = (String) payloadMap.get("deviceId");
        String customerId = (String) payloadMap.get("customerId");

        deviceProfileRepository.updateDeviceStatus("INACTIVE", new Integer(deviceId));
        userProfileRepository.nullifyTxnSigningDevice(new Integer(customerId), new Integer(deviceId));

        DeviceProfile deviceProfile = deviceProfileRepository.findOne(Integer.valueOf(deviceId));
        UserProfile userProfile = userProfileRepository.findByCustomerId(deviceProfile.getUserId());

        payloadMap.put("boRefNo", approvalId);
        payloadMap.put("moduleName", "Provide Assistance");
        payloadMap.put("customerId", deviceProfile.getUserId());
        payloadMap.put("customerIdType", userProfile.getIdType());
        payloadMap.put("customerIdNo", userProfile.getIdNo());
        payloadMap.put("username", userProfile.getUsername());
        payloadMap.put("deviceId", deviceId);
        payloadMap.put("deviceName", deviceProfile.getDeviceName());
        payloadMap.put("deviceOs", deviceProfile.getOs());

        String subscriberId = deviceProfile.getSubscriberId();

        if(StringUtils.isNotEmpty(subscriberId)){
            konySubscriptionService.deleteSubscriber(subscriberId, deviceProfile.getUserId(), userProfile.getCisNo());
        } else {
            logger.error("subscriberId not found for deviceId = " + deviceId);
        }

        WorkflowDeviceResponse workflowDeviceResponse = new WorkflowDeviceResponse();
        workflowDeviceResponse.setApprovalId(new Integer(approvalId));
        workflowDeviceResponse.setIdNo(userProfile.getIdNo());
        workflowDeviceResponse.setUsername(userProfile.getName());

        additionalDataHolder.setMap(payloadMap);

        return workflowDeviceResponse;
    }

}
