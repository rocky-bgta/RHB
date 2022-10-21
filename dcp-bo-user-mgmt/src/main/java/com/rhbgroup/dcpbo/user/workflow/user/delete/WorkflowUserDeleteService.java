package com.rhbgroup.dcpbo.user.workflow.user.delete;

import com.rhbgroup.dcpbo.user.common.BoData;

public interface WorkflowUserDeleteService {
	public BoData userDeleteApproval(int id, String reason);
}