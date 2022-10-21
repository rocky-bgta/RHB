package com.rhbgroup.dcpbo.customer.dto;

import com.rhbgroup.dcpbo.customer.contract.BoData;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class OlaCasaAuditEventResponse implements BoData {

    private List<Event> event;
    private List<EventCategory> eventCategory;
    private AuditPagination pagination;

    public OlaCasaAuditEventResponse() {
        event = new ArrayList<>();
        eventCategory = new ArrayList<>();
    }

}
