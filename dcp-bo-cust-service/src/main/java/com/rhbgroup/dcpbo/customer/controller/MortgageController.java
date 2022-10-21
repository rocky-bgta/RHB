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
import com.rhbgroup.dcpbo.customer.service.MortgageDetailsService;

@RestController
@RequestMapping("/bo")
public class MortgageController {
	@Autowired
	private MortgageDetailsService mortgageDetailsService;

	private static Logger logger = LogManager.getLogger(MortgageController.class);

	@BoControllerAudit(eventCode = "30025")
	@GetMapping(value = "/cs/customer/loan/mortgage/{accountNo}/details", produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
	public BoData getMortgageDetails(
			@RequestHeader("customerId") String sCustomerId,
			@PathVariable(value = "accountNo") String accountNo) throws IOException {
		logger.debug("getCardDetails()");
		
		int customerId = Integer.parseInt(sCustomerId);
		
		logger.debug("    customerId: " + customerId);
		logger.debug("    accountNo: " + accountNo);
		logger.debug("    mortgageDetailsService: " + mortgageDetailsService);

		return mortgageDetailsService.getMortgageDetails(customerId, accountNo);
	}
}
