package com.rhbgroup.dcpbo.customer.controller;

import java.io.IOException;

import com.rhbgroup.dcpbo.customer.audit.collector.BoControllerAudit;
import com.rhbgroup.dcpbo.customer.contract.BoData;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rhbgroup.dcpbo.customer.annotation.DcpIntegration;
import com.rhbgroup.dcpbo.customer.service.PersonalLoanService;

@RestController
@RequestMapping(value = "/bo")
public class PersonalLoanController {

	@Autowired
	PersonalLoanService personalLoanService;

	private static Logger logger = LogManager.getLogger(AsbController.class);

	@DcpIntegration
	@BoControllerAudit(eventCode = "30026")
	@GetMapping(value = "/cs/customer/loan/personal/{accountNo}/details", produces = {
			MediaType.APPLICATION_JSON_UTF8_VALUE })
	public BoData getPersonalLoanDetails(@RequestHeader("customerId") String sCustomerId,
										 @PathVariable(value = "accountNo") String accountNo) throws IOException {
		logger.debug("Get Personal Loan Details Controller...");

		int customerId = Integer.parseInt(sCustomerId);

		logger.debug("customerId: " + customerId);
		logger.debug("accountId : " + accountNo);

		return personalLoanService.getPersonalLoanDetails(customerId, accountNo);
	}
}
