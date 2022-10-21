package com.rhbgroup.dcpbo.customer.service.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.rhbgroup.dcpbo.customer.audit.collector.AdditionalDataHolder;
import com.rhbgroup.dcpbo.customer.model.McaTermData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.rhbgroup.dcp.adaptor.BusinessAdaptor;
import com.rhbgroup.dcp.data.entity.profiles.UserProfile;
import com.rhbgroup.dcp.data.repository.ProfileRepository;
import com.rhbgroup.dcp.model.Capsule;
import com.rhbgroup.dcp.model.Constants;
import com.rhbgroup.dcp.model.SourceChannel;
import com.rhbgroup.dcp.util.JsonUtil;
import com.rhbgroup.dcpbo.customer.contract.BoData;
import com.rhbgroup.dcpbo.customer.exception.CommonException;
import com.rhbgroup.dcpbo.customer.model.McaTermDetails;
import com.rhbgroup.dcpbo.customer.service.McaTermService;
import com.rhbgroup.dcpbo.customer.vo.GetMcaTermLogicRequestVo;
import com.rhbgroup.dcpbo.customer.vo.McaTermsVo;

@Service
public class McaTermServiceImpl implements McaTermService {

	private static Logger logger = LogManager.getLogger(McaTermServiceImpl.class);

	@Autowired
	@Qualifier("profileRepository")
	private ProfileRepository profileRepository;

	private BusinessAdaptor getMcaTermDetailsLogic;
	
	private BusinessAdaptor getMcaTermLogic;

	@Autowired
	private AdditionalDataHolder additionalDataHolder;

	private static final String CONST_DCP_GET_MCA_TERM_DETAIL_LOGIC = "GetMcaTermDetailsLogic";
	private static final String CONST_DCP_GET_MCA_TERM_LOGIC = "GetMcaTermLogic";

	public McaTermServiceImpl(@Qualifier("mcaTermDetailsLogic") BusinessAdaptor getMcaTermDetailsLogic, 
			@Qualifier("getMcaTermLogic") BusinessAdaptor getMcaTermLogic) {
		super();
		this.getMcaTermDetailsLogic = getMcaTermDetailsLogic;
		this.getMcaTermLogic = getMcaTermLogic;
	}

	public BoData getMcaTermDetails(Integer customerId, String accountNo, String referenceNo, String currencyCode) {
		logger.debug("getMcaTermDetails()");

		int userId = customerId;

		logger.debug("accountNo: {}", accountNo);
		logger.debug("currencyCode: {}", currencyCode);
		logger.debug("referenceNo: {}", referenceNo);
		logger.debug("userId: {}", userId);

		UserProfile userProfile = profileRepository.getUserProfileByUserId(userId);
		if (userProfile == null) {
			throw new CommonException(CommonException.GENERIC_ERROR_CODE, "User Profile not found.");
		}

		String cisNo = userProfile.getCisNo();
		logger.debug("cisNo: {}", cisNo);

		Map<String, String> capsulePayload = new HashMap<>();

		capsulePayload.put("accountNo", accountNo);
		capsulePayload.put("currencyCode", currencyCode);
		capsulePayload.put("referenceNo", referenceNo);

		String jsonStr = JsonUtil.objectToJson(capsulePayload);
		logger.debug("jsonStr: {}", jsonStr);

		Capsule capsule = new Capsule();
		capsule.setUserId(userId);
		capsule.updateCurrentMessage(jsonStr);
		capsule.setCisNumber(cisNo);
		capsule.setProperty(Constants.SOURCE_CHANNEL, SourceChannel.DCP_MBK);
		capsule.setMessageId(UUID.randomUUID().toString());
		capsule.setProperty(Constants.OPERATION_NAME, CONST_DCP_GET_MCA_TERM_DETAIL_LOGIC);
		logger.debug("capsule before: {}", capsule);
		capsule = getMcaTermDetailsLogic.executeBusinessLogic(capsule);
		logger.debug("capsule after: {}", capsule);
		logger.debug("isOperationSuccesful: {}", capsule.isOperationSuccessful());
		if (capsule.isOperationSuccessful() == null || !capsule.isOperationSuccessful()) {
			throw new CommonException(CommonException.GENERIC_ERROR_CODE,
					"Error calling getDuitnowDetails.executeBusinessLogic()");
		}
		jsonStr = capsule.getCurrentMessage();
		logger.debug("jsonStr: {}", jsonStr);

		McaTermData mcaTermData = JsonUtil.jsonToObject(jsonStr, McaTermData.class);

		McaTermDetails mcaTermDetails = new McaTermDetails();
		mcaTermDetails.setData(mcaTermData);



		HashMap<String, String> additionalData = new HashMap<>();
		additionalData.put("module", "Provide Assistance");
		additionalData.put("username", userProfile.getUsername());
		additionalData.put("customerId", userProfile.getIdNo());
		additionalData.put("accountNumber", accountNo);

		additionalDataHolder.setMap(additionalData);

		return mcaTermDetails;
	}
	
