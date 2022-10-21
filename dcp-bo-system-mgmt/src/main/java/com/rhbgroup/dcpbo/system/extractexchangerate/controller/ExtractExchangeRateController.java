package com.rhbgroup.dcpbo.system.extractexchangerate.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rhbgroup.dcpbo.common.audit.BoControllerAudit;
import com.rhbgroup.dcpbo.system.common.BoData;
import com.rhbgroup.dcpbo.system.extractexchangerate.service.ExtractExchangeRateService;

@RestController
@RequestMapping(value = "/bo")
public class ExtractExchangeRateController {

	private static Logger logger = LogManager.getLogger(ExtractExchangeRateController.class);

	@Autowired
	private ExtractExchangeRateService extractExchangeRateService;

	public ExtractExchangeRateController(ExtractExchangeRateService extractExchangeRateService) {
		this.extractExchangeRateService = extractExchangeRateService;
	}

	@BoControllerAudit(eventCode = "39001")
	@GetMapping(value = "/system/mca/rate/{currencyCode}", produces = { MediaType.APPLICATION_JSON_UTF8_VALUE })
	public BoData getExchangeRate(@PathVariable(value = "currencyCode") String currencyCode) {
		return extractExchangeRateService.getExchangeRate(currencyCode + ",");
	}
}
