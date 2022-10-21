package com.rhbgroup.dcpbo.customer.service.impl;

import com.rhbgroup.dcp.data.entity.profiles.UserProfile;
import com.rhbgroup.dcp.data.repository.ProfileRepository;
import com.rhbgroup.dcpbo.customer.contract.BoData;
import com.rhbgroup.dcpbo.customer.dto.CustomerProfile;
import com.rhbgroup.dcpbo.customer.exception.CommonException;
import com.rhbgroup.dcpbo.customer.model.AppConfig;
import com.rhbgroup.dcpbo.customer.model.BoLookUp;
import com.rhbgroup.dcpbo.customer.model.CardProduct;
import com.rhbgroup.dcpbo.customer.model.CardProfile;
import com.rhbgroup.dcpbo.customer.repository.AppConfigRepository;
import com.rhbgroup.dcpbo.customer.repository.BoLookupRepository;
import com.rhbgroup.dcpbo.customer.repository.CardProductRepository;
import com.rhbgroup.dcpbo.customer.repository.CardProfileRepository;
import com.rhbgroup.dcpbo.customer.service.CustomerProfileService;
import com.rhbgroup.dcpbo.customer.utils.CustomerServiceConstant;
import com.rhbgroup.dcpbo.customer.vo.CustomerProfileVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CustomerProfileServiceImpl implements CustomerProfileService {

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    BoLookupRepository bolookupRepository;

    @Autowired
    AppConfigRepository appConfigRepository;

    @Autowired
    CardProfileRepository cardProfileRepository;

    @Autowired
    CardProductRepository cardProductRepository;

    @Override
    public BoData getCustomerProfile(Integer customerId) {
        List<CustomerProfile> list = new ArrayList<>();
        CustomerProfileVo voData = new CustomerProfileVo();
        List<CardProfile> cardProfile = null;
        if (customerId == null) {
            throw new CommonException(CommonException.GENERIC_ERROR_CODE, "CustomerId is Empty:" + customerId);
        }
        UserProfile user = profileRepository.getUserProfileByUserId(customerId);

        if (user == null) {
            throw new CommonException(CommonException.GENERIC_ERROR_CODE, "Cannot find user profile data for CustomerId");
        }
        if (customerId.equals(user.getId())) {
            BoLookUp bolookUp = bolookupRepository.getLookUp(CustomerServiceConstant.ACTION_REQUIRED, user.getUserStatus());

            if (bolookUp != null) {
                CustomerProfile responseData = new CustomerProfile();
                responseData.setTitleEn(bolookUp.getTitleEn());
                responseData.setDescriptionEn(bolookUp.getDescriptionEn());
                responseData.setButton(bolookUp.getButtonEn());
                responseData.setActionURL(getActionURL(bolookUp, null, user));
                list.add(responseData);
            } else {
                bolookUp = bolookupRepository.getLookUp(CustomerServiceConstant.ACTION_REQUIRED, CustomerServiceConstant.TPIN_CODE);

            }
            AppConfig appConfig = appConfigRepository.getParameterValue(CustomerServiceConstant.CHANGE_PIN_COUNT);
            if (appConfig != null) {
                cardProfile = cardProfileRepository.getCardProfile(Integer.valueOf(appConfig.getParameterValue()), customerId);
                if (cardProfile != null) {
                    for (CardProfile crdPrfle : cardProfile) {
                        CustomerProfile responseData = new CustomerProfile();
                        CardProduct cardProduct = cardProductRepository.getProductCategory(crdPrfle.getCardNo());
                        String cardType = "";
                        if (cardProduct != null && (cardProduct.getCategory().equals(CustomerServiceConstant.DEBIT_CARD) || cardProduct.getCategory().equals(CustomerServiceConstant.PREPAID_CARD) || cardProduct.getCategory().equals(CustomerServiceConstant.CREDIT_CARD))) {
                            cardType = cardProduct.getCategory();
                        }
                        responseData.setCardType(cardType);
                        responseData.setTitleEn(bolookUp.getTitleEn());
                        responseData.setDescriptionEn(bolookUp.getDescriptionEn());
                        responseData.setButton(bolookUp.getButtonEn());
                        responseData.setCardNo(crdPrfle.getCardNo());
                        responseData.setActionURL(getActionURL(bolookUp, crdPrfle, user));
                        list.add(responseData);
                    }
                }
            }
        }
        voData.setActions(list);
        return voData;
    }

    public String getActionURL(BoLookUp bolookUp, CardProfile crdPrfle, UserProfile user) {
        String actionUrl = "";

        if (bolookUp == null || crdPrfle == null || user == null) {
            return actionUrl;
        }

        if (bolookUp.getCode().equals(CustomerServiceConstant.L_CODE) || bolookUp.getCode().equals(CustomerServiceConstant.C_CODE)) {
            actionUrl = CustomerServiceConstant.ACTION_URL + user.getId() + "/" + bolookUp.getCode();
        } else if (bolookUp.getType().equals(CustomerServiceConstant.ACTION_REQUIRED) && bolookUp.getCode().equals(CustomerServiceConstant.TPIN_CODE)) {
            actionUrl = CustomerServiceConstant.ACTION_URL + crdPrfle.getId() + "/" + bolookUp.getCode();
        }

        return actionUrl;
    }
}
