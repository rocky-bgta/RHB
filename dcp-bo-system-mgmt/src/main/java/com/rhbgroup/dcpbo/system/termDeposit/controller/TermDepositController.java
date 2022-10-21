package com.rhbgroup.dcpbo.system.termDeposit.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rhbgroup.dcpbo.common.audit.BoControllerAudit;
import com.rhbgroup.dcpbo.system.common.BoData;
import com.rhbgroup.dcpbo.system.termDeposit.dto.TermDepositPlacementConfirmationRequest;
import com.rhbgroup.dcpbo.system.termDeposit.service.TermDepositPlacementService;

@RestController
@RequestMapping(value = "/bo")
public class TermDepositController {

	private static Logger logger = LogManager.getLogger(TermDepositController.class);

	@Autowired
	private TermDepositPlacementService termDepositPlacementService;

	public TermDepositController(TermDepositPlacementService termDepositPlacementService) {
		this.termDepositPlacementService = termDepositPlacementService;
	}

	@BoControllerAudit(eventCode = "39001")
	@PostMapping(value = "/system/fpx/term-deposit/placement", produces = { MediaType.APPLICATION_JSON_UTF8_VALUE })
	public ResponseEntity<BoData> termDepositPlacement(@RequestBody TermDepositPlacementConfirmationRequest request) {
		logger.debug("getTermDepositPlacement()");
		return termDepositPlacementService.termDepositPlacement(request);
	}
}
