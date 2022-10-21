package com.rhbgroup.dcpbo.customer.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DelApprovalRequest {
    private String customerId;
    private String username;
    private Integer deviceId;
    private String name;
    private String os;
    private Integer functionId;
}