package com.rhbgroup.dcpbo.customer.service;

import com.rhbgroup.dcpbo.customer.dto.CustomerFavourites;
import com.rhbgroup.dcpbo.customer.exception.CommonException;
import com.rhbgroup.dcpbo.customer.model.*;
import com.rhbgroup.dcpbo.customer.repository.ProfileFavouriteRepo;
import com.rhbgroup.dcpbo.customer.repository.TopupBillerRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TopupFavouriteService {

    @Autowired
    ProfileFavouriteRepo profileFavouriteRepo;

    @Autowired
    TopupBillerRepo topupBillerRepo;

    public CustomerFavourites retrieveFavouriteTopupDetails(Integer favouriteId){
        CustomerFavourites customerFavourites = new CustomerFavourites();

        ProfileFavourite profileFavourite = profileFavouriteRepo.findById(favouriteId);

        if(profileFavourite == null || !profileFavourite.getTxnType().equals("TOPUP")){
            throw new CommonException(CommonException.CUSTOMER_NOT_FOUND);
        } else {
            if(profileFavourite.getPayeeId() != null) {
                TopupBiller topupBiller = topupBillerRepo.findById(profileFavourite.getPayeeId());
                if(topupBiller != null){
                    customerFavourites.setPayeeName(topupBiller.getBillerName());
                } else {
                    throw new CommonException(CommonException.GENERIC_ERROR_CODE);
                }
            }

            customerFavourites.setId(profileFavourite.getId());
            customerFavourites.setTxnType(profileFavourite.getTxnType());
            customerFavourites.setMainFunction(profileFavourite.getMainFunction());
            customerFavourites.setNickname(profileFavourite.getNickname());
            customerFavourites.setAmount(profileFavourite.getAmount());
            customerFavourites.setRef1(profileFavourite.getRef1());
            customerFavourites.setIsQuickLink(profileFavourite.getIsQuickLink());
            customerFavourites.setIsQuickPay(profileFavourite.getIsQuickPay());
        }

        return customerFavourites;
    }
}
