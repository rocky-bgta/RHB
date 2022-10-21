package com.rhbgroup.dcpbo.system.extractexchangerate.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rhbgroup.dcpbo.common.audit.BoControllerAudit;
import com.rhbgroup.dcpbo.system.common.BoData;
import com.rhbgroup.dcpbo.system.extractexchangerate.service.ExtractExchangeRateService;
import com.rhbgroup.dcpbo.system.extractexchangerate.service.ExtractInterestExchangeRateService;
import com.rhbgroup.dcpbo.system.extractexchangerate.service.impl.dto.ExtractInterestExchangeRateRequest;

@RestController
@RequestMapping(value = "/bo")
public class ExtractInterestExchangeRateController {

	private static Logger logger = LogManager.getLogger(ExtractInterestExchangeRateController.class);

	@Autowired
	private ExtractInterestExchangeRateService extractInterestExchangeRateService;

	public ExtractInterestExchangeRateController(ExtractInterestExchangeRateService extractInterestExchangeRateService) {
		this.extractInterestExchangeRateService = extractInterestExchangeRateService;
	}

	@BoControllerAudit(eventCode = "39001")
	@PostMapping(value = "/system/mca/interest-rate", produces = { MediaType.APPLICATION_JSON_UTF8_VALUE })
	public BoData getInterestExchangeRate(@RequestBody ExtractInterestExchangeRateRequest request) {
		logger.debug("getInterestExchangeRate()");
		return extractInterestExchangeRateService.getInterestExchangeRate(request);
	}
}
