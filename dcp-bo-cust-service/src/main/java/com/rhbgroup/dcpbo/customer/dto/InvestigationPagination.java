package com.rhbgroup.dcpbo.customer.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class InvestigationPagination {

    private String pageIndicator;
    private int recordCount;
    private int pageNum;
    private int totalPageNum;
}
