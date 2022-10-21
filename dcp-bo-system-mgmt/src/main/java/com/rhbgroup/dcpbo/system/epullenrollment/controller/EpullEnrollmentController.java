package com.rhbgroup.dcpbo.system.epullenrollment.controller;

import com.rhbgroup.dcp.estatement.model.EPullAutoEnrollmentRequest;
import com.rhbgroup.dcpbo.common.audit.BoControllerAudit;
import com.rhbgroup.dcpbo.system.epullenrollment.service.EpullEnrollmentService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/bo/system")
public class EpullEnrollmentController {
    private static Logger logger = LogManager.getLogger(EpullEnrollmentController.class);

    @Autowired
    private EpullEnrollmentService epullEnrollmentService;

    public EpullEnrollmentController(EpullEnrollmentService epullEnrollmentService) {
        this.epullEnrollmentService = epullEnrollmentService;
    }

    @BoControllerAudit(eventCode = "38001")
    @PostMapping(value = "/epull/enrollment", produces = { MediaType.APPLICATION_JSON_UTF8_VALUE })
    public ResponseEntity<String> executeEpullEnrollment(
            @RequestHeader("userProfileId") String userProfileId,
            @RequestBody EPullAutoEnrollmentRequest request) {
        logger.debug("executeEpullEnrollment()");
        logger.debug("userProfileId: " + userProfileId);
        return epullEnrollmentService.executeEpullEnrollmentProcess(userProfileId, request);
    }
}
