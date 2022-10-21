package com.rhbgroup.dcpbo.customer.service;

import java.io.IOException;
import java.util.UUID;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestHeader;

import com.rhbgroup.dcp.data.entity.profiles.UserProfile;
import com.rhbgroup.dcp.data.repository.ProfileRepository;
import com.rhbgroup.dcp.loans.bizlogic.GetHirePurchaseAccountDetailsLogic;
import com.rhbgroup.dcp.loans.model.LoanAccount;
import com.rhbgroup.dcp.model.Capsule;
import com.rhbgroup.dcp.model.Constants;
import com.rhbgroup.dcp.model.SourceChannel;
import com.rhbgroup.dcp.util.JsonUtil;
import com.rhbgroup.dcpbo.customer.contract.BoData;
import com.rhbgroup.dcpbo.customer.exception.CommonException;
import com.rhbgroup.dcpbo.customer.model.HirePurchaseDetails;
import com.rhbgroup.dcpbo.customer.repository.HirePurchaseProfileRepository;

@Service
public class HirePurchaseDetailsService {
    @Autowired
    private ProfileRepository profileRepository;

	@Autowired
	private HirePurchaseProfileRepository hirePurchaseProfileRepository;
	
	@Autowired
	private GetHirePurchaseAccountDetailsLogic getHirePurchaseDetailsLogic;
	
	private static Logger logger = LogManager.getLogger(HirePurchaseDetailsService.class);
	
	/*
	 * This is implemented as part of DCPBL-8724 user story
	 */
	public BoData getHirePurchaseDetails(
			@RequestHeader("customerId") Integer customerId,
			String accountNo) throws IOException {
		logger.debug("getHirePurchaseDetails()");
		logger.debug("    hirePurchaseProfileRepository: " + hirePurchaseProfileRepository);

		/*
		 * Get cisNo from DCP ProfileRepository. In DCP, customerId is userId.
		 */
        int userId = customerId;
        UserProfile userProfile = profileRepository.getUserProfileByUserId(userId);
        logger.debug("    userProfile: " + userProfile);
        if (userProfile == null)
            throw new CommonException(CommonException.GENERIC_ERROR_CODE, "Cannot find UserProfile for userId: " +  userId);

        String cisNo = userProfile.getCisNo();
        logger.debug("    cisNo: " + cisNo);
		
		logger.debug("    accountNo: " + accountNo);

		LoanAccount hirePurchaseAccount = new LoanAccount();
		hirePurchaseAccount.setAccountNo(accountNo);
		String jsonStr = JsonUtil.objectToJson(hirePurchaseAccount);
		logger.debug("    jsonStr: " + jsonStr);
		
		Capsule capsule = new Capsule();
		capsule.setUserId(userId);
        capsule.setCisNumber(cisNo);
        capsule.setProperty(Constants.SOURCE_CHANNEL, SourceChannel.DCP_MBK);
        capsule.setMessageId(UUID.randomUUID().toString());
        capsule.setProperty(Constants.OPERATION_NAME, "GetHirePurchaseDetails");
		capsule.updateCurrentMessage(jsonStr);
		logger.debug("    capsule: " + capsule);
		
		capsule = getHirePurchaseDetailsLogic.executeBusinessLogic(capsule);
		logger.debug("    capsule: " + capsule);
        logger.debug("        isOperationSuccesful: " + capsule.isOperationSuccessful());

        if (capsule.isOperationSuccessful() == null || !capsule.isOperationSuccessful())
                throw new CommonException(CommonException.GENERIC_ERROR_CODE, "Error calling GetHirePurchaseDetailsLogic.executeBusinessLogic()");
		
		jsonStr = capsule.getCurrentMessage();
		logger.debug("        jsonStr: " + jsonStr);

        HirePurchaseDetails hirePurchaseDetails = JsonUtil.jsonToObject(jsonStr, HirePurchaseDetails.class);
        logger.debug("    hirePurchaseDetails: " + hirePurchaseDetails);
		
		return hirePurchaseDetails;
	}
}
