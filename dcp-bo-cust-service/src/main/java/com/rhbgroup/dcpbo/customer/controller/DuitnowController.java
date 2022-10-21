package com.rhbgroup.dcpbo.customer.controller;

import java.io.IOException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rhbgroup.dcpbo.customer.audit.collector.BoControllerAudit;
import com.rhbgroup.dcpbo.customer.contract.BoData;
import com.rhbgroup.dcpbo.customer.service.DuitnowEnquiryService;

@RestController
@RequestMapping("/bo/cs/customer")
public class DuitnowController {
	@Autowired
	private DuitnowEnquiryService duitnowEnquiryService;

	private static Logger logger = LogManager.getLogger(DuitnowController.class);

	@BoControllerAudit(eventCode = "30033", value = "duitnowAdditionalDataRetriever")
	@GetMapping(value = "/duitnow", produces = { MediaType.APPLICATION_JSON_UTF8_VALUE })
	public BoData getDuitnowDetails(@RequestHeader("customerId") String sCustomerId) throws IOException {

		logger.debug("getDuitnowDetails : {}", sCustomerId);
		int customerId = Integer.parseInt(sCustomerId);

		return duitnowEnquiryService.getDuitnowDetails(customerId);
	}

}
