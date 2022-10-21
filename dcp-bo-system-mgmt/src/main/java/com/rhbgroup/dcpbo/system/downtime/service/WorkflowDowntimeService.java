package com.rhbgroup.dcpbo.system.downtime.service;

import org.springframework.http.ResponseEntity;

import com.rhbgroup.dcpbo.system.common.BoData;
import com.rhbgroup.dcpbo.system.downtime.vo.ApproveDowntimeAdhocRequestVo;
import com.rhbgroup.dcpbo.system.downtime.vo.DeleteDowntimeAdhocApprovalRequestVo;
import com.rhbgroup.dcpbo.system.downtime.vo.UpdateDowntimeAdhocApprovalRequestVo;

public interface WorkflowDowntimeService {
       
	public ResponseEntity<BoData> getApproval(Integer approvalId, Integer userId);
	public ResponseEntity<BoData> workflowApproveAdhocDowntime(Integer approvalId, ApproveDowntimeAdhocRequestVo request, String userId);
	public BoData deleteApproval(Integer approvalId, DeleteDowntimeAdhocApprovalRequestVo deleteDowntimeApprovalRequestVo, String userId);
    public BoData updateApproval(Integer approvalId, UpdateDowntimeAdhocApprovalRequestVo updateDowntimeAdhocApprovalRequest, String userId);

}
