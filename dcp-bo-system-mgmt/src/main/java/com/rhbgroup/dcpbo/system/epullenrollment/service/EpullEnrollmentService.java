package com.rhbgroup.dcpbo.system.epullenrollment.service;

import com.rhbgroup.dcp.estatement.model.EPullAutoEnrollmentRequest;
import org.springframework.http.ResponseEntity;

public interface EpullEnrollmentService {
    ResponseEntity<String> executeEpullEnrollmentProcess(String userId, EPullAutoEnrollmentRequest request);
}
