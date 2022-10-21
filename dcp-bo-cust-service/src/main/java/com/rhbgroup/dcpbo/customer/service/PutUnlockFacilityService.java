package com.rhbgroup.dcpbo.customer.service;

import com.rhbgroup.dcpbo.customer.audit.collector.AdditionalDataHolder;
import com.rhbgroup.dcpbo.customer.dto.UnlockData;
import com.rhbgroup.dcpbo.customer.dto.UnlockStatus;
import com.rhbgroup.dcpbo.customer.exception.CommonException;
import com.rhbgroup.dcpbo.customer.model.RegistrationToken;
import com.rhbgroup.dcpbo.customer.repository.CustomerVerificationRepo;
import com.rhbgroup.dcpbo.customer.repository.RegistrationTokenRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

@Service
@Transactional
public class PutUnlockFacilityService {

    @Autowired
    CustomerVerificationRepo customerVerificationRepository;

    @Autowired
    RegistrationTokenRepo registrationTokenRepository;

    @Autowired
    private AdditionalDataHolder additionalDataHolder;

    public UnlockStatus writeUnlockFacility(String acctNumber) {

        UnlockStatus unlockStatus = new UnlockStatus();
        Date date = new Date();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
        String strDate = dateFormat.format(date);
        String status;

        Integer successUpdate = customerVerificationRepository.updateUnlockStatus(acctNumber, Boolean.FALSE, date);

        if (successUpdate == 0) {
            throw new CommonException(CommonException.GENERIC_ERROR_CODE, "Reset operation for unlock status failed for " + acctNumber);
        } else {
            status = "Successful";
        }

        HashMap<String, String> additionalData = new HashMap<>();

        UnlockData unlockData = customerVerificationRepository.retrieveUnblockData(acctNumber);

        if (unlockData != null && !unlockData.getName().equalsIgnoreCase("")) {
            RegistrationToken registrationToken = registrationTokenRepository.findByAccountNumber(acctNumber);
            additionalData.put("name", registrationToken.getName());
            additionalData.put("idNo", registrationToken.getIdNo());
            additionalData.put("mobileNumber", registrationToken.getMobileNo());
        }

        if (status.equals("Successful")) {
            unlockStatus.setIsSuccess("1");
        } else {
            unlockStatus.setIsSuccess("0");
        }

        additionalData.put("accountNumber", acctNumber);
        additionalData.put("unlockStatus", status);
        additionalData.put("unlockDateTime", strDate);

        additionalDataHolder.setMap(additionalData);

        return unlockStatus;
    }
}
