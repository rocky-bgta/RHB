package com.rhbgroup.dcpbo.customer.dto;

import com.rhbgroup.dcpbo.customer.contract.BoData;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class AuditEvent implements BoData {

    private List<EventCategory> eventCategory;
    private List<Event> event;
    private AuditPagination pagination;

    public AuditEvent() {
        eventCategory = new ArrayList<>();
        event = new ArrayList<>();
    }

    public void addEventCategory(EventCategory eventCategory) {
        this.eventCategory.add(eventCategory);
    }

    public void addEvent(Event event) {
        this.event.add(event);
    }
}
