package com.rhbgroup.dcpbo.customer.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class TransactionAudit {

    private String channel;
    private String statusCode;
    private String statusDescription;
}