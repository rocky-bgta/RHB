package com.rhbgroup.dcpbo.system.downtime.whitelist.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rhbgroup.dcpbo.common.audit.BoControllerAudit;
import com.rhbgroup.dcpbo.system.downtime.whitelist.service.DowntimeAdhocWhitelistService;
import com.rhbgroup.dcpbo.system.downtime.whitelist.vo.AddDowntimeAdhocWhitelistRequest;
import com.rhbgroup.dcpbo.system.downtime.whitelist.vo.DeleteDowntimeAdhocWhitelistRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping(value = "/bo/system")
public class DowntimeAdhocWhitelistController {

	@Autowired
	private DowntimeAdhocWhitelistService downtimeAdhocWhitelistService;
        
	public DowntimeAdhocWhitelistController(DowntimeAdhocWhitelistService downtimeAdhocWhitelistService) {
		this.downtimeAdhocWhitelistService = downtimeAdhocWhitelistService;
	}
	
	@BoControllerAudit(eventCode = "40040", value = "boAuditAdditionalDataRetriever")
	@PostMapping(value = "/downtime/adhoc/whitelist", produces = { MediaType.APPLICATION_JSON_UTF8_VALUE })
	public ResponseEntity<?> addDowntimeAdhocWhitelist(@RequestBody AddDowntimeAdhocWhitelistRequest request,
			@RequestHeader(value = "userid", required = true) Integer userId) {
		return downtimeAdhocWhitelistService.addDowntimeAdhocWhitelist(request, userId);
	}
	
	@BoControllerAudit(eventCode = "40043", value = "boAuditAdditionalDataRetriever")
	@DeleteMapping(value = "/downtime/adhoc/whitelist/{id}", produces = { MediaType.APPLICATION_JSON_UTF8_VALUE })
	public ResponseEntity<?> deleteDowntimeAdhocWhitelist(@PathVariable("id") int id, @RequestBody DeleteDowntimeAdhocWhitelistRequest request,
			@RequestHeader(value = "userid", required = true) Integer userid) {
		return downtimeAdhocWhitelistService.deleteDowntimeAdhocWhitelist(request, userid, id);
	}
	
        @BoControllerAudit(eventCode = "40046")
        @GetMapping(value = "/downtime/adhoc/whitelist", produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
        public ResponseEntity<?> getDowntimeAdhocWhitelist(@RequestParam(name = "pageNo") Integer pageNo, 
                @RequestHeader(value = "userid", required = true) Integer userId){
            
            return downtimeAdhocWhitelistService.getDowntimeAdhocWhitelist(pageNo, userId);
        }
}
