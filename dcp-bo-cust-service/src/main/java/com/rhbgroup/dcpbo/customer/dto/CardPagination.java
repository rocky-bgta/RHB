package com.rhbgroup.dcpbo.customer.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class CardPagination {
    private String firstKey;
    private String lastKey;
    private String pageCounter;
    private Boolean isLastPage;
}