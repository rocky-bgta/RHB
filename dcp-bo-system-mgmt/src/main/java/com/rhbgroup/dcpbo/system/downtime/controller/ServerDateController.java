package com.rhbgroup.dcpbo.system.downtime.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rhbgroup.dcpbo.system.downtime.service.ServerDateService;

@RestController
@RequestMapping(value = "/bo/system")
public class ServerDateController {

	private static Logger logger = LogManager.getLogger(ServerDateController.class);

	@Autowired
	private ServerDateService serverDateService;

	@GetMapping(value = "/downtime/server-date", produces = { MediaType.APPLICATION_JSON_UTF8_VALUE })
	public ResponseEntity<?> getServerDate() {
		logger.debug("getServerDate()");
		
		ResponseEntity<?> responseEntity = serverDateService.getServerDate();
		logger.debug("    responseEntity: " + responseEntity);

		return responseEntity;
	}
}
