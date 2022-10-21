package com.rhbgroup.dcp.bo.batch.job.model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class SampleIBGRejectOut {
    private String date;
    private String teller;
    private String trace;
    private String ref1;
    private String name;
    private String amount;
    private String rejectCode;
    private String accountNumber;
    private String beneName;
    private String beneAccount;
}