	public BoData getMcaTerm(Integer customerId, GetMcaTermLogicRequestVo request) {

		int userId = customerId;
		
		logger.debug("getMcaTerm()");
		logger.debug("accountNo: {}", request.getAccountNo());
		logger.debug("currencyCode: {}", request.getCurrencyCode());
		logger.debug("userId: {}", userId);

		UserProfile userProfile = profileRepository.getUserProfileByUserId(userId);
		if (userProfile == null) {
			throw new CommonException(CommonException.GENERIC_ERROR_CODE, "User Profile not found.");
		}

		McaTermsVo mcaTermVo = new McaTermsVo();

		if (request.getCurrencyCode() != null && !request.getCurrencyCode().isEmpty()) {

			String cisNo = userProfile.getCisNo();
			logger.debug("cisNo: {}", cisNo);

			Map<String, Object> capsulePayload = new HashMap<>();

			capsulePayload.put("accountNo", request.getAccountNo());
			capsulePayload.put("currency", request.getCurrencyCode());

			String jsonStr = JsonUtil.objectToJson(capsulePayload);
			logger.debug("jsonStr: {}", jsonStr);

			Capsule capsule = new Capsule();
			capsule.setUserId(userId);
			capsule.updateCurrentMessage(jsonStr);
			capsule.setCisNumber(cisNo);
			capsule.setProperty(Constants.SOURCE_CHANNEL, SourceChannel.DCP_MBK);
			capsule.setMessageId(UUID.randomUUID().toString());
			capsule.setProperty(Constants.OPERATION_NAME, CONST_DCP_GET_MCA_TERM_LOGIC);

			logger.debug("capsule before: {}", capsule);
			capsule = getMcaTermLogic.executeBusinessLogic(capsule);
			logger.debug("capsule after: {}", capsule);

			logger.debug("isOperationSuccesful: {}", capsule.isOperationSuccessful());
			if (capsule.isOperationSuccessful() == null || !capsule.isOperationSuccessful()) {
				throw new CommonException(CommonException.GENERIC_ERROR_CODE,
						"Error calling getMcaTermLogic.executeBusinessLogic()");
			}
			jsonStr = capsule.getCurrentMessage();
			logger.debug("jsonStr: {}", jsonStr);

			mcaTermVo = JsonUtil.jsonToObject(jsonStr, McaTermsVo.class);
			
		} else {
			throw new CommonException(CommonException.GENERIC_ERROR_CODE, "Currency Code cannot be null or empty.");
		}

		HashMap<String, String> additionalData = new HashMap<>();
		additionalData.put("module", "Provide Assistance");
		additionalData.put("username", userProfile.getUsername());
		additionalData.put("customerId", userProfile.getIdNo());
		additionalData.put("accountNumber", request.getAccountNo());

		additionalDataHolder.setMap(additionalData);

		return mcaTermVo;

	}
}
