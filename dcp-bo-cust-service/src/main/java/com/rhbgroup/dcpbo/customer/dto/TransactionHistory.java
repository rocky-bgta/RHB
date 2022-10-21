package com.rhbgroup.dcpbo.customer.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class TransactionHistory {

    private String description;
    private String txnDate;
    private double amount;
}
