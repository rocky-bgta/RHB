package com.rhbgroup.dcpbo.customer.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rhbgroup.dcpbo.customer.dcpbo.BoApproval;
import com.rhbgroup.dcpbo.customer.dcpbo.BoApprovalDevice;
import com.rhbgroup.dcpbo.customer.dcpbo.BoConfigFunction;
import com.rhbgroup.dcpbo.customer.dcpbo.BoUser;
import com.rhbgroup.dcpbo.customer.exception.CommonException;
import com.rhbgroup.dcpbo.customer.model.DelApprovalRequest;
import com.rhbgroup.dcpbo.customer.model.DelApprovalResponse;
import com.rhbgroup.dcpbo.customer.model.DeviceProfile;
import com.rhbgroup.dcpbo.customer.model.UserProfile;
import com.rhbgroup.dcpbo.customer.repository.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

@Service
public class DeleteDeviceRequestService {

    @Autowired
    BoConfigFunctionRepository boConfigFunctionRepository;

    @Autowired
    BoApprovalRepository boApprovalRepository;

    @Autowired
    BoApprovalDeviceRepository boApprovalDeviceRepository;

    @Autowired
    BoUserRepository boUserRepository;

    @Autowired
    DeviceProfileRepository deviceProfileRepository;

    @Autowired
    UserProfileRepository userProfileRepository;

    @Autowired
    KonySubscriptionService konySubscriptionService;

    private static Logger logger = LogManager.getLogger(DeleteDeviceRequestService.class);

    private static final String DELETE_DEVICE_OPERATION_NAME = "BODeleteDevice";

    public DelApprovalResponse deleteDevice(Integer deviceId, Integer userId, DelApprovalRequest delApprovalRequest) {

        Integer functionId = delApprovalRequest.getFunctionId();
        String deviceName = delApprovalRequest.getName();
        Integer customerId = new Integer(delApprovalRequest.getCustomerId());

        BoConfigFunction boConfigFunction = boConfigFunctionRepository.findByFunctionId(functionId);
        Date dateNow = new Date();
        DelApprovalResponse delApprovalResponse = new DelApprovalResponse();

        if (!boConfigFunction.getApprovalRequired()) {

            DeviceProfile deviceProfile = deviceProfileRepository.findOne(deviceId);
            UserProfile userProfile = userProfileRepository.findByCustomerId(deviceProfile.getUserId());

            deviceProfileRepository.updateDeviceStatus("INACTIVE", deviceId);
            userProfileRepository.nullifyTxnSigningDevice(customerId, deviceId);

            delApprovalResponse.setApprovalId(0);
            delApprovalResponse.setIsWritten("Y");

            String subscriberId = deviceProfile.getSubscriberId();

            if (StringUtils.isNotEmpty(subscriberId)) {
                konySubscriptionService.deleteSubscriber(subscriberId, deviceProfile.getUserId(), userProfile.getCisNo());
            }

            return delApprovalResponse;

        } else {

            BoApprovalDevice boApprovalDevice;

            List<Integer> approvalIdList = boApprovalRepository.findPendingRequest(functionId);

            if (approvalIdList != null && approvalIdList.size() > 0) {
                boApprovalDevice = boApprovalDeviceRepository.findApprovalIdByApprovalList(approvalIdList, deviceId.toString());
                if (boApprovalDevice != null) throw new CommonException("40002", "", HttpStatus.FORBIDDEN);
            }

            BoUser boUser = boUserRepository.findOne(userId);

            BoApproval newBoApproval = new BoApproval();

            newBoApproval.setFunctionId(functionId);
            newBoApproval.setCreatorId(userId);
            newBoApproval.setDescription(deviceName);
            newBoApproval.setActionType("DELETE");
            newBoApproval.setStatus("P");
            newBoApproval.setCreatedTime(dateNow);
            newBoApproval.setCreatedBy(boUser.getUsername());
            newBoApproval.setUpdatedTime(dateNow);
            newBoApproval.setUpdatedBy(boUser.getUsername());

            BoApproval savedBoApproval = boApprovalRepository.save(newBoApproval);

            UserProfile userProfile = userProfileRepository.findByCustomerId(new Integer(delApprovalRequest.getCustomerId()));
            HashMap<String, Object> data = new HashMap<>();

            data.put("boRefNo", savedBoApproval.getId());
            data.put("moduleName", "Provide Assistance");
            data.put("customerId", delApprovalRequest.getCustomerId());
            data.put("customerIdType", userProfile.getIdType());
            data.put("customerIdNo", userProfile.getIdNo());
            data.put("username", delApprovalRequest.getUsername());
            data.put("deviceId", deviceId);
            data.put("deviceName", delApprovalRequest.getName());
            data.put("deviceOs", delApprovalRequest.getOs());

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode payloadJson = objectMapper.convertValue(data, JsonNode.class);

            BoApprovalDevice newBoApprovalDevice = new BoApprovalDevice();

            newBoApprovalDevice.setApprovalId(savedBoApproval.getId());
            newBoApprovalDevice.setState("B");
            newBoApprovalDevice.setLockingId(deviceId.toString());
            newBoApprovalDevice.setPayload(payloadJson.toString());
            newBoApprovalDevice.setCreatedTime(dateNow);
            newBoApprovalDevice.setCreatedBy(boUser.getUsername());
            newBoApprovalDevice.setUpdatedTime(dateNow);
            newBoApprovalDevice.setUpdatedBy(boUser.getUsername());

            boApprovalDeviceRepository.save(newBoApprovalDevice);

            delApprovalResponse.setApprovalId(savedBoApproval.getId());
            delApprovalResponse.setIsWritten("N");

            return delApprovalResponse;

        }
    }
}

