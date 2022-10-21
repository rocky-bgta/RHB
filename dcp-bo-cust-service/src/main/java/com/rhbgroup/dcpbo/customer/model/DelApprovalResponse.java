package com.rhbgroup.dcpbo.customer.model;

import com.rhbgroup.dcpbo.customer.contract.BoData;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DelApprovalResponse implements BoData {
    private Integer approvalId;
    private String isWritten;
}

