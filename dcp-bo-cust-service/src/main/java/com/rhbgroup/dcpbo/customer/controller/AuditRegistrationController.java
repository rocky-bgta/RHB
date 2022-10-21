package com.rhbgroup.dcpbo.customer.controller;

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
import com.rhbgroup.dcpbo.customer.service.AuditRegistrationService;

@RestController
@RequestMapping("/bo/cs")
public class AuditRegistrationController {
	
	@Autowired
	private AuditRegistrationService auditRegistrationService;

	private static Logger logger = LogManager.getLogger(AuditRegistrationController.class);

	@BoControllerAudit(eventCode = "30002")
	@GetMapping(value = "/customer/registration/audit/{token}/details",
			produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
	public BoData getAuditRegistrationDetails(
			@PathVariable(value = "token") String token) {
		logger.debug("getAuditRegistrationDetails()");
		logger.info("token: " +token);
		return auditRegistrationService.getAuditRegistrationDetails(token);
	}
	
	@BoControllerAudit(eventCode = "300041")
		@GetMapping(path = "/customer/registration/audit/list")
		public BoData getList(@RequestParam(value = "cisNo") String cisNo,
							  @RequestParam(value = "pageNo", required = false, defaultValue = "1") Integer pageNo,
							  @RequestParam(value = "from", required = false, defaultValue = "empty") String frDate,
							  @RequestParam(value = "to", required = false, defaultValue = "empty") String toDate) {
	
	        return auditRegistrationService.listing(cisNo, pageNo,frDate, toDate);
	
		}

}
