package com.rhbgroup.dcpbo.user.workflow.usergroup;

import com.rhbgroup.dcpbo.user.common.BoData;

public interface WFUserGroupService {

	public BoData getWorkflowApprovalDetail(Integer userId, int approvalId);

	public BoData approveCreate(Integer userId, WFUserGroupApprovalActionDetail wfUserGroupApprovalActionDetail);
	public BoData approveUpdate(Integer userId, WFUserGroupApprovalActionDetail wfUserGroupApprovalActionDetail);
	public BoData approveDelete(Integer userId, WFUserGroupApprovalActionDetail wfUserGroupApprovalActionDetail);
}
