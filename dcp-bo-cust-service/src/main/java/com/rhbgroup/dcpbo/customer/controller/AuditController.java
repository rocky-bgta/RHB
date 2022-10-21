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
import com.rhbgroup.dcpbo.customer.exception.CommonException;
import com.rhbgroup.dcpbo.customer.service.AuditDetailsService;
import com.rhbgroup.dcpbo.customer.service.DcpCustomerAuditService;

@RestController
@RequestMapping("/bo")
public class AuditController {
	@Autowired
	private AuditDetailsService auditDetailsService;

	@Autowired
	private DcpCustomerAuditService dcpCustomerAuditService;

	private static Logger logger = LogManager.getLogger(AuditController.class);

	@BoControllerAudit(eventCode = "30002")
	@GetMapping(value = "/cs/customer/audit/{auditId}/{eventCode}/details",
			produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
	public BoData getAuditDetails(
			@PathVariable(value = "auditId") String sAuditId,
			@PathVariable(value = "eventCode") String eventCode) {
		logger.debug("getAuditDetails()");

		int auditId = Integer.parseInt(sAuditId);

		logger.info("    auditId: " + auditId);
		logger.info("    eventCode: " + eventCode);
		logger.info("    auditDetailsService: " + auditDetailsService);

		return auditDetailsService.getAuditDetailsActivity(auditId, eventCode);
	}

    @BoControllerAudit(eventCode = "30001")
	@GetMapping(path = "/cs/customer/audit/list")
	public BoData getList(@RequestHeader("customerId") int customerId,
						  @RequestParam(value = "pageNo", required = false, defaultValue = "1") Integer pageNo,
						  @RequestParam(value = "from", required = false, defaultValue = "empty") String frDate,
						  @RequestParam(value = "to", required = false, defaultValue = "empty") String toDate,
						  @RequestParam(value = "categories", required = false, defaultValue = "all") String catIds) {

        return dcpCustomerAuditService.listing(frDate, toDate, customerId, catIds, pageNo);

	}
}
