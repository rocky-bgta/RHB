package com.rhbgroup.dcp.bo.batch.job.model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ExtractMonthlySubsByState {
    private String rowNumber;
    private String idType;
    private String idNo;
    private String accountName;
    private String dateOfBirth;
    private String activeStatus;
    private String recordStatus;
    private String idIndicator;
    private String stateCode;
    private String state;
}