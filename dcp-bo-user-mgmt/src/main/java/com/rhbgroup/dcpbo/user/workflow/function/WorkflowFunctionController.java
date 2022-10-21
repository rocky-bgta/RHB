package com.rhbgroup.dcpbo.user.workflow.function;

import com.rhbgroup.dcpbo.common.audit.BoControllerAudit;
import com.rhbgroup.dcpbo.user.common.BoData;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import javax.validation.constraints.NotNull;

@RestController
@RequestMapping(path = "/bo/workflow")
public class WorkflowFunctionController {
	
	private WorkflowFunctionService workflowFunctionService;

	public WorkflowFunctionController(WorkflowFunctionService workflowFunctionService) {
		this.workflowFunctionService = workflowFunctionService;
	}

	@BoControllerAudit(eventCode = "20026")
    @GetMapping(value = "/function/{functionId}",
    		produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
	public BoData getWorkflowFunction(@PathVariable("functionId") int functionId,
									  @RequestParam(value = "status", defaultValue = "") String status) {
    	return workflowFunctionService.getWorkflowFunctionService(functionId,status);
	}
}
