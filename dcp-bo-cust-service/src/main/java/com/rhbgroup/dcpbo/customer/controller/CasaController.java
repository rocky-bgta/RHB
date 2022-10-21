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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.rhbgroup.dcpbo.customer.audit.collector.BoControllerAudit;
import com.rhbgroup.dcpbo.customer.contract.BoData;
import com.rhbgroup.dcpbo.customer.service.CasaDetailsService;
import com.rhbgroup.dcpbo.customer.service.CasaTransactionsService;

@RestController
@RequestMapping("/bo")
public class CasaController {
	@Autowired
	private CasaDetailsService casaDetailsService;

	@Autowired
	private CasaTransactionsService casaTransactionsService;

	private static Logger logger = LogManager.getLogger(CasaController.class);

	@BoControllerAudit(eventCode = "30005")
	@GetMapping(value = "/cs/customer/casa/{accountNo}/details",
			produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
	public BoData getCasaDetails(
			@RequestHeader("customerId") String sCustomerId,
			@PathVariable(value = "accountNo") String accountNo) throws IOException {
		logger.debug("getCasaDetails()");
		
		int customerId = Integer.parseInt(sCustomerId);
		
		logger.debug("    customerId: " + customerId);
		logger.debug("    accountNo: " + accountNo);
		logger.debug("    casaDetailsService: " + casaDetailsService);

		return casaDetailsService.getCasaDetails(customerId, accountNo);
	}

    @BoControllerAudit(eventCode = "30006")
	@GetMapping(value = "/cs/customer/casa/{accountNo}/transactions",
			produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
	public BoData getCasaTransactions (
			@RequestHeader("customerId") String sCustomerId,
			@PathVariable(value = "accountNo") String accountNo,
			@RequestParam(value = "firstKey", required = false, defaultValue = "") String firstKey,
			@RequestParam(value = "lastKey", required = false, defaultValue = "") String lastKey,
			@RequestParam(value = "pageCounter") String sPageCounter) {
		logger.debug("getCasaTransactions()");
		
		int customerId = Integer.parseInt(sCustomerId);
		int pageCounter = Integer.parseInt(sPageCounter);
		
		logger.debug("    customerId: " + customerId);
		logger.debug("    accountId: " + accountNo);
		logger.debug("    firstKey: " + firstKey);
		logger.debug("    lastKey: " + lastKey);
		logger.debug("    pageCounter: " + pageCounter);
		logger.debug("    casaDetailsService: " + casaDetailsService);

		return casaTransactionsService.getCasaTransactions(customerId, accountNo, firstKey, lastKey, pageCounter);
	}
}
