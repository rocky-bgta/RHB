package com.rhbgroup.dcpbo.customer.dto;

import com.rhbgroup.dcpbo.customer.contract.BoData;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class BoAuditEvents implements BoData {

    private List<BoAuditEvent> event;
    private BoAuditPagination pagination;

    public BoAuditEvents() {
        event = new ArrayList<>();
        pagination = new BoAuditPagination();
    }
}
