package com.rhbgroup.dcpbo.customer.service;

import java.io.IOException;
import java.util.UUID;

import com.rhbgroup.dcp.data.entity.profiles.UserProfile;
import com.rhbgroup.dcp.data.repository.ProfileRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestHeader;

import com.rhbgroup.dcp.loans.bizlogic.GetAsbLoanAccountDetailsLogic;
import com.rhbgroup.dcp.loans.model.LoanAccount;
import com.rhbgroup.dcp.model.Capsule;
import com.rhbgroup.dcp.model.Constants;
import com.rhbgroup.dcp.model.SourceChannel;
import com.rhbgroup.dcp.util.JsonUtil;
import com.rhbgroup.dcpbo.customer.contract.BoData;
import com.rhbgroup.dcpbo.customer.exception.CommonException;
import com.rhbgroup.dcpbo.customer.model.AsbDetails;
import com.rhbgroup.dcpbo.customer.repository.AsbProfileRepository;

@Service
public class AsbDetailsService {

	@Autowired
	private ProfileRepository profileRepository;

	@Autowired
	private AsbProfileRepository asbProfileRepository;
	
	@Autowired
	private GetAsbLoanAccountDetailsLogic getAsbLoanAccountDetailsLogic;
	
	private static Logger logger = LogManager.getLogger(AsbDetailsService.class);
	
	/*
	 * This is implemented as part of DCPBL-8471 user story
	 */
	public BoData getAsbDetails(
			@RequestHeader("customerId") Integer customerId,
			String accountNo) throws IOException {
		logger.info("working");
		logger.info("    asbProfileRepository: " + asbProfileRepository);

		/*
		 * In DCP, customerId is userId.
		 */
		int userId = customerId;

		logger.info("    accountNo: " + accountNo);

		LoanAccount asbAccount = new LoanAccount();
		asbAccount.setAccountNo(accountNo);
		String jsonStr = JsonUtil.objectToJson(asbAccount);
		logger.info("    jsonStr: " + jsonStr);

		UserProfile userProfile = profileRepository.getUserProfileByUserId(userId);


		Capsule capsule = new Capsule();
		capsule.setUserId(userId);
        capsule.setUserId(customerId);
		capsule.setCisNumber(userProfile.getCisNo());
        capsule.setProperty(Constants.SOURCE_CHANNEL, SourceChannel.DCP_MBK);
        capsule.setMessageId(UUID.randomUUID().toString());
        capsule.setProperty(Constants.OPERATION_NAME, "GetAsbLoanAccountDetails");
		capsule.updateCurrentMessage(jsonStr);
		logger.info("    capsule: " + capsule);
		
		capsule = getAsbLoanAccountDetailsLogic.executeBusinessLogic(capsule);
		logger.info("    capsule: " + capsule);
        logger.info("        isOperationSuccesful: " + capsule.isOperationSuccessful());

        if (capsule.isOperationSuccessful() == null || !capsule.isOperationSuccessful())
                throw new CommonException(CommonException.GENERIC_ERROR_CODE, "Error calling GetAsbLoanAccountDetailsLogic.executeBusinessLogic()");
		
		jsonStr = capsule.getCurrentMessage();
		logger.info("        jsonStr: " + jsonStr);

		AsbDetails asbDetails = JsonUtil.jsonToObject(jsonStr, AsbDetails.class);
        logger.info("    asbDetails: " + asbDetails);
		
		return asbDetails;
	}
}
