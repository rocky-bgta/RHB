package com.rhbgroup.dcp.bo.batch.job.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PrepaidReloadExtractionOut {
    private String txnTime;
    private String refNo;
    private String hostRefNo;
    private String mobileNo;
    private String prepaidProductCode;
    private String amount;
    private String filler = "\"S\"";
}
