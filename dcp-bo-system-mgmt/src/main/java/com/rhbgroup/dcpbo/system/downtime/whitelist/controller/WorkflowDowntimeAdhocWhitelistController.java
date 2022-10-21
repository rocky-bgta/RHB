package com.rhbgroup.dcpbo.system.downtime.whitelist.controller;

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
import com.rhbgroup.dcpbo.system.common.BoData;
import com.rhbgroup.dcpbo.system.downtime.whitelist.service.WorkflowDowntimeAdhocWhitelistService;
import com.rhbgroup.dcpbo.system.downtime.whitelist.vo.ApproveAddDowntimeAdhocWhitelistRequest;
import com.rhbgroup.dcpbo.system.downtime.whitelist.vo.ApproveDeleteDowntimeAdhocWhitelistRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping(value = "/bo")
public class WorkflowDowntimeAdhocWhitelistController {

	@Autowired
	private WorkflowDowntimeAdhocWhitelistService workflowDowntimeAdhocWhitelistService;
        
	public WorkflowDowntimeAdhocWhitelistController(WorkflowDowntimeAdhocWhitelistService workflowDowntimeAdhocWhitelistService) {
		this.workflowDowntimeAdhocWhitelistService = workflowDowntimeAdhocWhitelistService;
	}
	
	@BoControllerAudit(eventCode = "40041", value = "boAuditAdditionalDataRetriever")
	@PostMapping(value = "/workflow/downtime/whitelist/approval/{approvalId}", produces = { MediaType.APPLICATION_JSON_UTF8_VALUE })
	public BoData workflowAddDowntimeWhitelistApproval(@PathVariable(value = "approvalId") Integer approvalId,
			@RequestBody ApproveAddDowntimeAdhocWhitelistRequest approveAddDowntimeAdhocWhitelistRequest,
			@RequestHeader(value = "userid",defaultValue = "0") String userId) {
		return workflowDowntimeAdhocWhitelistService.approveAddDowntimeWhitelist(approvalId, approveAddDowntimeAdhocWhitelistRequest, userId);
	}
	
	
	@BoControllerAudit(eventCode = "40044", value = "boAuditAdditionalDataRetriever")
	@PutMapping(value = "/workflow/downtime/whitelist/delete/approval/{approvalId}", produces = { MediaType.APPLICATION_JSON_UTF8_VALUE })
	public BoData workflowDeleteDowntimeWhitelistApproval(@PathVariable(value = "approvalId") Integer approvalId,
			@RequestBody ApproveDeleteDowntimeAdhocWhitelistRequest approveDeleteDowntimeAdhocWhitelistRequest,
			@RequestHeader(value = "userid",defaultValue = "0") String userId) {
		return workflowDowntimeAdhocWhitelistService.approveDeleteDowntimeWhitelist(approvalId, approveDeleteDowntimeAdhocWhitelistRequest, userId);
	}
        
        @BoControllerAudit(eventCode = "40046", value = "boAuditAdditionalDataRetriever")
	@GetMapping(value = "/workflow/downtime/whitelist/approval/{approvalId}", produces = { MediaType.APPLICATION_JSON_UTF8_VALUE })
	public ResponseEntity<BoData> workflowGetDowntimeWhitelistApproval(@PathVariable(value = "approvalId") Integer approvalId,
			@RequestParam(name = "actionType") String actionType,
			@RequestHeader(value = "userid") Integer userId) {
		
            return workflowDowntimeAdhocWhitelistService.getWhitelistApproval(approvalId, userId, actionType);
	}
        
}
