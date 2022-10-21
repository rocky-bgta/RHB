package com.rhbgroup.dcpbo.customer.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class OlaCasaPagination {

    private Integer recordCount;
    private Integer pageNo;
    private Integer totalPageNo;
    private String pageIndicator;

}
