package com.rhbgroup.dcpbo.customer.service;

import java.util.HashMap;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.rhbgroup.dcpbo.customer.audit.collector.AdditionalDataHolder;
import com.rhbgroup.dcpbo.customer.exception.CommonException;
import com.rhbgroup.dcpbo.customer.model.DeviceProfile;
import com.rhbgroup.dcpbo.customer.model.UserProfile;
import com.rhbgroup.dcpbo.customer.repository.DeviceProfileRepository;
import com.rhbgroup.dcpbo.customer.repository.UserProfileRepository;

@Service
public class DeleteDeviceService {

    @Autowired
    private DeviceProfileRepository deviceProfileRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private AdditionalDataHolder additionalDataHolder;

    @Autowired
    KonySubscriptionService konySubscriptionService;

    public static final String ACTIVE = "ACTIVE";
    public static final String INACTIVE = "INACTIVE";
    
    private static final String DELETE_DEVICE_OPERATION_NAME = "BODeleteDevice";

	private static Logger logger = LogManager.getLogger(DeleteDeviceService.class);


    public boolean deleteDevice(Integer deviceId){

        DeviceProfile deviceProfile = deviceProfileRepository.findOne(deviceId);
        UserProfile userProfile = userProfileRepository.findByCustomerId(deviceProfile.getUserId());

        Integer successDel = deviceProfileRepository.updateDeviceStatus(INACTIVE, deviceId);

        if (successDel!=1)
            throw new CommonException(CommonException.GENERIC_ERROR_CODE, "Delete device operation is unsuccessful for device id " + deviceId);

        Integer successNull = userProfileRepository.nullifyTxnSigningDevice(deviceProfile.getUserId(), deviceId);
        if(successNull != 1) {
        	logger.warn("Delete device with no TxnSigningDevice for device id : {}", deviceId);
        }

        //DCPBL-19683 : to call KES subscriber service for inactive and delete subscriber
        String subscriberId = deviceProfile.getSubscriberId();
        if(StringUtils.isNotEmpty(subscriberId)){
            konySubscriptionService.deleteSubscriber(subscriberId, deviceProfile.getUserId(), userProfile.getCisNo());
        }

        HashMap<String, String> additionalData = new HashMap<>();

        String deviceName = deviceProfile.getDeviceName();
        String os = deviceProfile.getOs();

        additionalData.put("moduleName", "Provide Assistance");
        additionalData.put("customerId", String.valueOf(userProfile.getId()));
        additionalData.put("customerIdType", userProfile.getIdType());
        additionalData.put("customerIdNo", userProfile.getIdNo());
        additionalData.put("userId", userProfile.getId().toString());
        additionalData.put("username", userProfile.getUsername());
        additionalData.put("deviceId", deviceId.toString());
        additionalData.put("deviceName", deviceName);
        additionalData.put("deviceOs", os);

        additionalDataHolder.setMap(additionalData);

        return true;
    }
}
