package com.rhbgroup.dcpbo.customer.controller;


import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.rhbgroup.dcpbo.customer.annotation.DcpIntegration;
import com.rhbgroup.dcpbo.customer.aspect.ApiContext;
import com.rhbgroup.dcpbo.customer.audit.collector.BoControllerAudit;
import com.rhbgroup.dcpbo.customer.contract.BoData;
import com.rhbgroup.dcpbo.customer.service.ConfigService;
import com.rhbgroup.dcpbo.customer.service.CustomerAccountsService;
import com.rhbgroup.dcpbo.customer.service.CustomerProfileService;
import com.rhbgroup.dcpbo.customer.vo.CustomerTrxLimitVo;

@RestController
@RequestMapping(value = "/bo/cs/customer")
public class CustomerController {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private ConfigService configService;
	
    @Autowired
    private CustomerAccountsService customerAccountsService;

	@Autowired
	ApiContext apiContext;
	
	@Autowired
    private CustomerProfileService userProfileService;

	@BoControllerAudit(eventCode = "30016")
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@DcpIntegration
	@GetMapping(value = "/limits/{customerID}", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody List<CustomerTrxLimitVo> getCustomerTrxLimit(@PathVariable("customerID") String customerID) {
		List<CustomerTrxLimitVo> responseBody = configService.getCustomerTrxLimits(customerID);
		return responseBody;
	}

	@BoControllerAudit(eventCode = "30015")
	@GetMapping(value = "/accounts", produces = { MediaType.APPLICATION_JSON_UTF8_VALUE })
	public BoData getCustomerAccounts(@RequestHeader("customerId") String sCustomerId) {
		int customerId = Integer.parseInt(sCustomerId);
		return customerAccountsService.getCustomerAccounts(customerId);
	}
	@BoControllerAudit(eventCode = "30044")
	@GetMapping(value = "/status", produces = { MediaType.APPLICATION_JSON_UTF8_VALUE })
	public BoData getCustomerProfile(@RequestHeader("customerId") Integer customerId) {
		return userProfileService.getCustomerProfile(customerId);
	}
}
