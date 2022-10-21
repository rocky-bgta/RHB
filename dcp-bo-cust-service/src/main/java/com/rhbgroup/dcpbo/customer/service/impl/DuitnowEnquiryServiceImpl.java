package com.rhbgroup.dcpbo.customer.service.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.rhbgroup.dcpbo.customer.audit.collector.AdditionalDataHolder;
import com.rhbgroup.dcpbo.customer.model.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.rhbgroup.dcp.adaptor.BusinessAdaptor;
import com.rhbgroup.dcp.data.entity.profiles.UserProfile;
import com.rhbgroup.dcp.data.entity.transfers.NADStatistic;
import com.rhbgroup.dcp.data.repository.CommonRepository;
import com.rhbgroup.dcp.data.repository.ProfileRepository;
import com.rhbgroup.dcp.data.repository.TransferRepository;
import com.rhbgroup.dcp.model.Capsule;
import com.rhbgroup.dcp.model.Constants;
import com.rhbgroup.dcp.model.SourceChannel;
import com.rhbgroup.dcp.profiles.model.DcpGetDuitnowEnquiryRequest;
import com.rhbgroup.dcp.util.JsonUtil;
import com.rhbgroup.dcpbo.customer.contract.BoData;
import com.rhbgroup.dcpbo.customer.enums.FundTransferMainFunctionType;
import com.rhbgroup.dcpbo.customer.exception.CommonException;
import com.rhbgroup.dcpbo.customer.service.DuitnowEnquiryService;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class DuitnowEnquiryServiceImpl implements DuitnowEnquiryService {

	private BusinessAdaptor getDuitnowSendersLogic;

	private BusinessAdaptor getDuitnowEnquiryLogic;

	@Autowired
	@Qualifier(value = "profileRepository")
	private ProfileRepository profileRepository;
	
	@Autowired
	@Qualifier(value = "transferRepository")
	private TransferRepository transferRepository;
	
	@Autowired
	@Qualifier(value = "commonRepository")
	private CommonRepository commonRepository;

	@Value("${dcp.stardate.url}")
	private String dcpStardate;

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    private AdditionalDataHolder additionalDataHolder;

    private static Logger logger = LogManager.getLogger(DuitnowEnquiryServiceImpl.class);

	private static final String CONST_DCP_DUITNOW_SENDER_LOGIC = "GetDuitNowSendersLogic";
	private static final String CONST_DCP_DUITNOW_ENQ_LOGIC = "GetDuitnowEnquiryLogic";
	//private static final String CONST_DCP_DUITNOW_MAIN_FUNCTION = "DUITNOW";
	private static final String CONST_DCP_DUITNOW_NAD_MAX_ABUSE_COUNTER = "nad.max.abuse.counter";

	public DuitnowEnquiryServiceImpl(@Qualifier("duitnowSendersLogic") BusinessAdaptor getDuitNowSendersLogic,
			@Qualifier("duitnowEnquiryLogic") BusinessAdaptor getDuitnowEnquiryLogic) {
		super();
		this.getDuitnowSendersLogic = getDuitNowSendersLogic;
		this.getDuitnowEnquiryLogic = getDuitnowEnquiryLogic;
	}

	/*
	 * This is implemented as part of DCPBL-13539 user story
	 */

	public BoData getDuitnowDetails(Integer customerId) {
		DuitnowDetail duitnowDetail = new DuitnowDetail();
		logger.debug("getDuitnowDetails customerId : {}", customerId);
		UserProfile userProfile = profileRepository.getUserProfileByUserId(customerId);
		String referenceId = generateReferenceId();
        String duitNowSenderJsonStr = getDcpDuitnowSenderInfo(customerId, userProfile, referenceId);

		DuitnowSenderInfo duitnowSenderInfo = JsonUtil.jsonToObject(duitNowSenderJsonStr, DuitnowSenderInfo.class);
		logger.debug("duitnowSender: {}", duitnowSenderInfo);
		logger.debug("idNumber: {}", duitnowSenderInfo.getIdNumber());
		logger.debug("idType: {}", duitnowSenderInfo.getIdType());
		logger.debug("otpMobileNumber: {}", duitnowSenderInfo.getOtpMobileNumber());
		duitnowDetail.setSenders(duitnowSenderInfo);
		String duitNowEnquiryJsonStr = getDcpDuitnowEnquiry(customerId, duitnowSenderInfo.getIdNumber(),
				duitnowSenderInfo.getIdType(), duitnowSenderInfo.getOtpMobileNumber(), userProfile);
		DuitnowProxyInfo DuitnowProxyInfo = JsonUtil.jsonToObject(duitNowEnquiryJsonStr, DuitnowProxyInfo.class);
		logger.debug("duitnowEnquiry: {}", DuitnowProxyInfo);
		duitnowDetail.setProxy(DuitnowProxyInfo);

        HashMap<String, String> additionalData = new HashMap<>();
        additionalData.put("Module", "Provide Assistance");
        additionalData.put("Username", userProfile.getUsername());
        additionalData.put("Customer ID", userProfile.getIdNo());

        additionalDataHolder.setMap(additionalData);

		return duitnowDetail;
	}

    private String getDcpDuitnowSenderInfo(Integer userId, UserProfile userProfile, String referenceId) {
		Map<String, String> jsonBody = new HashMap<>();

		jsonBody.put("userId", String.valueOf(userId));
		
		String jsonStr = JsonUtil.objectToJson(jsonBody);
		logger.debug("getDcpDuitnowSenderInfo rq : {}", jsonStr);
		Capsule capsule = new Capsule();
		capsule.setProperty(Constants.SOURCE_CHANNEL, SourceChannel.DCP_MBK);
		capsule.setMessageId(UUID.randomUUID().toString());
		capsule.setProperty(Constants.OPERATION_NAME, CONST_DCP_DUITNOW_SENDER_LOGIC);
		capsule.updateCurrentMessage(jsonStr);
		capsule.setUserId(userProfile.getId());
		capsule.setCisNumber(userProfile.getCisNo());
		capsule.setReferenceId(referenceId);
		logger.debug("capsule: {}", capsule);
		capsule = getDuitnowSendersLogic.executeBusinessLogic(capsule);
		logger.debug("capsule: {}", capsule);
		logger.debug("isOperationSuccesful: {}", capsule.isOperationSuccessful());
		if (capsule.isOperationSuccessful() == null || !capsule.isOperationSuccessful())
			throw new CommonException(CommonException.GENERIC_ERROR_CODE,
					"Error calling getDuitnowDetails.executeBusinessLogic()");
		
		//jsonStr = capsule.getCurrentMessage();
		
		// senders.nadEnquiryStatus
		// nad_lookup_count, transfer_completion_count, add_fav_completion_count -> tbl_nad_statistic
		// nad.max.abuse.counter -> parameter key at tbl_app_config
		String nadEnquiryStatus = "Disabled";
		NADStatistic nadStatistic = transferRepository.getTodayNADStatisticByUserId(userId, FundTransferMainFunctionType.DUITNOW.toString());
		int nadMaxAbuseCounter = Integer.parseInt(commonRepository.getAppConfigLookup(CONST_DCP_DUITNOW_NAD_MAX_ABUSE_COUNTER));
		if(nadMaxAbuseCounter == 0) {
			nadEnquiryStatus = "Disabled";
		} else if(nadMaxAbuseCounter < 0) {
			nadEnquiryStatus = "Enable";
		} else {
			// nadMaxAbuseCounter > 0
			// If (nad_lookup_count - transfer_completion_count - add_fav_completion_count) >= nad.max.abuse.counter, return "Disabled" else return "Enable"
			if(nadStatistic != null) {
				if((nadStatistic.getNadLookupCount() - nadStatistic.getTransferCompletionCount() - nadStatistic.getAddFavCompletionCount()) >= nadMaxAbuseCounter) {
					nadEnquiryStatus = "Disabled";
				} else {
					nadEnquiryStatus = "Enable";
				}
			} else {
				nadEnquiryStatus = "Enable";
			}
		}
		
		// Append nadEnquiryStatus
		Map<String, Object> jsonBodyRes = JsonUtil.jsonToMap(capsule.getCurrentMessage());		
		jsonBodyRes.put("nadEnquiryStatus", nadEnquiryStatus);
		jsonStr = JsonUtil.objectToJson(jsonBodyRes);
				
		logger.debug("getDcpDuitnowSenderInfo rs: {}", jsonStr);
		return jsonStr;
	}

	private String getDcpDuitnowEnquiry(Integer userId, String duitnowIdNo, String duitnowIdType,
			String otpMobileNumber, UserProfile userProfile) {

		DcpGetDuitnowEnquiryRequest dcpGetDuitnowEnquiryRequest = new DcpGetDuitnowEnquiryRequest();
		dcpGetDuitnowEnquiryRequest.setDuitnowIdNo(duitnowIdNo);
		dcpGetDuitnowEnquiryRequest.setDuitnowIdType(duitnowIdType);
		dcpGetDuitnowEnquiryRequest.setOtpMobileNumber(otpMobileNumber);
		String jsonStr = JsonUtil.objectToJson(dcpGetDuitnowEnquiryRequest);
		logger.debug("getDcpDuitnowEnquiry rq: {}", jsonStr);
		Capsule capsule = new Capsule();
		capsule.setProperty(Constants.SOURCE_CHANNEL, SourceChannel.DCP_MBK);
		capsule.setMessageId(UUID.randomUUID().toString());
		capsule.setProperty(Constants.OPERATION_NAME, CONST_DCP_DUITNOW_ENQ_LOGIC);
		capsule.setUserId(userId);
		capsule.updateCurrentMessage(jsonStr);
		capsule.setUserId(userProfile.getId());
		capsule.setCisNumber(userProfile.getCisNo());
		capsule.setReferenceId(generateReferenceId());
		logger.debug("capsule: {}", capsule);
		capsule = getDuitnowEnquiryLogic.executeBusinessLogic(capsule);
		logger.debug("capsule: {}", capsule);
		logger.debug("isOperationSuccesful: {}", capsule.isOperationSuccessful());
		if (capsule.isOperationSuccessful() == null || !capsule.isOperationSuccessful())
			throw new CommonException(CommonException.GENERIC_ERROR_CODE,
					"Error calling getDuitnowDetails.executeBusinessLogic()");

		jsonStr = capsule.getCurrentMessage();
		logger.debug("getDcpDuitnowEnquiry rs : {}", jsonStr);
		return jsonStr;
	}

	public String generateReferenceId(){
		try {
			String dcpStardateServiceUrl = dcpStardate;

			UriComponentsBuilder builder = UriComponentsBuilder
					.fromUriString( dcpStardateServiceUrl );

			String response = restTemplate.getForObject( builder.toUriString(), String.class );
			return response;
		}catch(Exception e){
			throw new CommonException(CommonException.GENERIC_ERROR_CODE,
						"Error in generating referenceId ");
		}
	}

}
