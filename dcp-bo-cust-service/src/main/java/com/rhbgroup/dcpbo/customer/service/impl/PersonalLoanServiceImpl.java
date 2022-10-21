package com.rhbgroup.dcpbo.customer.service.impl;

import java.util.UUID;

import com.rhbgroup.dcpbo.customer.contract.BoData;
import com.rhbgroup.dcpbo.customer.model.MortgageDetails;
import com.rhbgroup.dcpbo.customer.model.PersonalLoan;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestHeader;

import com.rhbgroup.dcp.data.entity.profiles.UserProfile;
import com.rhbgroup.dcp.data.repository.ProfileRepository;
import com.rhbgroup.dcp.loans.bizlogic.GetPersonalFinanceAccountDetailsLogic;
import com.rhbgroup.dcp.loans.model.LoanAccount;
import com.rhbgroup.dcp.model.Capsule;
import com.rhbgroup.dcp.model.Constants;
import com.rhbgroup.dcp.model.SourceChannel;
import com.rhbgroup.dcp.util.JsonUtil;
import com.rhbgroup.dcpbo.customer.exception.CommonException;
import com.rhbgroup.dcpbo.customer.service.PersonalLoanService;

@Service
public class PersonalLoanServiceImpl implements PersonalLoanService {

	private static Logger logger = LogManager.getLogger(PersonalLoanServiceImpl.class);

	@Autowired
	private ProfileRepository profileRepository;

	@Autowired
	private GetPersonalFinanceAccountDetailsLogic getPersonalFinanceAccountDetailsLogic;

	public BoData getPersonalLoanDetails(@RequestHeader("customerId") Integer customerId, String accountNo)
			throws CommonException {
		logger.debug("getPersonalLoanDetails()");

		int userId = customerId;


		UserProfile userProfile = profileRepository.getUserProfileByUserId(userId);
		if (userProfile == null) {
			throw new CommonException(CommonException.GENERIC_ERROR_CODE, "User Profile not found.");
		}

		String cisNo = userProfile.getCisNo();
		logger.debug("cisNo: " + cisNo);
		
		logger.debug("accountNo: " + accountNo);

		LoanAccount asbAccount = new LoanAccount();
		asbAccount.setAccountNo(accountNo);
		String jsonStr = JsonUtil.objectToJson(asbAccount);
		logger.debug("jsonStr: " + jsonStr);

		Capsule capsule = new Capsule();
		capsule.setUserId(userId);
		capsule.updateCurrentMessage(jsonStr);
		logger.debug("capsule: " + capsule);

		capsule.setCisNumber(cisNo);
		capsule.setProperty(Constants.SOURCE_CHANNEL, SourceChannel.DCP_MBK);

		capsule.setMessageId(UUID.randomUUID().toString());
		capsule.setProperty(Constants.OPERATION_NAME, "GetPersonalFinanceAccountDetails");

		capsule = getPersonalFinanceAccountDetailsLogic.executeBusinessLogic(capsule);
		logger.debug("capsule: " + capsule);

		jsonStr = capsule.getCurrentMessage();
		logger.debug("jsonStr: " + jsonStr);

		PersonalLoan personalLoan = JsonUtil.jsonToObject(jsonStr, PersonalLoan.class);

		return personalLoan;
	}
}
