package com.rhbgroup.dcp.bo.batch.job.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

import com.rhbgroup.dcp.bo.batch.framework.model.BaseModel;

@Setter
@Getter
public class SampleIBGReject extends BaseModel {
    private Date date;
    private String teller;
    private String trace;
    private String ref1;
    private String name;
    private Double amount;
    private String rejectCode;
    private String accountNumber;
    private String beneName;
    private String beneAccount;
}
