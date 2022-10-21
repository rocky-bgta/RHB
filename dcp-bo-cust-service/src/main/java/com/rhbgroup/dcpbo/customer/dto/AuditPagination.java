package com.rhbgroup.dcpbo.customer.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class AuditPagination {

    private String pageIndicator;
    private int activityCount;
    private int pageNum;
    private int totalPageNum;
}
