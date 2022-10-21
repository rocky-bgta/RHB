package com.rhbgroup.dcpbo.customer.service.impl;

import java.util.HashMap;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.rhbgroup.dcpbo.customer.audit.collector.AdditionalDataHolder;
import com.rhbgroup.dcpbo.customer.contract.BoData;
import com.rhbgroup.dcpbo.customer.dto.ResetUserNameResponse;
import com.rhbgroup.dcpbo.customer.model.UserProfile;
import com.rhbgroup.dcpbo.customer.repository.CardProfileRepository;
import com.rhbgroup.dcpbo.customer.repository.UserProfileRepository;
import com.rhbgroup.dcpbo.customer.service.ResetUserNameService;

@Service
public class ResetUserNameServiceImpl implements ResetUserNameService {

	@Autowired
	private UserProfileRepository userProfileRepository;
	
	@Autowired
	private CardProfileRepository cardProfileRepository;

	private static Logger logger = LogManager.getLogger(ResetUserNameServiceImpl.class);

	private static final String LOCKEDREASON = "lockedReason";
	
	private static final String LOCKEDREASONL = "Reset password counter (wrong more than 3 attempts)";
	private static final String LOCKEDREASONC = "Reset challenge question counter (wrong more than 3 attempts)";
	private static final String LOCKEDREASONTPIN = "Reset card T-PIN counter (wrong more than 3 attempts)";
	
	private static final String STATUSTITLE = "Username Successfully Reset";
	private static final String STATUSTITLETPIN = "Card T-PIN Counter Successfully Reset";
	private static final String STATUSDESC = "User can login to DCP now";
	private static final String STATUSDESCTPIN = "User can continue to set their card PIN now";

	@Autowired
	private AdditionalDataHolder additionalDataHolder;

	@Override
	public BoData resetUserName(String id, String code) {
		logger.info("Inside resetUserName()");
		HashMap<String, String> additionalData = new HashMap<>();

		UserProfile userProfile = userProfileRepository.getUserProfile(Integer.valueOf(id));
		String username = userProfile.getUsername();
		String name = userProfile.getName();
		String idNo = userProfile.getIdNo();

		additionalData.put("username", username);
		additionalData.put("name", name);
		additionalData.put("userStatus", "A");
		additionalData.put("nric", idNo);

		ResetUserNameResponse resetUserNameResponse = new ResetUserNameResponse();
		
		if (code.equals("L")) {
			userProfileRepository.updateFailedLoginCount(Integer.valueOf(id));
			additionalData.put(LOCKEDREASON, LOCKEDREASONL);
			resetUserNameResponse.setStatusTitle(STATUSTITLE);
			resetUserNameResponse.setStatusDesc(STATUSDESC);
		} else if(code.equals("C")) {
			userProfileRepository.updateFailedChallengeCount(Integer.valueOf(id));
			additionalData.put(LOCKEDREASON, LOCKEDREASONC);
			resetUserNameResponse.setStatusTitle(STATUSTITLE);
			resetUserNameResponse.setStatusDesc(STATUSDESC);
		} else if(code.equals("TPIN")) {
			cardProfileRepository.updateFailedCardTpinCount(Integer.valueOf(id));
			additionalData.put(LOCKEDREASON, LOCKEDREASONTPIN);
			resetUserNameResponse.setStatusTitle(STATUSTITLETPIN);
			resetUserNameResponse.setStatusDesc(STATUSDESCTPIN);
		}
		
		additionalDataHolder.setMap(additionalData);
		return resetUserNameResponse;
	}

}
