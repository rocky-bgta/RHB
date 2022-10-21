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
import com.rhbgroup.dcpbo.customer.service.McaCallTransactionsService;

@RestController
@RequestMapping("/bo")
public class McaController {
	@Autowired
	private McaCallTransactionsService mcaCallTransactionsService;

	private static Logger logger = LogManager.getLogger(McaController.class);

	@BoControllerAudit(eventCode = "30031", value = "mcaAdditionalDataRetriever")
	@GetMapping(value = "/cs/customer/mca/{accountNo}/call/transactions/", produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
	public BoData getMcaCallTransactions(
			@RequestHeader("customerId") Integer customerId,
			@PathVariable(value = "accountNo") String accountNo,
			@RequestParam(value = "foreignCurrency") String foreignCurrency,
			@RequestParam(value = "pageCounter") Integer pageCounter,
			@RequestParam(value = "firstKey") String firstKey,
			@RequestParam(value = "lastKey") String lastKey
			) throws IOException {
		logger.debug("getMcaCallTransactions()");
		
		logger.debug("    customerId: " + customerId);
		logger.debug("    accountNo: " + accountNo);
		logger.debug("    foreignCurrency: " + foreignCurrency);
		logger.debug("    pageCounter: " + pageCounter);
		logger.debug("    firstKey: " + firstKey);
		logger.debug("    lastKey: " + lastKey);

		return mcaCallTransactionsService.getMcaCallTransactions(customerId, accountNo, foreignCurrency, pageCounter, firstKey, lastKey);
	}
}
