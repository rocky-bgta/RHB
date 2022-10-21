
package com.rhbgroup.dcpbo.system.downtime.whitelist.service;

import com.rhbgroup.dcpbo.system.common.BoData;
import org.springframework.http.ResponseEntity;

/**
 *
 * @author Faizal Musa
 */
public interface WorkflowDowntimeWhitelistService {
    
    public ResponseEntity<BoData> getWhitelistApproval(Integer approvalId, Integer boUserId);
}
