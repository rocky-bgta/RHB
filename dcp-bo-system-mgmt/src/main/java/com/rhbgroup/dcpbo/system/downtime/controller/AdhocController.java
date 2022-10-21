package com.rhbgroup.dcpbo.system.downtime.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.rhbgroup.dcpbo.common.audit.BoControllerAudit;
import com.rhbgroup.dcpbo.system.common.BoData;
import com.rhbgroup.dcpbo.system.downtime.service.DowntimeAdhocService;
import com.rhbgroup.dcpbo.system.downtime.vo.AddDowntimeAdhocRequestVo;
import com.rhbgroup.dcpbo.system.downtime.vo.DeleteDowntimeAdhocRequestVo;
import com.rhbgroup.dcpbo.system.downtime.vo.UpdateDowntimeAdhocRequestVo;

@RestController
@RequestMapping(value = "/bo/system")
public class AdhocController {

	private static Logger logger = LogManager.getLogger(AdhocController.class);

	@Autowired
	private DowntimeAdhocService downtimeAdhocService;

	public AdhocController(DowntimeAdhocService downtimeAdhocService) {
		this.downtimeAdhocService = downtimeAdhocService;
	}
	
	@BoControllerAudit(eventCode = "40030",value = "boAuditAdditionalDataRetriever")
	@PostMapping(value = "/downtime/adhoc", produces = { MediaType.APPLICATION_JSON_UTF8_VALUE })
	public ResponseEntity<BoData> addDowntimeAdhoc(@RequestBody AddDowntimeAdhocRequestVo request,
			@RequestHeader(value = "userid",defaultValue = "0") String userid) {
		return downtimeAdhocService.addDowntimeAdhoc(request, userid);
	}

	@BoControllerAudit(eventCode = "40033", value = "boAuditAdditionalDataRetriever")
	@PutMapping(value = "/downtime/adhoc/{id}", produces = { MediaType.APPLICATION_JSON_UTF8_VALUE })
	public ResponseEntity<BoData> updateDowntimeAdhoc(@PathVariable("id") int id, @RequestBody UpdateDowntimeAdhocRequestVo request,
			@RequestHeader(value = "userid",defaultValue = "0") String userid) {
		return downtimeAdhocService.updateDowntimeAdhoc(request, id, userid);
	}

	@BoControllerAudit(eventCode = "40036", value = "boAuditAdditionalDataRetriever")
	@DeleteMapping(value = "/downtime/adhoc/{id}", produces = { MediaType.APPLICATION_JSON_UTF8_VALUE })
	public ResponseEntity<BoData> deleteDowntimeAdhoc(@PathVariable("id") int id, @RequestBody DeleteDowntimeAdhocRequestVo request,
			@RequestHeader(value = "userid",defaultValue = "0") String userid) {
		return downtimeAdhocService.deleteDowntimeAdhoc(request, id, userid);
	}
	
	@BoControllerAudit(eventCode = "40039")
	@GetMapping(path = "/downtime/adhoc", produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
	public @ResponseBody BoData getDowntimeAdhocs(
			@RequestParam(value = "pageNo", required = true, defaultValue = "1") Integer pageNo,
			@RequestParam(value = "startTime", required = false, defaultValue = "") String startTime,
			@RequestParam(value = "endTime", required = false, defaultValue = "") String endTime,
			@RequestParam(value = "adhocCategory", required = false, defaultValue = "ALL") String adhocCategory,
			@RequestParam(value = "status", required = false, defaultValue = "ALL") String status) {
        return downtimeAdhocService.getDowntimeAdhocs(pageNo, startTime, endTime, adhocCategory, status);
	}

        @GetMapping(value = "/downtime/adhoc/adhocCategory", produces = { MediaType.APPLICATION_JSON_UTF8_VALUE })
	public ResponseEntity<BoData> getAdhocCategoryList() {
		logger.debug("getAdhocCategoryList()");
		return downtimeAdhocService.getAdhocCategoryList();
	}
	
	@GetMapping(value = "/downtime/adhoc/adhocType", produces = { MediaType.APPLICATION_JSON_UTF8_VALUE })
	public @ResponseBody BoData getAdhocType(
			@RequestParam(value = "category", required = false, defaultValue = "ALL") String category) {
		return downtimeAdhocService.getAdhocTypesList(category);
	}

}
