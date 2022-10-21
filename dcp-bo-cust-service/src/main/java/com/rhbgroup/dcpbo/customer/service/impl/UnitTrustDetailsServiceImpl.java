package com.rhbgroup.dcpbo.customer.service.impl;

import com.rhbgroup.dcp.data.entity.profiles.UserProfile;
import com.rhbgroup.dcp.data.repository.ProfileRepository;
import com.rhbgroup.dcp.investments.bizlogic.GetUnitTrustDetailsLogic;
import com.rhbgroup.dcp.loans.model.PersonalFinanceAccountsInquiry;
import com.rhbgroup.dcp.model.Capsule;
import com.rhbgroup.dcp.util.JsonUtil;
import com.rhbgroup.dcpbo.customer.audit.collector.AdditionalDataHolder;
import com.rhbgroup.dcpbo.customer.contract.BoData;
import com.rhbgroup.dcpbo.customer.exception.CommonException;
import com.rhbgroup.dcpbo.customer.model.UnitTrustDetails;
import com.rhbgroup.dcpbo.customer.service.UnitTrustDetailsService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class UnitTrustDetailsServiceImpl implements UnitTrustDetailsService {

	private static Logger logger = LogManager.getLogger(UnitTrustDetailsServiceImpl.class);

	@Autowired
    private GetUnitTrustDetailsLogic getUnitTrustDetailsLogic;

	@Autowired
    private AdditionalDataHolder additionalDataHolder;

    @Autowired
    private ProfileRepository profileRepository;

    @Override
	public BoData getUnitTrustDetails(String accountNo, Integer customerId) {
        Map<String, String> accountMap = new HashMap<>();
        accountMap.put("accountNo", accountNo);

        String body = JsonUtil.objectToJson(accountMap);

        logger.debug("Capsule current message : " + body);

        Capsule capsule = new Capsule(body);
        capsule.setUserId(customerId);

        capsule = getUnitTrustDetailsLogic.executeBusinessLogic(capsule);

        if (capsule.isOperationSuccessful() == null || !capsule.isOperationSuccessful()) {
            throw new CommonException(CommonException.GENERIC_ERROR_CODE, "operation failed", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        String jsonStr = capsule.getCurrentMessage();
        UnitTrustDetails unitTrustDetails = JsonUtil.jsonToObject(jsonStr, UnitTrustDetails.class);

        HashMap<String, String> additionalData = new HashMap<>();
        additionalData.put("module", "Provide Assistance");

        try {
            UserProfile userProfile = profileRepository.getUserProfileByUserId(customerId);
            additionalData.put("username", userProfile.getUsername());
            additionalData.put("customerId", userProfile.getIdNo());
        } catch (Exception ex) {
            logger.error("Failed to get user with customer id = " + customerId);
        }

        additionalData.put("accountNumber", accountNo);

        additionalDataHolder.setMap(additionalData);

        return unitTrustDetails;
    }
}
