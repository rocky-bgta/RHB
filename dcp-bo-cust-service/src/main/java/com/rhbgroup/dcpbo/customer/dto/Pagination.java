package com.rhbgroup.dcpbo.customer.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Pagination {

    private String firstKey;
    private String lastKey;
    int pageCounter;
    Boolean isLastPage;
}