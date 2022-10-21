package com.rhbgroup.dcpbo.system.downtime.whitelist.service;

import com.rhbgroup.dcpbo.system.common.BoData;
import com.rhbgroup.dcpbo.system.downtime.whitelist.vo.ApproveAddDowntimeAdhocWhitelistRequest;
import com.rhbgroup.dcpbo.system.downtime.whitelist.vo.ApproveDeleteDowntimeAdhocWhitelistRequest;
import org.springframework.http.ResponseEntity;

public interface WorkflowDowntimeAdhocWhitelistService {
       
	public BoData approveAddDowntimeWhitelist(Integer approvalId, ApproveAddDowntimeAdhocWhitelistRequest approveAddDowntimeAdhocWhitelistRequest, String userId);
	public BoData approveDeleteDowntimeWhitelist(Integer approvalId, ApproveDeleteDowntimeAdhocWhitelistRequest approveDeleteDowntimeAdhocWhitelistRequest, String userId);
        public ResponseEntity<BoData> getWhitelistApproval(Integer approvalId, Integer boUserId, String actionType);
}
