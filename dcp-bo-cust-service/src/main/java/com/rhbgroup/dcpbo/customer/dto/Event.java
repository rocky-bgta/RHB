package com.rhbgroup.dcpbo.customer.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Event {

    private String eventId;
    private String eventCode;
    private String eventCategoryId;
    private String eventName;
    private String status;
    private String channel;
    private String description;
    private String timestamp;
    private String refId;
}
