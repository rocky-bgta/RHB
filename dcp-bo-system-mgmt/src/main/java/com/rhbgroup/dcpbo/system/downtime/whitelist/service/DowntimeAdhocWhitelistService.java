package com.rhbgroup.dcpbo.system.downtime.whitelist.service;

import org.springframework.http.ResponseEntity;

import com.rhbgroup.dcpbo.system.common.BoData;
import com.rhbgroup.dcpbo.system.downtime.whitelist.vo.AddDowntimeAdhocWhitelistRequest;
import com.rhbgroup.dcpbo.system.downtime.whitelist.vo.DeleteDowntimeAdhocWhitelistRequest;

public interface DowntimeAdhocWhitelistService {
	
	public ResponseEntity<BoData> addDowntimeAdhocWhitelist(AddDowntimeAdhocWhitelistRequest request, Integer userId);

	public ResponseEntity<BoData> deleteDowntimeAdhocWhitelist(DeleteDowntimeAdhocWhitelistRequest request, Integer userId, Integer id);

        public ResponseEntity<BoData> getDowntimeAdhocWhitelist(Integer pageNo, Integer userId);
}
