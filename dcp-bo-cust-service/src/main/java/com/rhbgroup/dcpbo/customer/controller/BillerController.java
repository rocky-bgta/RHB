package com.rhbgroup.dcpbo.customer.controller;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.rhbgroup.dcpbo.customer.audit.collector.BoControllerAudit;
import com.rhbgroup.dcpbo.customer.contract.BoData;
import com.rhbgroup.dcpbo.customer.service.BillerService;

@RestController
@RequestMapping(path = "/bo")
public class BillerController {
	
	@Autowired
	private BillerService billerService;
	
	private static Logger logger = LogManager.getLogger(BillerController.class);
	
	public BillerController(BillerService billerService) {
		this.billerService=billerService;
	}
	
	@BoControllerAudit(eventCode = "50085")
	@GetMapping(path = "/biller/dashboard", produces = { MediaType.APPLICATION_JSON_UTF8_VALUE })
	public @ResponseBody BoData getBillerCount() {
		logger.debug("getBillerCount()");
		return billerService.getBillerCount();
	}

}
