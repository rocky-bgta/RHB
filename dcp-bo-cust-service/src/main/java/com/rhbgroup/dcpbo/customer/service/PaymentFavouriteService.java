package com.rhbgroup.dcpbo.customer.service;

import com.rhbgroup.dcpbo.customer.dto.CustomerFavourites;
import com.rhbgroup.dcpbo.customer.exception.CommonException;
import com.rhbgroup.dcpbo.customer.model.Biller;
import com.rhbgroup.dcpbo.customer.model.ProfileFavourite;
import com.rhbgroup.dcpbo.customer.repository.BillerRepo;
import com.rhbgroup.dcpbo.customer.repository.ProfileFavouriteRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PaymentFavouriteService {

    @Autowired
    ProfileFavouriteRepo profileFavouriteRepo;

    @Autowired
    BillerRepo billerRepo;

    public CustomerFavourites retrieveFavouritePaymentDetails(Integer favouriteId){
        CustomerFavourites customerFavourites = new CustomerFavourites();

        ProfileFavourite profileFavourite = profileFavouriteRepo.findById(favouriteId);
        if(profileFavourite == null || !profileFavourite.getTxnType().equals("PAYMENT")){
            throw new CommonException(CommonException.CUSTOMER_NOT_FOUND);
        } else {
            customerFavourites.setId(profileFavourite.getId());
            customerFavourites.setTxnType(profileFavourite.getTxnType());
            customerFavourites.setMainFunction(profileFavourite.getMainFunction());
            customerFavourites.setPayeeId(profileFavourite.getPayeeId());

            if(profileFavourite.getMainFunction().equals("OTHER_BILLER") && profileFavourite.getPayeeId() != null){
                Biller biller = billerRepo.findById(profileFavourite.getPayeeId());
                if(biller != null){
                    customerFavourites.setPayeeName(biller.getBillerName());
                } else {
                    throw new CommonException(CommonException.GENERIC_ERROR_CODE);
                }
            }

            customerFavourites.setNickname(profileFavourite.getNickname());
            customerFavourites.setAmount(profileFavourite.getAmount());
            customerFavourites.setRef1(profileFavourite.getRef1());
            customerFavourites.setRef2(profileFavourite.getRef2());
            customerFavourites.setRef3(profileFavourite.getRef3());
            customerFavourites.setRef4(profileFavourite.getRef4());

            customerFavourites.setIsQuickLink(profileFavourite.getIsQuickLink());
            customerFavourites.setIsQuickPay(profileFavourite.getIsQuickPay());
        }

        return customerFavourites;
    }
}
