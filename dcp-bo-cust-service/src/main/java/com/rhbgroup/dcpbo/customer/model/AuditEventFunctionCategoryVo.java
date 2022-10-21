package com.rhbgroup.dcpbo.customer.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
@JsonInclude
public class AuditEventFunctionCategoryVo {

    private Integer categoryId;
    private String categoryName;
    private List<EventsDetails> events;

}
