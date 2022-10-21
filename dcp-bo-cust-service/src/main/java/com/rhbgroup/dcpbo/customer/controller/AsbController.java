package com.rhbgroup.dcpbo.customer.controller;

import java.io.IOException;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rhbgroup.dcpbo.customer.audit.collector.BoControllerAudit;
import com.rhbgroup.dcpbo.customer.contract.BoData;
import com.rhbgroup.dcpbo.customer.service.AsbDetailsService;

@RestController
@RequestMapping("/bo")
public class AsbController {
	@Autowired
	private AsbDetailsService asbDetailsService;

	private static Logger logger = LogManager.getLogger(AsbController.class);

	@BoControllerAudit(eventCode = "30000")
	@GetMapping(value = "/cs/customer/loan/asb/{accountNo}/details", produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
	public BoData getAsbDetails(
			@RequestHeader("customerId") String sCustomerId,
			@PathVariable(value = "accountNo") String accountNo) throws IOException {
		logger.debug("getCardDetails()");
		
		int customerId = Integer.parseInt(sCustomerId);
		
		logger.debug("    customerId: " + customerId);
		logger.debug("    accountId: " + accountNo);
		logger.debug("    asbDetailsService: " + asbDetailsService);

		return asbDetailsService.getAsbDetails(customerId, accountNo);
	}
}
