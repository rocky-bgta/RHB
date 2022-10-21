package com.rhbgroup.dcpbo.system.downtime.controller;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.rhbgroup.dcpbo.system.common.BoData;
import com.rhbgroup.dcpbo.system.downtime.service.BankService;


@RestController
@RequestMapping("/bo")
public class BankController {
	
	@Autowired
	private BankService bankService;
	
	private static Logger logger = LogManager.getLogger(BankController.class);
	
	public BankController(BankService bankService) {
		this.bankService=bankService;
	}
	
	@GetMapping(path = "/bank", produces = { MediaType.APPLICATION_JSON_UTF8_VALUE })
	public @ResponseBody BoData getBankPaymentType(@RequestParam(value = "pageNo", required = true, defaultValue = "1") Integer pageNo) {
		
		logger.debug("getBankPaymentType()");
		return bankService.getBankPaymentType(pageNo);
	}

}
