package com.rhbgroup.dcpbo.user.info;

import com.rhbgroup.dcpbo.common.audit.BoControllerAudit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rhbgroup.dcpbo.user.common.BoData;

@RestController
@RequestMapping(path = "/bo/user")
public class InfoController {
	
	private InfoService userService;
	
	private static Logger logger = LogManager.getLogger(InfoController.class);
	
	public InfoController(InfoService userService) {
		this.userService = userService;
	}

	@BoControllerAudit(eventCode = "20022")
    @GetMapping(value = "/staffid/{staffId}",
    		produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
	public BoData getStaffId(@PathVariable("staffId") String staffId) {
		logger.debug("getStaffId()");
		logger.debug("    userService: " + userService);

    	return userService.getStaffId(staffId);
	}
}
