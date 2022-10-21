package com.rhbgroup.dcpbo.system.downtime.dto;

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
