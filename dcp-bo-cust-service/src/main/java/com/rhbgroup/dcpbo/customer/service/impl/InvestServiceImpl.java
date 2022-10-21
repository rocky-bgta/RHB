package com.rhbgroup.dcpbo.customer.service.impl;


import com.rhbgroup.dcpbo.customer.dto.CustomerFavourites;
import com.rhbgroup.dcpbo.customer.model.FundDetails;
import com.rhbgroup.dcpbo.customer.model.ProfileFavourite;
import com.rhbgroup.dcpbo.customer.repository.FundRepository;
import com.rhbgroup.dcpbo.customer.repository.ProfileFavouriteRepo;
import com.rhbgroup.dcpbo.customer.service.InvestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class InvestServiceImpl implements InvestService {

    @Autowired
    ProfileFavouriteRepo profileFavouriteRepo;

    @Autowired
    FundRepository fundRepository;

    public CustomerFavourites retrieveFavouriteInvestDetails(Integer favouriteId) {
        CustomerFavourites customerFavourites = new CustomerFavourites();


        int id = favouriteId;
        ProfileFavourite favourite = profileFavouriteRepo.findById(id);

        if (favourite != null && favourite.getTxnType().equals("INVEST")) {

            customerFavourites.setId(favourite.getId());
            customerFavourites.setNickname(favourite.getNickname());

            FundDetails fund = fundRepository.findByfundId(favourite.getRef1());
            if (fund != null) {
                customerFavourites.setFundName(fund.getFundLongName());
                customerFavourites.setFundShortName(fund.getFundShortName());
            }
            customerFavourites.setMembersName(favourite.getRef2());
            customerFavourites.setMembershipNumber(favourite.getToAccountNo());
            customerFavourites.setIsQuickLink(favourite.getIsQuickLink());
            customerFavourites.setIsQuickPay(favourite.getIsQuickPay());

        }


        return customerFavourites;
    }

}
