package com.rhbgroup.dcpbo.customer.controller;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.rhbgroup.dcpbo.customer.audit.collector.BoControllerAudit;
import com.rhbgroup.dcpbo.customer.contract.BoData;
import com.rhbgroup.dcpbo.customer.service.ResetUserNameService;

@RestController
@RequestMapping(path="/bo/cs") 
public class ResetUserNameController {
	
	@Autowired
	ResetUserNameService resetUserNameService;
	
	private static Logger logger = LogManager.getLogger(ResetUserNameController.class);
	
	public ResetUserNameController(ResetUserNameService resetUserNameService) {
		this.resetUserNameService=resetUserNameService;
	}
	
    @BoControllerAudit(eventCode = "30042", value = "putUnblockFacilityAdditionalDataRetriever")
    @PutMapping(value = "/customer/reset/{id}/{code}", produces = { MediaType.APPLICATION_JSON_UTF8_VALUE })
    public @ResponseBody
    BoData resetUserName(@PathVariable("id") String id, @PathVariable("code") String code){
    	logger.info("Inside resetUserName");
        return resetUserNameService.resetUserName(id, code);
    }

}
