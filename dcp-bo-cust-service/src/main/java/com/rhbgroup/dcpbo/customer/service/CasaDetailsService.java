package com.rhbgroup.dcpbo.customer.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.rhbgroup.dcp.data.entity.profiles.UserProfile;
import com.rhbgroup.dcp.data.repository.ProfileRepository;
import com.rhbgroup.dcp.deposits.casa.bizlogic.GetAccountDetailsLogic;
import com.rhbgroup.dcp.model.Capsule;
import com.rhbgroup.dcp.model.Constants;
import com.rhbgroup.dcp.model.SourceChannel;
import com.rhbgroup.dcp.util.JsonUtil;
import com.rhbgroup.dcpbo.customer.contract.BoData;
import com.rhbgroup.dcpbo.customer.exception.CommonException;
import com.rhbgroup.dcpbo.customer.model.AccountDetails;

@Service
public class CasaDetailsService {
	@Autowired
	private ProfileRepository profileRepository;
	
	@Autowired
	private GetAccountDetailsLogic getAccountDetailsLogic;
	
	private static Logger logger = LogManager.getLogger(CasaDetailsService.class);
	
	/*
	 * This is implemented as part of DCPBL-8199 user story
	 */
	public BoData getCasaDetails(Integer customerId, String accountNo) throws IOException {
		logger.debug("getCasaDetails()");
		logger.debug("    profileRepository: " + profileRepository);

		/*
		 * Get cisNo from DCP ProfileRepository. In DCP, customerId is userId.
		 */
		int userId = customerId;
		UserProfile userProfile = profileRepository.getUserProfileByUserId(userId);
		logger.debug("    userProfile: " + userProfile);
		if (userProfile == null)
            throw new CommonException(CommonException.GENERIC_ERROR_CODE);

		String cisNo = userProfile.getCisNo();
		logger.debug("    cisNo: " + cisNo);

		logger.debug("    accountNo: " + accountNo);

		Map<String, String> jsonBody = new HashMap<>();

		logger.info("Account No: " + accountNo);

		jsonBody.put("accountNo", accountNo);

		String jsonStr = JsonUtil.objectToJson(jsonBody);
		logger.debug("    jsonStr: " + jsonStr);
		
		Capsule capsule = new Capsule(jsonStr);
		capsule.setUserId(customerId);
		capsule.setCisNumber(cisNo);
		capsule.setProperty(Constants.SOURCE_CHANNEL, SourceChannel.DCP_MBK);
		capsule.setMessageId(UUID.randomUUID().toString());
		capsule.setProperty(Constants.OPERATION_NAME, "GetAccountDetails");
		logger.debug("    capsule: " + capsule);
		
		capsule = getAccountDetailsLogic.executeBusinessLogic(capsule);
		logger.debug("    capsule: " + capsule);
        logger.debug("        isOperationSuccesful: " + capsule.isOperationSuccessful());

        if (capsule.isOperationSuccessful() == null || !capsule.isOperationSuccessful())
                throw new CommonException(CommonException.GENERIC_ERROR_CODE, "Error calling GetAccountDetailsLogic.executeBusinessLogic()");
		
		jsonStr = capsule.getCurrentMessage();
		logger.debug("        jsonStr: " + jsonStr);
		
		AccountDetails accountDetails = JsonUtil.jsonToObject(jsonStr, AccountDetails.class);
		logger.debug("    accountDetails: " + accountDetails);
		
		return accountDetails;
	}
}
