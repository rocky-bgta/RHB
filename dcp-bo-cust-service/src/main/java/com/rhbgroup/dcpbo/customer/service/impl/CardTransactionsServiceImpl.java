package com.rhbgroup.dcpbo.customer.service.impl;

import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.rhbgroup.dcp.creditcards.bizlogic.GetCreditCardTransactionsLogic;
import com.rhbgroup.dcp.creditcards.model.DcpCardTransactionHistoryRequest;
import com.rhbgroup.dcp.creditcards.model.DcpCardTransactionHistoryRequestPagination;
import com.rhbgroup.dcp.data.entity.profiles.UserProfile;
import com.rhbgroup.dcp.data.repository.ProfileRepository;
import com.rhbgroup.dcp.model.Capsule;
import com.rhbgroup.dcp.model.Constants;
import com.rhbgroup.dcp.model.SourceChannel;
import com.rhbgroup.dcp.util.JsonUtil;
import com.rhbgroup.dcpbo.customer.contract.BoData;
import com.rhbgroup.dcpbo.customer.dto.CardTransactions;
import com.rhbgroup.dcpbo.customer.exception.CommonException;
import com.rhbgroup.dcpbo.customer.model.CardProfile;
import com.rhbgroup.dcpbo.customer.repository.CardRepository;
import com.rhbgroup.dcpbo.customer.service.CardTransactionsService;

@Service
public class CardTransactionsServiceImpl implements CardTransactionsService {
	@Autowired
	private ProfileRepository profileRepository;
	
	@Autowired
	private CardRepository cardRepository;
	
	@Autowired
	private GetCreditCardTransactionsLogic getCreditCardTransactionsLogic;
	
	private static Logger logger = LogManager.getLogger(CardTransactionsServiceImpl.class);
	
	/*
	 * This is implemented as part of DCPBL-8744 user story
	 */
	public BoData getCardTransactions (Integer customerId, String accountNo, String firstKey, String lastKey, String pageCounter) {
		logger.debug("getCardTransactions()");
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


		String cardNo = accountNo;
		logger.debug("    cardNo: " + cardNo);

		DcpCardTransactionHistoryRequestPagination pagination = new DcpCardTransactionHistoryRequestPagination();
		pagination.setFirstKey(firstKey);
		pagination.setLastKey(lastKey);
		pagination.setPageCounter(pageCounter);
		
		DcpCardTransactionHistoryRequest dcpCardTransactionHistoryRequest = new DcpCardTransactionHistoryRequest();
		dcpCardTransactionHistoryRequest.setCardNo(cardNo);
		dcpCardTransactionHistoryRequest.setPagination(pagination);
		
        String jsonStr = JsonUtil.objectToJson(dcpCardTransactionHistoryRequest);
        logger.debug("    jsonStr: " + jsonStr);
		
        Capsule capsule = new Capsule();
        capsule.setCisNumber(cisNo);
        capsule.setUserId(customerId);
        capsule.setProperty(Constants.SOURCE_CHANNEL, SourceChannel.DCP_MBK);
        capsule.setMessageId(UUID.randomUUID().toString());
        capsule.setProperty(Constants.OPERATION_NAME, "GetCreditCardTransactions");
        capsule.updateCurrentMessage(jsonStr);
        logger.debug("    capsule: " + capsule);

        capsule = getCreditCardTransactionsLogic.executeBusinessLogic(capsule);
        logger.debug("    capsule: " + capsule);
        logger.debug("        isOperationSuccesful: " + capsule.isOperationSuccessful());

        if (capsule.isOperationSuccessful() == null || !capsule.isOperationSuccessful())
        	throw new CommonException(CommonException.GENERIC_ERROR_CODE, "Error calling GetCreditCardDetailsLogic.executeBusinessLogic()");

        jsonStr = capsule.getCurrentMessage();
        logger.debug("        jsonStr: " + jsonStr);
		
		CardTransactions cardTransactions = JsonUtil.jsonToObject(jsonStr, CardTransactions.class);
		return cardTransactions;
	}
}
