package com.rhbgroup.dcp.bo.batch.job.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PrepaidReloadFileFromIBK {
    int jobExecutionId;
    String fileName;
    String paymentType;
    String txnTime;
    String refNo;
    String hostRefNo;
    String mobileNo;
    String prepaidProductCode;
    String amount;
    String txnStatus;
    String createdTime;
}
