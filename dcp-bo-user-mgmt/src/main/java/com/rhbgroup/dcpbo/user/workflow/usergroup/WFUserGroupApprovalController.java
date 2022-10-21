package com.rhbgroup.dcpbo.user.workflow.usergroup;

import com.rhbgroup.dcpbo.common.audit.BoControllerAudit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rhbgroup.dcpbo.user.common.BoData;

@RestController
@RequestMapping(path = "/bo/workflow/function/usergroup/approval/")
public class WFUserGroupApprovalController {

	private WFUserGroupService wfUserGroupService;

	private static Logger logger = LogManager.getLogger(WFUserGroupApprovalController.class);

	public WFUserGroupApprovalController(WFUserGroupService wfUserGroupService) {
		this.wfUserGroupService = wfUserGroupService;
	}

	@BoControllerAudit(eventCode = "20028")
	@GetMapping(value = "/{approvalId}", produces = { MediaType.APPLICATION_JSON_UTF8_VALUE })
	public BoData getWorkflowUserApprovalDetail(@RequestHeader("userId") Integer userId,
			@PathVariable("approvalId") int approvalId) {

		return wfUserGroupService.getWorkflowApprovalDetail(userId, approvalId);
	}
}
