package com.rhbgroup.dcpbo.user.workflow.device;

import com.rhbgroup.dcpbo.user.common.BoData;
import com.rhbgroup.dcpbo.user.workflow.function.device.WorkflowFunctionDeviceApproval;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
public class WorkflowDeviceResponse implements BoData {

    private Integer ApprovalId;
    private String idNo;
    private String username;

}
