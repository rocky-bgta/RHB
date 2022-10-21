package com.rhbgroup.dcpbo.customer.service.impl;

import java.util.HashMap;
import java.util.UUID;

import com.rhbgroup.dcpbo.customer.audit.collector.AdditionalDataHolder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.rhbgroup.dcp.data.entity.profiles.UserProfile;
import com.rhbgroup.dcp.data.repository.ProfileRepository;
import com.rhbgroup.dcp.deposits.mca.bizlogic.GetDepositsMcaTransaction;
import com.rhbgroup.dcp.deposits.mca.model.DcpMcaTransactionPaginationRequest;
import com.rhbgroup.dcp.deposits.mca.model.DcpMcaTransactionRequest;
import com.rhbgroup.dcp.model.Capsule;
import com.rhbgroup.dcp.model.Constants;
import com.rhbgroup.dcp.model.SourceChannel;
import com.rhbgroup.dcp.util.JsonUtil;
import com.rhbgroup.dcpbo.customer.contract.BoData;
import com.rhbgroup.dcpbo.customer.dto.DepositMcaTransaction;
import com.rhbgroup.dcpbo.customer.exception.CommonException;
import com.rhbgroup.dcpbo.customer.service.McaCallTransactionsService;

@Service
public class McaCallTransactionsServiceImpl implements McaCallTransactionsService {

	@Autowired
	private ProfileRepository profileRepository;

	@Autowired
	private GetDepositsMcaTransaction getDepositsMcaTransaction;

	@Autowired
	private AdditionalDataHolder additionalDataHolder;

	private static Logger logger = LogManager.getLogger(McaCallTransactionsServiceImpl.class);

	@Override
	public BoData getMcaCallTransactions(Integer customerId, String accountNo, String foreignCurrency,
			Integer pageCounter, String firstKey, String lastKey) {
		logger.debug("getMcaCallTransactions()");
		logger.debug("    customerId: " + customerId);
		logger.debug("    accountNo: " + accountNo);
		logger.debug("    foreignCurrency: " + foreignCurrency);
		logger.debug("    pageCounter: " + pageCounter);
		logger.debug("    firstKey: " + firstKey);
		logger.debug("    lastKey: " + lastKey);

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

		DcpMcaTransactionPaginationRequest pagination = new DcpMcaTransactionPaginationRequest();
		pagination.setFirstKey(firstKey);
		pagination.setLastKey(lastKey);
		pagination.setPageCounter(pageCounter);

		DcpMcaTransactionRequest dcpMcaTransactionRequest = new DcpMcaTransactionRequest();
		dcpMcaTransactionRequest.setAccountNo(accountNo);
		dcpMcaTransactionRequest.setForeignCurrency(foreignCurrency);
		dcpMcaTransactionRequest.setPagination(pagination);

		String jsonStr = JsonUtil.objectToJson(dcpMcaTransactionRequest);
		logger.debug("    jsonStr: " + jsonStr);

		Capsule capsule = new Capsule();
		capsule.setCisNumber(cisNo);
		capsule.setUserId(customerId);
		capsule.setProperty(Constants.SOURCE_CHANNEL, SourceChannel.DCP_MBK);
		capsule.setMessageId(UUID.randomUUID().toString());
		capsule.setProperty(Constants.OPERATION_NAME, "GetDepositMcaTransaction");
		capsule.updateCurrentMessage(jsonStr);
		logger.debug("    capsule: " + capsule);

		capsule = getDepositsMcaTransaction.executeBusinessLogic(capsule);
		logger.debug("    capsule: " + capsule);
		logger.debug("        isOperationSuccesful: " + capsule.isOperationSuccessful());

		if (capsule.isOperationSuccessful() == null || !capsule.isOperationSuccessful())
			throw new CommonException(CommonException.GENERIC_ERROR_CODE,
					"Error calling " + GetDepositsMcaTransaction.class.getSimpleName() + ".executeBusinessLogic()");

		jsonStr = capsule.getCurrentMessage();
		logger.debug("        jsonStr: " + jsonStr);

		DepositMcaTransaction depositMcaTransaction = JsonUtil.jsonToObject(jsonStr, DepositMcaTransaction.class);

        HashMap<String, String> additionalData = new HashMap<>();
        additionalData.put("module", "Provide Assistance");
        additionalData.put("username", userProfile.getUsername());
        additionalData.put("customerId", userProfile.getIdNo());
        additionalData.put("accountNumber", accountNo);

        additionalDataHolder.setMap(additionalData);

		return depositMcaTransaction;
	}

}
