package com.rhbgroup.dcpbo.customer.controller;

import com.rhbgroup.dcpbo.customer.audit.collector.BoControllerAudit;
import com.rhbgroup.dcpbo.customer.contract.BoData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.rhbgroup.dcpbo.customer.annotation.DcpIntegration;
import com.rhbgroup.dcpbo.customer.aspect.ApiContext;
import com.rhbgroup.dcpbo.customer.service.ProfileFavouriteService;
import com.rhbgroup.dcpbo.customer.vo.ProfileFavouriteListVo;

@RestController
@RequestMapping(path = "/bo/cs/customer")
public class CustomerFavouriteController {

	private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private ProfileFavouriteService profileFavouriteService;

	@Autowired
	ApiContext apiContext;

	@BoControllerAudit(eventCode = "30017")
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@DcpIntegration
	@GetMapping(value = "/favourites/list", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody BoData getCustomerFavouriteList(@RequestHeader("customerId") String customerId) {
		BoData responseBody = profileFavouriteService.getProfileFavourites(customerId);
		return responseBody;
	}

}
