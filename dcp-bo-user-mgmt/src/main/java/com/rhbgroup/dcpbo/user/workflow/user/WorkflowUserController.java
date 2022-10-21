package com.rhbgroup.dcpbo.user.workflow.user;

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
import com.rhbgroup.dcpbo.user.workflow.user.delete.WorkflowUserDeleteService;
import com.rhbgroup.dcpbo.user.workflow.user.delete.dto.UserDeleteApprovalRequestVo;

@RestController
@RequestMapping(path = "/bo/workflow/user")

public class WorkflowUserController {

	@Autowired
	WorkflowUserDeleteService workflowUserDeleteService;

	@Autowired
	WFUserApprovalService wfUserApprovalService;
	
	public WorkflowUserController(WorkflowUserDeleteService workflowUserDeleteService, WFUserApprovalService wfUserApprovalService) {
		this.workflowUserDeleteService = workflowUserDeleteService;
		this.wfUserApprovalService = wfUserApprovalService;
	}
	
	@BoControllerAudit(eventCode = "21008", value = "boAuditAdditionalDataRetriever")
	@PutMapping(value = "/delete/approval/{approvalId}",
			produces = { MediaType.APPLICATION_JSON_UTF8_VALUE })
	public BoData userDeleteApproval(
			@PathVariable("approvalId") int approvalId,
			@RequestBody UserDeleteApprovalRequestVo userDeleteApprovalRequestVo) {
		String reason = userDeleteApprovalRequestVo.getReason();
		BoData userDeleteApprovalResponseVo = workflowUserDeleteService.userDeleteApproval(approvalId, reason);
		return userDeleteApprovalResponseVo;
	}
	
	@BoControllerAudit(eventCode = "21005", value = "boAuditAdditionalDataRetriever")
	@PutMapping(value = "/update/approval/{approvalId}", produces = { MediaType.APPLICATION_JSON_UTF8_VALUE })
	public BoData approveUpdate(@RequestHeader("userId") Integer userId, @PathVariable("approvalId") Integer approvalId,
			@RequestBody WFUserApprovalActionDetail wfUserApprovalActionDetail) {

		wfUserApprovalActionDetail.setApprovalId(approvalId);
		return wfUserApprovalService.approveUpdate(userId, wfUserApprovalActionDetail);
	}
	
	@BoControllerAudit(eventCode = "21002", value = "boAuditAdditionalDataRetriever")
	@PostMapping(value = "/approval/{approvalId}", produces = { MediaType.APPLICATION_JSON_UTF8_VALUE })
	public BoData approveCreation(@RequestHeader("userId") Integer userId,
			@PathVariable("approvalId") Integer approvalId,
			@RequestBody WFUserApprovalActionDetail wfUserApprovalActionDetail) {

		wfUserApprovalActionDetail.setApprovalId(approvalId);
		return wfUserApprovalService.approveCreate(userId, wfUserApprovalActionDetail);
	}

}
