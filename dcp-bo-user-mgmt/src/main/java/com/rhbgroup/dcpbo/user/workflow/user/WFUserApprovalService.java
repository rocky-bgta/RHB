package com.rhbgroup.dcpbo.user.workflow.user;

import com.rhbgroup.dcpbo.user.common.BoData;

public interface WFUserApprovalService {

	public BoData getWorkflowApprovalDetail(Integer userId, int approvalId);
	
	public BoData approveCreate(Integer userId, WFUserApprovalActionDetail wfUserApprovalActionDetail);
	
	public BoData approveUpdate(Integer userId, WFUserApprovalActionDetail wfUserApprovalActionDetail);
}
