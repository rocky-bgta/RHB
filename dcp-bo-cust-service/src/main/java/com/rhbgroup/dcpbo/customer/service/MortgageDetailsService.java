package com.rhbgroup.dcpbo.customer.service;

import com.rhbgroup.dcp.data.entity.profiles.UserProfile;
import com.rhbgroup.dcp.data.repository.ProfileRepository;
import com.rhbgroup.dcp.loans.bizlogic.GetMortgageDetailsLogic;
import com.rhbgroup.dcp.loans.model.LoanAccount;
import com.rhbgroup.dcp.model.Capsule;
import com.rhbgroup.dcp.model.Constants;
import com.rhbgroup.dcp.model.SourceChannel;
import com.rhbgroup.dcp.util.JsonUtil;
import com.rhbgroup.dcpbo.customer.contract.BoData;
import com.rhbgroup.dcpbo.customer.exception.CommonException;
import com.rhbgroup.dcpbo.customer.model.MortgageDetails;
import com.rhbgroup.dcpbo.customer.repository.MortgageProfileRepository;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.UUID;

@Service
public class MortgageDetailsService {
    private static Logger logger = LogManager.getLogger(MortgageDetailsService.class);
    @Autowired
    private MortgageProfileRepository mortgageProfileRepository;
    @Autowired
    private GetMortgageDetailsLogic getMortgageDetailsLogic;
    @Autowired
    private ProfileRepository profileRepository;

    /*
     * This is implemented as part of DCPBL-8723 user story
     */
    public BoData getMortgageDetails(
            @RequestHeader("customerId") Integer customerId,
            String accountNo) {
        logger.debug("getMortgageDetails()");
        logger.debug("    mortgageProfileRepository: " + mortgageProfileRepository);

        /*
         * In DCP, customerId is userId.
         */
        int userId = customerId;

        logger.debug("    accountNo: " + accountNo);

        LoanAccount mortgageAccount = new LoanAccount();
        mortgageAccount.setAccountNo(accountNo);
        String jsonStr = JsonUtil.objectToJson(mortgageAccount);
        logger.debug("    jsonStr: " + jsonStr);

        UserProfile userProfile = profileRepository.getUserProfileByUserId(userId);

        Capsule capsule = new Capsule();
        capsule.setUserId(userId);
        capsule.setUserId(customerId);
        capsule.setCisNumber(userProfile.getCisNo());
        capsule.setProperty(Constants.SOURCE_CHANNEL, SourceChannel.DCP_MBK);
        capsule.setMessageId(UUID.randomUUID().toString());
        capsule.setProperty(Constants.OPERATION_NAME, "GetMortgageDetails");
        capsule.updateCurrentMessage(jsonStr);
        logger.debug("    capsule: " + capsule);

        capsule = getMortgageDetailsLogic.executeBusinessLogic(capsule);
        logger.debug("    capsule: " + capsule);
        logger.debug("        isOperationSuccesful: " + capsule.isOperationSuccessful());

        if (capsule.isOperationSuccessful() == null || !capsule.isOperationSuccessful())
            throw new CommonException(CommonException.GENERIC_ERROR_CODE, "Error calling GetMortgageDetailsLogic.executeBusinessLogic()");

        jsonStr = capsule.getCurrentMessage();
        logger.debug("        jsonStr: " + jsonStr);

        MortgageDetails mortgageDetails = JsonUtil.jsonToObject(jsonStr, MortgageDetails.class);
        logger.debug("    mortgageDetails: " + mortgageDetails);

        return mortgageDetails;
    }
}
