package com.rhbgroup.dcp.bo.batch.job.model;

import com.rhbgroup.dcp.bo.batch.framework.model.BaseModel;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PremierCustomerInfoandRMCodeTaggingDetail extends BaseModel {

    // Common
    private String filler;
    private String end;
    private String fileName;
    private String createdTime;
    private String newRmCode;
    private String oldRmCode;
    private String oldIsPremier;

    // Data for Header
    private String headerIdentifier;
    private String processingDt;
    private String systemDt;
    private String systemTime;
    private String jobName;
    private String jobNumber;
    private String jobStep;
    private String program;
    private String id;

    // Data for detail
    private String indicator;
    private String cifNo;
    private String rmCode;
    private String cisNo2;
    private String fullNm;
    private String cisNo3;
    private String idNo;
    private String cisNo4;
    private String staffInd;

    // Data for trailer
    private String trailerIndicator;
    private String trailerTotalRecord;
    private String trailerTotalAmt;
    private String ttlHash;
}