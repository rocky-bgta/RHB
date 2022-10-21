package com.rhbgroup.dcpbo.customer.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import com.rhbgroup.dcpbo.customer.audit.collector.BoControllerAudit;
import com.rhbgroup.dcpbo.customer.contract.BoData;
import com.rhbgroup.dcpbo.customer.service.UnitTrustDetailsService;

@RestController
@RequestMapping(value = "/bo/cs/customer")
public class UnitTrustController {
	
    @Autowired
    private UnitTrustDetailsService unitTrustDetailsService;

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@BoControllerAudit(eventCode = "30032", value = "unitTrustAdditionalDataRetriever")
	@GetMapping(value = "/ut/{accountNo}/details", produces = { MediaType.APPLICATION_JSON_UTF8_VALUE })
	public BoData getUnitTrustDetails(@PathVariable("accountNo") String accountNo, @RequestHeader("customerId") Integer customerId) {
		logger.debug("Get unit trust details");
		logger.debug("    accountNo: " + accountNo);
		logger.debug("    unitTrustDetailsService: " + unitTrustDetailsService);

		return unitTrustDetailsService.getUnitTrustDetails(accountNo, customerId);
	}
}
