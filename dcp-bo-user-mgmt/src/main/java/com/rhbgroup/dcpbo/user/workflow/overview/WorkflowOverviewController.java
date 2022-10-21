package com.rhbgroup.dcpbo.user.workflow.overview;

import com.rhbgroup.dcpbo.common.audit.BoControllerAudit;
import com.rhbgroup.dcpbo.user.common.BoData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/bo/workflow")
public class WorkflowOverviewController {
	
	private WorkflowOverviewService workflowOverviewService;

	private static Logger logger = LogManager.getLogger(WorkflowOverviewController.class);
	
	public WorkflowOverviewController(WorkflowOverviewService workflowOverviewService) {
		this.workflowOverviewService = workflowOverviewService;
	}

	@BoControllerAudit(eventCode = "20029")
    @GetMapping(value = "/overview/user/",
    		produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
	public BoData getWorkflowOverview(@RequestHeader("userid") int userId) {

    	return workflowOverviewService.getWorkflowOverviewService(userId);
	}
}
