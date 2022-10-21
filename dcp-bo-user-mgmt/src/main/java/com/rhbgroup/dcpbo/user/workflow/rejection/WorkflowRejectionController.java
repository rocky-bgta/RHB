package com.rhbgroup.dcpbo.user.workflow.rejection;

import com.rhbgroup.dcpbo.common.audit.BoControllerAudit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.spring.web.json.Json;

@RestController
@RequestMapping(path = "/bo/workflow")
public class WorkflowRejectionController {
	
	private WorkflowRejectionService workflowRejectionService;

	public WorkflowRejectionController(WorkflowRejectionService workflowRejectionService) {
		this.workflowRejectionService = workflowRejectionService;
	}

	@BoControllerAudit(eventCode = "00000", value = "boAuditAdditionalDataRetriever")
	@PutMapping(value = "/rejection/{approvalId}",
    		produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
	public WorkflowRejection putWorkflowRejection(@PathVariable("approvalId") int approvalId,
												  @RequestBody RejectReasonRequestBody rejectReason,
												  @RequestHeader("userid") Integer userId) {

    	return workflowRejectionService.putWorkflowRejectionService(approvalId, rejectReason, userId);
	}
}
