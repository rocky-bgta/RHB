package com.rhbgroup.dcpbo.customer.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.rhbgroup.dcpbo.customer.contract.BoData;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TelemetryData implements BoData {
    private List<Telemetry> data;
    
    private AuditPagination pagination;

}
