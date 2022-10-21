package com.rhbgroup.dcpbo.customer.controller;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.rhbgroup.dcpbo.customer.annotation.DcpIntegration;
import com.rhbgroup.dcpbo.customer.audit.collector.BoControllerAudit;
import com.rhbgroup.dcpbo.customer.contract.BoData;
import com.rhbgroup.dcpbo.customer.service.McaTermService;
import com.rhbgroup.dcpbo.customer.vo.GetMcaTermLogicRequestVo;

@RestController
@RequestMapping(value = "/bo/cs/customer/mca")
public class McaTermController {

	@Autowired
	McaTermService mcaTermService;

	private static Logger logger = LogManager.getLogger(AsbController.class);

	@DcpIntegration
	@BoControllerAudit(eventCode = "30030", value = "mcaAdditionalDataRetriever")
	@GetMapping(value = "/{accountNo}/term/details/{referenceNo}", produces = { MediaType.APPLICATION_JSON_UTF8_VALUE })
	public BoData getMcaTermDetails(@RequestHeader("customerId") String sCustomerId,
			@PathVariable(value = "accountNo") String accountNo,
			@PathVariable(value = "referenceNo") String referenceNo, @RequestParam("currency") String currencyCode)
			throws IOException {
		logger.debug("accountNo: {}", accountNo);
		logger.debug("referenceNo: {}", referenceNo);
		logger.debug("currencyCode: {}", currencyCode);
		int customerId = Integer.parseInt(sCustomerId);
		return mcaTermService.getMcaTermDetails(customerId, accountNo, referenceNo, currencyCode);
	}
	
	@DcpIntegration
	@BoControllerAudit(eventCode = "30029", value = "mcaAdditionalDataRetriever")
	@PostMapping(value = "/term", produces = { MediaType.APPLICATION_JSON_UTF8_VALUE })
	public BoData getMcaTerm(@RequestHeader("customerId") String sCustomerId, @RequestBody GetMcaTermLogicRequestVo request)
			throws IOException {
		logger.debug("accountNo: {}", request.getAccountNo());
		logger.debug("currencyCode: {}", request.getCurrencyCode());
		Integer customerId = Integer.parseInt(sCustomerId);
		return mcaTermService.getMcaTerm(customerId, request);
	}
}
