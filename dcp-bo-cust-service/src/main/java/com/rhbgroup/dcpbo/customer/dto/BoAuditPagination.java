package com.rhbgroup.dcpbo.customer.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class BoAuditPagination {

    private String pageIndicator;
    private int totalPageNo;
    private int pageNo;
    private long activityCount;
    private int pageSize;

}
