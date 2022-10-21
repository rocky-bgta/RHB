package com.rhbgroup.dcpbo.customer.service.impl;

import java.util.UUID;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.rhbgroup.dcp.creditcards.bizlogic.GetCreditCardDetailsLogic;
import com.rhbgroup.dcp.data.entity.profiles.UserProfile;
import com.rhbgroup.dcp.data.repository.ProfileRepository;
import com.rhbgroup.dcp.model.Capsule;
import com.rhbgroup.dcp.model.Constants;
import com.rhbgroup.dcp.model.SourceChannel;
import com.rhbgroup.dcp.util.JsonUtil;
import com.rhbgroup.dcpbo.customer.contract.BoData;
import com.rhbgroup.dcpbo.customer.exception.CommonException;
import com.rhbgroup.dcpbo.customer.model.CreditCardDetails;
import com.rhbgroup.dcpbo.customer.service.CardDetailsService;
import com.rhbgroup.dcpbo.customer.dto.CreditCardDetailsRequest;


@Service
public class CardDetailsServiceImpl implements CardDetailsService{

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private GetCreditCardDetailsLogic getCreditCardDetailsLogic;

    private static Logger logger = LogManager.getLogger(CardDetailsService.class);

    /*
     * This is implemented as part of DCPBL-8743 user story
     */
    public BoData getCardDetails(
            Integer customerId,
            String cardNo,
            String channelFlag,
            String connectorCode,
            String blockCode,
            String accountBlockCode) {
        logger.debug("getCardDetails()");
        logger.debug("    profileRepository: " + profileRepository);

		/*
		 * Get cisNo from DCP ProfileRepository. In DCP, customerId is userId.
		 */
        int userId = customerId;
        UserProfile userProfile = profileRepository.getUserProfileByUserId(userId);
        logger.debug("    userProfile: " + userProfile);
        if (userProfile == null)
            throw new CommonException(CommonException.GENERIC_ERROR_CODE, "Unable to find user detail");

        String cisNo = userProfile.getCisNo();
        logger.debug("    cisNo: " + cisNo);


        logger.debug("    cardNo: " + cardNo);
        
        CreditCardDetailsRequest creditCardDetailsRequest = new CreditCardDetailsRequest();
        creditCardDetailsRequest.setCardNo(cardNo);
        creditCardDetailsRequest.setChannelFlag(channelFlag);
        creditCardDetailsRequest.setConnectorCode(connectorCode);
        
        if(blockCode!=null && !blockCode.equals("empty")) {
                creditCardDetailsRequest.setBlockCode(blockCode);
        }
        
        if(accountBlockCode!=null && !accountBlockCode.equals("empty")) {
                creditCardDetailsRequest.setAccountBlockCode(accountBlockCode);
        }
        
        String jsonStr = JsonUtil.objectToJson(creditCardDetailsRequest);
        logger.debug("    jsonStr: " + jsonStr);

        Capsule capsule = new Capsule();
        capsule.setCisNumber(cisNo);
        capsule.setUserId(customerId);
        capsule.setProperty(Constants.SOURCE_CHANNEL, SourceChannel.DCP_MBK);
        capsule.setMessageId(UUID.randomUUID().toString());
        capsule.setProperty(Constants.OPERATION_NAME, "GetCreditCardDetails");
        capsule.updateCurrentMessage(jsonStr);
        logger.debug("    capsule: " + capsule);

        capsule = getCreditCardDetailsLogic.executeBusinessLogic(capsule);
        logger.debug("    capsule: " + capsule);
        logger.debug("        isOperationSuccesful: " + capsule.isOperationSuccessful());

        if (capsule.isOperationSuccessful() == null || !capsule.isOperationSuccessful())
        	throw new CommonException(CommonException.GENERIC_ERROR_CODE, "Error calling GetCreditCardDetailsLogic.executeBusinessLogic()");
        
        jsonStr = capsule.getCurrentMessage();
        logger.debug("        jsonStr: " + jsonStr);

        CreditCardDetails creditCardDetails = JsonUtil.jsonToObject(jsonStr, CreditCardDetails.class);
        logger.debug("    creditCardDetails: " + creditCardDetails);

        return creditCardDetails;
    }
}
