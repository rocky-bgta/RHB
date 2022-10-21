package com.rhbgroup.dcpbo.customer.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@JsonInclude
public class EventsDetails {

    private Integer eventId;
    private String eventCode;
    private String eventName;
}
