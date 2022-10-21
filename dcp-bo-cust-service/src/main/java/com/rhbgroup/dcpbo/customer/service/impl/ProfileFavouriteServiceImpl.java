package com.rhbgroup.dcpbo.customer.service.impl;

import com.rhbgroup.dcpbo.customer.enums.FavouriteToIdTypeDesc;
import com.rhbgroup.dcpbo.customer.exception.CommonException;
import com.rhbgroup.dcpbo.customer.model.*;
import com.rhbgroup.dcpbo.customer.repository.*;
import com.rhbgroup.dcpbo.customer.service.ProfileFavouriteService;
import com.rhbgroup.dcpbo.customer.vo.ProfileFavouriteListVo;
import com.rhbgroup.dcpbo.customer.vo.ProfileFavouriteVo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ProfileFavouriteServiceImpl implements ProfileFavouriteService {

    private final Logger log = LogManager.getLogger(ProfileFavouriteServiceImpl.class);
    private final String INVEST = "INVEST";

    @Autowired
    ProfileFavouriteRepo profileFavouriteRepo;

    @Autowired
    UserProfileRepo userProfileRepo;

    @Autowired
    BankRepository bankRepo;

    @Autowired
    BillerRepo billerRepo;

    @Autowired
    TopupBillerRepo topupBillerRepo;

    @Autowired
    FundRepository fundRepository;

    public ProfileFavouriteListVo getProfileFavourites(String customerId) {
        Integer customerIdInt = 0;
        try {
            customerIdInt = Integer.parseInt(customerId);
        } catch (NumberFormatException e) {
            throw new CommonException("Invalid customer Id format.");
        }

        UserProfile userProfile = userProfileRepo.findOneById(customerIdInt);

        if (userProfile == null) {
            throw new CommonException(CommonException.GENERIC_ERROR_CODE,
                    "No user profile found user Id :" + customerIdInt);
        }

        ProfileFavouriteListVo profileFavouriteListVo = new ProfileFavouriteListVo();
        List<ProfileFavouriteVo> profileFavouriteVos = new ArrayList<ProfileFavouriteVo>();
        List<ProfileFavourite> profileFavouriteList = profileFavouriteRepo.findAllByUserId(userProfile.getId());

        if (profileFavouriteList != null) {
            for (ProfileFavourite profileFavourite : profileFavouriteList) {
                ProfileFavouriteVo singleData = new ProfileFavouriteVo();
                singleData.setId(String.valueOf(profileFavourite.getId()));
                singleData.setTxnType(profileFavourite.getTxnType());
                singleData.setMainFunction(profileFavourite.getMainFunction());

                if (profileFavourite.getTxnType().toUpperCase().equals("TRANSFER")) {
                    List<String> mainFuncList = getTransferMainFuncPayeeName();
                    if (mainFuncList.contains(profileFavourite.getMainFunction())) {
                        Bank bank = bankRepo.findOneById(profileFavourite.getPayeeId());

                        if (bank == null) {
                            throw new CommonException(CommonException.GENERIC_ERROR_CODE,
                                    "No bank found for payee Id :" + profileFavourite.getPayeeId());
                        }
                        singleData.setPayeeName(bank.getBankName());
                    }

                    List<String> mainFuncList2 = getTransferMainFuncAccNo();
                    if (mainFuncList2.contains(profileFavourite.getMainFunction())) {
                        singleData.setToAccountNo(profileFavourite.getToAccountNo());
                    }

                    if (profileFavourite.getMainFunction().toUpperCase().equals("DUITNOW")) {
                        singleData.setToAccountNo(profileFavourite.getToAccountNo());
                        singleData.setToIdType(profileFavourite.getToIdType());
                        singleData.setToIdNo(profileFavourite.getToIdNo());

                        singleData.setToIdDescription(
                                FavouriteToIdTypeDesc.getDescription(profileFavourite.getToIdType()).getDescription());
                    }

                    if (profileFavourite.getMainFunction().toUpperCase().equals("TOPUP")) {
                        singleData.setNickname(profileFavourite.getNickname());
                    }

                    List<String> mainLabelList = getTransferMainLabel();
                    if (mainLabelList.contains(profileFavourite.getMainFunction())) {
                        singleData.setMainLabel(profileFavourite.getNickname());
                    }
                }

                if (profileFavourite.getTxnType().toUpperCase().equals("PAYMENT")) {
                    if (profileFavourite.getMainFunction().toUpperCase().equals("OTHER_BILLER")) {
                        Biller biller = billerRepo.findById(profileFavourite.getPayeeId());

                        if (biller == null) {
                            throw new CommonException(CommonException.GENERIC_ERROR_CODE,
                                    "No biller found for payee Id: " + profileFavourite.getPayeeId());
                        }
                        singleData.setPayeeName(biller.getBillerName());
                        singleData.setMainLabel(profileFavourite.getNickname());
                    }

                    singleData.setNickname(profileFavourite.getNickname());

                    if (profileFavourite.getMainFunction().toUpperCase().equals("JOMPAY_BILLER")) {
                        singleData.setPayeeId(profileFavourite.getPayeeId());
                        singleData.setMainLabel(profileFavourite.getNickname());
                    }

                    singleData.setRef1(profileFavourite.getRef1());
                }


                if (profileFavourite.getTxnType().toUpperCase().equals("TOPUP")) {

                    TopupBiller topupBiller = topupBillerRepo.findById(profileFavourite.getPayeeId());

                    if (topupBiller == null) {
                        throw new CommonException(CommonException.GENERIC_ERROR_CODE,
                                "No Topup biller found for payee Id: " + profileFavourite.getPayeeId());
                    }
                    singleData.setPayeeName(topupBiller.getBillerName());
                    singleData.setMainLabel(profileFavourite.getNickname());
                    singleData.setRef1(profileFavourite.getRef1());
                }


                if (profileFavourite.getTxnType().toUpperCase().equals(INVEST)) {

                    singleData.setMainLabel(profileFavourite.getNickname());
                    singleData.setRef1(profileFavourite.getRef1());
                    singleData.setToAccountNo(profileFavourite.getToAccountNo());
                    singleData.setNickname(profileFavourite.getNickname());
                    singleData.setMainLabel(profileFavourite.getNickname());
                    singleData.setRef1(profileFavourite.getRef1());
                    singleData.setToIdType(profileFavourite.getToIdType());
                    singleData.setToIdNo(profileFavourite.getToIdNo());
                    singleData.setPayeeId(profileFavourite.getPayeeId());

                    singleData.setToIdDescription(
                            FavouriteToIdTypeDesc.getDescription(profileFavourite.getToIdType()).getDescription());

                    FundDetails fund = fundRepository.findByfundId(profileFavourite.getRef1());
                    if (fund != null) {
                        singleData.setPayeeName(fund.getFundLongName());

                    }
                }

                singleData.setIsQuickLink(profileFavourite.getIsQuickLink());
                singleData.setIsQuickPay(profileFavourite.getIsQuickPay());

                if (profileFavourite.getMainFunction().toUpperCase().equals("DUITNOW")
                        && profileFavourite.getToIdType().toUpperCase().equals("PSPT")) {
                    singleData.setDuitnowCountryName(profileFavourite.getDuitnowCountryCode());
                }

                singleData.setToIdNo(profileFavourite.getToIdNo());

                profileFavouriteVos.add(singleData);
            }

            // Sort the data by main label
            if (profileFavouriteVos.size() > 0) {
                Collections.sort(profileFavouriteVos, new Comparator<ProfileFavouriteVo>() {
                    public int compare(ProfileFavouriteVo contentVo1, ProfileFavouriteVo contentVo2) {
                        return contentVo1.getMainLabel().toUpperCase()
                                .compareTo(contentVo2.getMainLabel().toUpperCase());
                    }
                });
            }

            profileFavouriteListVo.setFavourites(profileFavouriteVos);
        }

        return profileFavouriteListVo;
    }

    private List<String> getTransferMainFuncPayeeName() {
        return Arrays.asList("OWN", "OTHER_RHB", "IBG", "IBFT", "INSTANT");
    }

    private List<String> getTransferMainFuncAccNo() {
        return Arrays.asList("OWN", "OTHER_RHB", "IBG", "IBFT", "INSTANT", "DUITNOW");
    }

    private List<String> getTransferMainLabel() {
        return Arrays.asList("OWN", "OTHER_RHB", "IBG", "IBFT", "INSTANT", "DUITNOW");
    }
}
