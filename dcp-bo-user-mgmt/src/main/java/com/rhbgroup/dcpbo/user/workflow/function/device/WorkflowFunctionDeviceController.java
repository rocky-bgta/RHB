package com.rhbgroup.dcpbo.user.workflow.function.device;

import com.rhbgroup.dcpbo.common.audit.BoControllerAudit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import com.rhbgroup.dcpbo.user.common.BoData;

@RestController
@RequestMapping(path = "/bo/workflow")
public class WorkflowFunctionDeviceController {
	
	private WorkflowFunctionDeviceApprovalService workflowFunctionDeviceApprovalService;

	private static Logger logger = LogManager.getLogger(WorkflowFunctionDeviceController.class);
	
	public WorkflowFunctionDeviceController(WorkflowFunctionDeviceApprovalService workflowFunctionDeviceApprovalService) {
		this.workflowFunctionDeviceApprovalService = workflowFunctionDeviceApprovalService;
	}

	@BoControllerAudit(eventCode = "30034")
    @GetMapping(value = "/function/device/approval/{approvalId}",
    		produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
	public BoData getDeviceApproval(@PathVariable("approvalId") Integer approvalId,
									@RequestHeader(value = "userid") Integer userId) {
    	logger.debug("getDeviceApproval()");
    	logger.debug("    approvalId: " + approvalId);

		WorkflowFunctionDeviceApproval workflowFunctionDeviceApproval = (WorkflowFunctionDeviceApproval) workflowFunctionDeviceApprovalService
				.getDeviceApproval(approvalId, userId);
		logger.debug("    workflowFunctionDeviceApproval: " + workflowFunctionDeviceApproval);
    	
    	return workflowFunctionDeviceApproval;
    }
}
