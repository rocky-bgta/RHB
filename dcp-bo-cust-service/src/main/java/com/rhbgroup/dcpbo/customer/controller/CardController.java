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
import com.rhbgroup.dcpbo.customer.service.CardDetailsService;
import com.rhbgroup.dcpbo.customer.service.CardTransactionsService;

@RestController
@RequestMapping("/bo")
public class CardController {

	@Autowired
	private CardDetailsService cardDetailsService;

	@Autowired
	private CardTransactionsService cardTransactionsService;

	private static Logger logger = LogManager.getLogger(CardController.class);

	@BoControllerAudit(eventCode = "30003")
	@GetMapping(value = "/cs/customer/card/{cardId}/{channelFlag}/{connectorCode}/{blockCode}/{accountBlockCode}/details",
			produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
	public BoData getCardDetails(
			@RequestHeader("customerId") String sCustomerId,
			@PathVariable(value = "cardId") String sCardId,
			@PathVariable(value = "channelFlag") String channelFlag,
			@PathVariable(value = "connectorCode") String connectorCode,
			@PathVariable(value = "blockCode") String blockCode,
			@PathVariable(value = "accountBlockCode") String accountBlockCode) throws IOException {
		logger.debug("getCardDetails()");
		
		int customerId = Integer.parseInt(sCustomerId);
		
		logger.debug("    customerId: " + customerId);
		logger.debug("    cardId: " + sCardId);
		logger.debug("    cardDetailsService: " + cardDetailsService);

		return cardDetailsService.getCardDetails(customerId, sCardId, channelFlag, connectorCode, blockCode, accountBlockCode);
	}

    @BoControllerAudit(eventCode = "30004")
	@GetMapping(value = "/cs/customer/card/{cardId}/transactions",
			produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
	public BoData getCardTransactions (
			@RequestHeader("customerId") String sCustomerId,
			@PathVariable(value = "cardId") String sCardId,
			@RequestParam(value = "firstKey", required = false, defaultValue = "") String firstKey,
			@RequestParam(value = "lastKey", required = false, defaultValue = "") String lastKey,
			@RequestParam(value = "pageCounter") String pageCounter) {
		logger.debug("getCardTransactions()");
		
		int customerId = Integer.parseInt(sCustomerId);
		
		logger.debug("    customerId: " + customerId);
		logger.debug("    cardId: " + sCardId);
		logger.debug("    firstKey: " + firstKey);
		logger.debug("    lastKey: " + lastKey);
		logger.debug("    pageCounter: " + pageCounter);
		logger.debug("    cardTransactionsService: " + cardTransactionsService);

		return cardTransactionsService.getCardTransactions(customerId, sCardId, firstKey, lastKey, pageCounter);
	}
}
