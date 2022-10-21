package com.rhbgroup.dcp.bo.batch.job.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class PrepaidReloadExtraction {
    private Date txnTime;
    private String refNo;
    private String hostRefNo;
    private String mobileNo;
    private String prepaidProductCode;
    private Double amount;
}
