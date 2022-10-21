package com.rhbgroup.dcpbo.customer.controller;

import com.rhbgroup.dcpbo.customer.audit.collector.BoControllerAudit;
import com.rhbgroup.dcpbo.customer.contract.BoData;
import com.rhbgroup.dcpbo.customer.service.PaymentFavouriteService;
import com.rhbgroup.dcpbo.customer.service.TopupFavouriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import com.rhbgroup.dcpbo.customer.service.InvestService;

@RestController
@RequestMapping(value = "bo/cs/favourites/")
public class FavouritesController {

    @Autowired
    TopupFavouriteService topupFavouriteService;

    @Autowired
    PaymentFavouriteService paymentFavouriteService;
    
    @Autowired
    InvestService investService;

    @BoControllerAudit(eventCode = "30019")
    @GetMapping(value = "{favouriteId}/topup", produces = { MediaType.APPLICATION_JSON_UTF8_VALUE })
    public BoData getFavouriteTopup(@PathVariable("favouriteId") Integer favouriteId) {
        return topupFavouriteService.retrieveFavouriteTopupDetails(favouriteId);
    }

    @BoControllerAudit(eventCode = "30018")
    @GetMapping(value = "{favouriteId}/payment", produces = { MediaType.APPLICATION_JSON_UTF8_VALUE })
    public BoData getFavouritePayments(@PathVariable("favouriteId") Integer favouriteId) {
        return paymentFavouriteService.retrieveFavouritePaymentDetails(favouriteId);
    }
    
    @BoControllerAudit(eventCode = "30039")
    @GetMapping(value = "{favouriteId}/invest", produces = { MediaType.APPLICATION_JSON_UTF8_VALUE })
    public BoData getFavouriteInvest(@PathVariable("favouriteId") Integer favouriteId) {
        return investService.retrieveFavouriteInvestDetails(favouriteId);
        
    }
}
