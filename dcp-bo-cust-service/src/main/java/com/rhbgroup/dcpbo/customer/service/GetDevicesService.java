package com.rhbgroup.dcpbo.customer.service;

import com.rhbgroup.dcpbo.customer.dto.Device;
import com.rhbgroup.dcpbo.customer.exception.CommonException;
import com.rhbgroup.dcpbo.customer.model.DeviceProfile;
import com.rhbgroup.dcpbo.customer.model.UserProfile;
import com.rhbgroup.dcpbo.customer.repository.DeviceProfileRepository;
import com.rhbgroup.dcpbo.customer.repository.UserProfileRepository;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


@Service
public class GetDevicesService {
    @Autowired
    DeviceProfileRepository deviceProfileRepository;
    @Autowired
    UserProfileRepository userProfileRepository;


    //Retrieve Topup transition details
    public List<Device> retrieveDevice(String customerId) {
        //- 1 -search devices and return if it's not deleted
        List<DeviceProfile> deviceProfileList;
        List<Device> deviceList = new ArrayList<>();
        Integer userId = Integer.parseInt(customerId);

        deviceProfileList = deviceProfileRepository.findDeviceByUserIdAndActiveDevice(userId);
        if(deviceProfileList.size() == 0)
            return  deviceList;

        //- 2 -retrieve profile user for device to compare with current device
        UserProfile userprofile = userProfileRepository.findByCustomerId(userId);
        if (userprofile == null)
            throw new CommonException(CommonException.GENERIC_ERROR_CODE,"No user profile found for customer " + customerId);
        Integer mainDeviceId = userprofile.getTxnSigningDevice();

        //- 3 -populate response object
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

        for (DeviceProfile deviceProfile :deviceProfileList){
            Device device;
            device = modelMapper.map(deviceProfile,Device.class);
            //DCPBL-12538
            device.setName(deviceProfile.getDeviceName());
            //format time to include timezone

            Boolean primaryDevice = false;
            if (deviceProfile.getId().equals(mainDeviceId))
                primaryDevice = true;
            device.setPrimaryDevice(String.valueOf(primaryDevice));

            deviceList.add(device);
        }

        return deviceList;
    }
}