package com.rhbgroup.dcpbo.customer.service.impl;

import java.util.UUID;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.rhbgroup.dcp.adaptor.BusinessAdaptor;
import com.rhbgroup.dcp.asnb.model.DcpAsnbTxnRequest;
import com.rhbgroup.dcp.data.entity.profiles.UserProfile;
import com.rhbgroup.dcp.data.repository.ProfileRepository;
import com.rhbgroup.dcp.model.Capsule;
import com.rhbgroup.dcp.model.Constants;
import com.rhbgroup.dcp.model.SourceChannel;
import com.rhbgroup.dcp.uber.asnb.bizlogic.GetAsnbTransactionInquiryLogic;
import com.rhbgroup.dcp.util.JsonUtil;
import com.rhbgroup.dcpbo.customer.contract.BoData;
import com.rhbgroup.dcpbo.customer.dto.AsnbTransactions;
import com.rhbgroup.dcpbo.customer.exception.AsnbTransactionException;
import com.rhbgroup.dcpbo.customer.exception.CommonException;
import com.rhbgroup.dcpbo.customer.service.AsnbTransactionsService;

/**
 * 
 * @author hassan.malik
 *
 */

@Service
public class AsnbTransactionsServiceImpl implements AsnbTransactionsService{

	@Autowired
	private ProfileRepository profileRepository;

	@Autowired
	private GetAsnbTransactionInquiryLogic getAsnbTransactionsInquiryLogic;

	private static Logger logger = LogManager.getLogger(AsnbTransactionsServiceImpl.class);

	/*
	 * This is implemented as part of DCP2-1546 user story
	 */
	public BoData getAsnbTransactions(Integer customerId, String fundId, String identificationNumber,
			String identificationType, String membershipNumber, boolean isMinor, String guardianIdNumber) {
		
		logger.debug("getAsnbTransactions()");
		logger.debug("profileRepository: " + profileRepository);

		/*
		 * Get cisNo from DCP ProfileRepository. In DCP, customerId is userId.
		 */
		int userId = customerId;
		UserProfile userProfile = profileRepository.getUserProfileByUserId(userId);
		logger.debug("userProfile:" + userProfile);
		if (userProfile == null)
			throw new CommonException(CommonException.GENERIC_ERROR_CODE);
		String jsonStr = "";

		DcpAsnbTxnRequest asnbTransRequest = new DcpAsnbTxnRequest();
		asnbTransRequest.setFundId(fundId);
		asnbTransRequest.setIdentificationNumber(identificationNumber);
		asnbTransRequest.setIdentificationType(identificationType);
		asnbTransRequest.setMembershipNumber(membershipNumber);
		// if minor is true send guardianIdNumber
		if (isMinor) {
			logger.debug("isMinor:" + isMinor);
			asnbTransRequest.setGuardianIdNumber(guardianIdNumber);
		}

		jsonStr = JsonUtil.objectToJson(asnbTransRequest);
		logger.debug("TransactionService jsonStr: " + jsonStr);
		AsnbTransactions asnbTransactions;
		
		try {
			jsonStr = callDcpBusinessLogic(getAsnbTransactionsInquiryLogic, userProfile, jsonStr);
			logger.debug("From TransactionService, jsonStr: " + jsonStr);
		} catch (Exception e) {
			logger.debug("exception: " + e);
		}
		asnbTransactions = JsonUtil.jsonToObject(jsonStr, AsnbTransactions.class);
		
		return asnbTransactions;
	}

	private String callDcpBusinessLogic(BusinessAdaptor businessAdaptor, UserProfile userProfile, String currentMessage)
			throws AsnbTransactionException {
		String cisNo = userProfile.getCisNo();
		logger.debug("cisNo:" + cisNo);

		int userId = userProfile.getId();
		logger.debug("userId: " + userId);

		Capsule capsule = new Capsule();
		capsule.setCisNumber(cisNo);
		capsule.setUserId(userId);
		capsule.setProperty(Constants.SOURCE_CHANNEL, SourceChannel.DCP_MBK);
		capsule.setMessageId(UUID.randomUUID().toString());
		capsule.setProperty(Constants.OPERATION_NAME, businessAdaptor.getClass().getSimpleName());
		capsule.updateCurrentMessage(currentMessage);
		logger.debug("capsule:" + capsule);

		String businessLogicName = businessAdaptor.getClass().getSimpleName();
		logger.debug("Calling DCP business logic:" + businessLogicName + "executeBusinessLogic()");

		capsule = businessAdaptor.executeBusinessLogic(capsule);
		logger.debug("capsule:" + capsule);
		logger.debug("isOperationSuccesful:" + capsule.isOperationSuccessful());

		if (capsule.isOperationSuccessful() == null || !capsule.isOperationSuccessful()) {
			logger.error("capsule.isOperationSuccessful() is either null or false when calling DCP business logic: "
					+ businessLogicName + ". Throwing exception ...");
			throw new AsnbTransactionException("Error calling " + businessLogicName + ".executeBusinessLogic()");
		}

		String jsonStr = capsule.getCurrentMessage();
		logger.debug("jsonStr: " + jsonStr);

		return jsonStr;
	}

	

}
