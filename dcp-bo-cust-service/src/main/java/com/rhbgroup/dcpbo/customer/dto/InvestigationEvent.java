package com.rhbgroup.dcpbo.customer.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class InvestigationEvent {

    private String auditId;
    private String eventCode;
    private String eventName;
    private String summaryDescription;
    private String username;
    private String channel;
    private String statusDescription;
    private String refId;
    private String timestamp;
}
