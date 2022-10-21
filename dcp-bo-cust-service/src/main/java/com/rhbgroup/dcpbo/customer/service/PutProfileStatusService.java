package com.rhbgroup.dcpbo.customer.service;

import com.rhbgroup.dcpbo.customer.audit.collector.AdditionalDataHolder;
import com.rhbgroup.dcpbo.customer.dto.ProfileStatus;
import com.rhbgroup.dcpbo.customer.enums.UserStatus;
import com.rhbgroup.dcpbo.customer.exception.CommonException;
import com.rhbgroup.dcpbo.customer.model.UserProfile;
import com.rhbgroup.dcpbo.customer.repository.UserProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;


/**
 * Service class for managing users.
 */
@Service
@Transactional
public class PutProfileStatusService {

    @Autowired
    UserProfileRepository userProfileRepository;

    private static final String LOCKEDREASON = "Challenge question attempted more than 3 times";

    @Autowired
    private AdditionalDataHolder additionalDataHolder;

    public ProfileStatus writeProfileStatus(String customerId) {
        ProfileStatus profileStatus = new ProfileStatus();
        Integer customerIdInt = Integer.parseInt(customerId);
        UserProfile userProfile = userProfileRepository.findByCustomerId(customerIdInt);
        if (userProfile == null)
            throw new CommonException(CommonException.GENERIC_ERROR_CODE, "User not found for id " + customerId);

        Integer successUpdate = userProfileRepository.updateProfileStatus(customerIdInt, UserStatus.ACTIVE.getValue());
        if (successUpdate != 1)
            throw new CommonException(CommonException.GENERIC_ERROR_CODE, "Reset operation for customer profile failed for " + customerId);

        profileStatus.setUserStatus(UserStatus.ACTIVE.getValue());

        //Populate additional Data for auditing
        HashMap<String, String> additionalData = new HashMap<>();

        String username = userProfile.getUsername();
        String name = userProfile.getName();
        additionalData.put("username", username);
        additionalData.put("name", name);
        additionalData.put("lockedReason", LOCKEDREASON);

        additionalDataHolder.setMap(additionalData);

        return profileStatus;
    }
}
