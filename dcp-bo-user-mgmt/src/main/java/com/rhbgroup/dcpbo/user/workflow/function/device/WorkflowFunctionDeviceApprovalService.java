package com.rhbgroup.dcpbo.user.workflow.function.device;

import com.rhbgroup.dcpbo.user.common.BoData;

public interface WorkflowFunctionDeviceApprovalService {

	public BoData getDeviceApproval(Integer approvalId, Integer userId);

}
