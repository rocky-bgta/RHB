package com.rhbgroup.dcpbo.customer.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.rhbgroup.dcpbo.customer.contract.BoData;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
@JsonInclude
public class AuditEventsResponseVo implements BoData {

    private List<AuditEventFunctionCategoryVo> category;

}
