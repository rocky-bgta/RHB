package com.rhbgroup.dcpbo.customer.service.impl;

import com.rhbgroup.dcp.data.entity.profiles.UserProfile;
import com.rhbgroup.dcp.data.repository.ProfileRepository;
import com.rhbgroup.dcp.model.Capsule;
import com.rhbgroup.dcp.model.Constants;
import com.rhbgroup.dcp.model.SourceChannel;
import com.rhbgroup.dcp.uber.deposits.casa.bizlogic.GetTermDepositDetailsLogic;
import com.rhbgroup.dcp.util.JsonUtil;
import com.rhbgroup.dcpbo.customer.contract.BoData;
import com.rhbgroup.dcpbo.customer.dto.TermDepositDetails;
import com.rhbgroup.dcpbo.customer.repository.BoDepositRepository;
import com.rhbgroup.dcpbo.customer.service.ViewDepositService;

import java.util.HashMap;
import java.util.Map;

public class ViewTermDepositServiceImpl implements ViewDepositService {

    private BoDepositRepository boDepositRepository;
    private GetTermDepositDetailsLogic getTermDepositDetailsLogic;
    private ProfileRepository profileRepository;

    public ViewTermDepositServiceImpl(BoDepositRepository boDepositRepository,
                                      GetTermDepositDetailsLogic getTermDepositDetailsLogic,
                                      ProfileRepository profileRepository) {
        this.boDepositRepository = boDepositRepository;
        this.getTermDepositDetailsLogic = getTermDepositDetailsLogic;
        this.profileRepository = profileRepository;
    }

    @Override
    public BoData detail(int customerId, String accountNo) {

        Capsule requestCapsule = new Capsule();
        Map<String,Object> dcpPayload = new HashMap<>();
        dcpPayload.put("accountNo", accountNo);

        UserProfile userProfile = profileRepository.getUserProfileByUserId(customerId);
        requestCapsule.setCisNumber(userProfile.getCisNo());

        requestCapsule.updateCurrentMessage(JsonUtil.objectToJson(dcpPayload));
        requestCapsule.setUserId(customerId);
        requestCapsule.setUserName(userProfile.getUsername());
        requestCapsule.setProperty(Constants.SOURCE_CHANNEL, SourceChannel.DCP_MBK);

        Capsule responseCapsule = getTermDepositDetailsLogic.executeBusinessLogic(requestCapsule);

        return JsonUtil.jsonToObject(responseCapsule.getCurrentMessage(), TermDepositDetails.class);
    }
}
