package com.rhbgroup.dcpbo.user.workflow.device;

import com.rhbgroup.dcpbo.common.audit.BoControllerAudit;
import com.rhbgroup.dcpbo.user.common.BoData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/bo/workflow/device")
public class WorkflowDeviceController {

    private WorkflowDeviceService workflowDeviceService;

    public WorkflowDeviceController(WorkflowDeviceService workflowDeviceService){
        this.workflowDeviceService = workflowDeviceService;
    }

    @BoControllerAudit(eventCode = "30035", value = "boAuditAdditionalDataRetriever")
    @PutMapping(value = "/delete/approval/{approvalId}", produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
    public BoData getWorkflowFunction(@PathVariable("approvalId") Integer approvalId,
                                      @RequestBody WorkflowDeviceRequest workflowDeviceRequest,
                                      @RequestHeader("userid") Integer userId) {

        return workflowDeviceService.approveDeletion(workflowDeviceRequest.getReason(), approvalId, userId);
    }
}
