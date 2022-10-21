package com.rhbgroup.dcpbo.user.create;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rhbgroup.dcpbo.common.audit.BoControllerAudit;
import com.rhbgroup.dcpbo.user.common.BoData;

@RestController
@RequestMapping(path = "/bo")
public class UserFunctionController {
	
	private UserFunctionService userfunctionService;

	public UserFunctionController(UserFunctionService userfunctionService) {
		this.userfunctionService = userfunctionService;
	}

	@BoControllerAudit(eventCode = "21001", value = "boAuditAdditionalDataRetriever")
	@PostMapping(value = "/user", produces = { MediaType.APPLICATION_JSON_UTF8_VALUE })
	public BoData createBoUser(@RequestHeader Integer userId, @RequestBody UserCreateRequestVo request) {
		return userfunctionService.createBoUser(userId, request);
	}
	
	@BoControllerAudit(eventCode = "21004", value = "boAuditAdditionalDataRetriever")
	@PutMapping(value = "/user/{userid}/update", produces = { MediaType.APPLICATION_JSON_UTF8_VALUE })
	public BoData updateBoUser(@RequestHeader("userid") Integer creatorId, @RequestBody UserUpdateRequestVo request,
			@PathVariable(value = "userid") String userid) {
		return userfunctionService.updateBoUser(creatorId, request, userid);
	}
	
	@BoControllerAudit(eventCode = "21007", value = "boAuditAdditionalDataRetriever")
	@PutMapping(value = "/user/{userId}/delete", produces = { MediaType.APPLICATION_JSON_UTF8_VALUE })
	public BoData deleteBoUser(@RequestHeader("userid") Integer creatorId, @RequestBody UserDeleteRequestVo request,
			@PathVariable(value = "userId") Integer userId) {
		return userfunctionService.deleteBoUser(creatorId, request, userId);
	}
	
}
