package com.rhbgroup.dcpbo.customer.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class DepositMcaTransactionPagination {
    private String firstKey;
    private String lastKey;
    private Boolean isLastPage;
    private Integer pageCounter;
}