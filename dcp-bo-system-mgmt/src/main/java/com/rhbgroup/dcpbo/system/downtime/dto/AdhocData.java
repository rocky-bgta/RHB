package com.rhbgroup.dcpbo.system.downtime.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.rhbgroup.dcpbo.system.common.BoData;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AdhocData implements BoData {
    private List<Adhoc> adhoc;
    
    private AuditPagination pagination;

}
