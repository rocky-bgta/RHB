package com.rhbgroup.dcpbo.user.workflow.usergroup;

import org.springframework.beans.factory.annotation.Autowired;
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
@RequestMapping(path = "/bo/workflow/usergroup")

public class WFUserGroupController {

	@Autowired
	WFUserGroupService wfUserGroupService;

	public WFUserGroupController(WFUserGroupService wfUserGroupService) {
		this.wfUserGroupService = wfUserGroupService;
	}

	@BoControllerAudit(eventCode = "22002", value = "boAuditAdditionalDataRetriever")
	@PostMapping(value = "/approval/{approvalId}", produces = { MediaType.APPLICATION_JSON_UTF8_VALUE })
	public BoData approveCreation(@RequestHeader("userId") Integer userId,
			@PathVariable("approvalId") Integer approvalId,
			@RequestBody WFUserGroupApprovalActionDetail wfUserGroupApprovalActionDetail) {

		wfUserGroupApprovalActionDetail.setApprovalId(approvalId);
		return wfUserGroupService.approveCreate(userId, wfUserGroupApprovalActionDetail);
	}

	@BoControllerAudit(eventCode = "22005", value = "boAuditAdditionalDataRetriever")
	@PutMapping(value = "/update/approval/{approvalId}", produces = { MediaType.APPLICATION_JSON_UTF8_VALUE })
	public BoData approveUpdate(@RequestHeader("userId") Integer userId, @PathVariable("approvalId") Integer approvalId,
			@RequestBody WFUserGroupApprovalActionDetail wfUserGroupApprovalActionDetail) {

		wfUserGroupApprovalActionDetail.setApprovalId(approvalId);
		return wfUserGroupService.approveUpdate(userId, wfUserGroupApprovalActionDetail);
	}

	@BoControllerAudit(eventCode = "22008", value = "boAuditAdditionalDataRetriever")
	@PutMapping(value = "/delete/approval/{approvalId}", produces = { MediaType.APPLICATION_JSON_UTF8_VALUE })
	public BoData approveDelete(@RequestHeader("userid") Integer userId, @PathVariable("approvalId") Integer approvalId,
			@RequestBody WFUserGroupApprovalActionDetail wfUserGroupApprovalActionDetail) {
		wfUserGroupApprovalActionDetail.setApprovalId(approvalId);
		return wfUserGroupService.approveDelete(userId, wfUserGroupApprovalActionDetail);
	}

}
