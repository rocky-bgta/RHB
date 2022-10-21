package com.rhbgroup.dcpbo.system.downtime.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rhbgroup.dcpbo.common.audit.BoControllerAudit;
import com.rhbgroup.dcpbo.system.common.BoData;
import com.rhbgroup.dcpbo.system.downtime.service.WorkflowDowntimeService;
import com.rhbgroup.dcpbo.system.downtime.vo.ApproveDowntimeAdhocRequestVo;
import com.rhbgroup.dcpbo.system.downtime.vo.DeleteDowntimeAdhocApprovalRequestVo;
import com.rhbgroup.dcpbo.system.downtime.vo.UpdateDowntimeAdhocApprovalRequestVo;

@RestController
@RequestMapping(value = "/bo")
public class WorkflowDowntimeController {

	@Autowired
	private WorkflowDowntimeService workflowDowntimeService;

	private static Logger logger = LogManager.getLogger(WorkflowDowntimeController.class);

	public WorkflowDowntimeController() {
	}
	
	@GetMapping(value = "/workflow/downtime/approval/{approvalId}", produces = { MediaType.APPLICATION_JSON_UTF8_VALUE })
	public ResponseEntity<?> workflowDowntimeApproval(@PathVariable(value = "approvalId") Integer approvalId,
		@RequestHeader Integer userId) {
		logger.debug("workflowDowntimeApproval()");
		logger.debug("    approvalId: " + approvalId);
		
		return workflowDowntimeService.getApproval(approvalId, userId);
	}
	
	@BoControllerAudit(eventCode = "40031", value = "boAuditAdditionalDataRetriever")
	@PostMapping(value = "/workflow/downtime/adhoc/approval/{approvalId}", produces = { MediaType.APPLICATION_JSON_UTF8_VALUE })
	public ResponseEntity<?> workflowApproveAdhocDowntime(@PathVariable(value = "approvalId") Integer approvalId,
			 @RequestBody ApproveDowntimeAdhocRequestVo request,
			 @RequestHeader(value = "userid",defaultValue = "0") String userId) {
		logger.debug("workflowAdhocDowntimeApproval()");
		logger.debug("    userId: " + userId);
		logger.debug("    approvalId: " + approvalId);
		
		return workflowDowntimeService.workflowApproveAdhocDowntime(approvalId, request, userId);
	}
	
	@BoControllerAudit(eventCode = "40037", value = "boAuditAdditionalDataRetriever")
	@PutMapping(value = "/workflow/downtime/adhoc/delete/approval/{approvalId}", produces = { MediaType.APPLICATION_JSON_UTF8_VALUE })
	public BoData workflowDeleteDowntimeApproval(@PathVariable(value = "approvalId") Integer approvalId,
			@RequestBody DeleteDowntimeAdhocApprovalRequestVo deleteDowntimeApprovalRequestVo,
			@RequestHeader(value = "userid",defaultValue = "0") String userId) {
	
		return workflowDowntimeService.deleteApproval(approvalId, deleteDowntimeApprovalRequestVo, userId);
	}

        @BoControllerAudit(eventCode = "40034", value = "boAuditAdditionalDataRetriever")
	@PutMapping(value = "/workflow/downtime/adhoc/update/approval/{approvalId}", produces = { MediaType.APPLICATION_JSON_UTF8_VALUE })
	public BoData workflowUpdateDowntimeApproval(@PathVariable(value = "approvalId") Integer approvalId,
			@RequestBody UpdateDowntimeAdhocApprovalRequestVo updateDowntimeAdhocApprovalRequestVo,
			@RequestHeader(value = "userid",defaultValue = "0") String userId) {
	
		return workflowDowntimeService.updateApproval(approvalId, updateDowntimeAdhocApprovalRequestVo, userId);
	}

}
