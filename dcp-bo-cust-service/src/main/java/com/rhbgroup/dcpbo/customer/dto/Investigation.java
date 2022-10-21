package com.rhbgroup.dcpbo.customer.dto;

import com.rhbgroup.dcpbo.customer.contract.BoData;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class Investigation implements BoData {

    private List<InvestigationEvent> event;
    private InvestigationPagination pagination;

    public Investigation() {
        event = new ArrayList<>();
    }

    public void addEvent(InvestigationEvent event) {
        this.event.add(event);
    }
}
