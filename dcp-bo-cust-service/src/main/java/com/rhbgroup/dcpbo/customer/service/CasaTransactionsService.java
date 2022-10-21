package com.rhbgroup.dcpbo.customer.service;

import java.util.UUID;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.rhbgroup.dcp.data.entity.profiles.UserProfile;
import com.rhbgroup.dcp.data.repository.ProfileRepository;
import com.rhbgroup.dcp.deposits.casa.bizlogic.GetDepositTransactionsLogic;
import com.rhbgroup.dcp.deposits.casa.model.DcpTransactionHistoryRequest;
import com.rhbgroup.dcp.deposits.casa.model.DcpTransactionHistoryRequestPagination;
import com.rhbgroup.dcp.model.Capsule;
import com.rhbgroup.dcp.model.Constants;
import com.rhbgroup.dcp.model.SourceChannel;
import com.rhbgroup.dcp.util.JsonUtil;
import com.rhbgroup.dcpbo.customer.contract.BoData;
import com.rhbgroup.dcpbo.customer.dto.CasaTransactions;
import com.rhbgroup.dcpbo.customer.exception.CommonException;

@Service
public class CasaTransactionsService {
	@Autowired
	private ProfileRepository profileRepository;
	
	@Autowired
	private GetDepositTransactionsLogic getDepositTransactionsLogic;
	
	private static Logger logger = LogManager.getLogger(CasaTransactionsService.class);
	
	/*
	 * This is implemented as part of DCPBL-8171 user story
	 */
	public BoData getCasaTransactions (Integer customerId, String accountNo, String firstKey, String lastKey, Integer pageCounter) {
		logger.debug("getCasaTransactions()");
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

		DcpTransactionHistoryRequestPagination pagination = new DcpTransactionHistoryRequestPagination();
		pagination.setFirstKey(firstKey);
		pagination.setLastKey(lastKey);
		pagination.setPageCounter(pageCounter);
		pagination.setSource("");
		
		DcpTransactionHistoryRequest dcpTransactionHistoryRequest = new DcpTransactionHistoryRequest();
		dcpTransactionHistoryRequest.setAccountNo(accountNo);
		dcpTransactionHistoryRequest.setPagination(pagination);
		
        String jsonStr = JsonUtil.objectToJson(dcpTransactionHistoryRequest);
        logger.debug("    jsonStr: " + jsonStr);
		
        Capsule capsule = new Capsule();
        capsule.setCisNumber(cisNo);
        capsule.setUserId(customerId);
        capsule.setProperty(Constants.SOURCE_CHANNEL, SourceChannel.DCP_MBK);
        capsule.setMessageId(UUID.randomUUID().toString());
        capsule.setProperty(Constants.OPERATION_NAME, "GetDepositTransactions");
        capsule.updateCurrentMessage(jsonStr);
        logger.debug("    capsule: " + capsule);

        capsule = getDepositTransactionsLogic.executeBusinessLogic(capsule);
        logger.debug("    capsule: " + capsule);
        logger.debug("        isOperationSuccesful: " + capsule.isOperationSuccessful());

        if (capsule.isOperationSuccessful() == null || !capsule.isOperationSuccessful())
                throw new CommonException(CommonException.GENERIC_ERROR_CODE, "Error calling GetDepositTransactionsLogic.executeBusinessLogic()");

        jsonStr = capsule.getCurrentMessage();
        logger.debug("        jsonStr: " + jsonStr);
		
		CasaTransactions casaTransactions = JsonUtil.jsonToObject(jsonStr, CasaTransactions.class);
		return casaTransactions;
	}
}
