package com.rhbgroup.dcpbo.customer.controller;

import com.rhbgroup.dcpbo.customer.audit.collector.BoControllerAudit;
import com.rhbgroup.dcpbo.customer.contract.BoData;
import com.rhbgroup.dcpbo.customer.service.AuditEventsFunctionService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@RestController
@RequestMapping(path = "/bo/investigation")
public class AuditEventsFunctionController {

    private AuditEventsFunctionService auditEventsFunctionService;

    private static Logger logger = LogManager.getLogger(AuditEventsFunctionController.class);

    public AuditEventsFunctionController(AuditEventsFunctionService auditEventsFunctionService) {
        this.auditEventsFunctionService = auditEventsFunctionService;
    }

    @BoControllerAudit(eventCode = "60001")
    @GetMapping(value = "/audit/events", produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
    public BoData getAuditEventsFunction(@RequestParam(value = "eventCode", defaultValue = "") String eventCode){

        return auditEventsFunctionService.getAuditEventslisting(eventCode);

    }
}
