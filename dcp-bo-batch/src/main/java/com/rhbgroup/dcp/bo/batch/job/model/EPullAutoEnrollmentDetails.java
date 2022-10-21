package com.rhbgroup.dcp.bo.batch.job.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class EPullAutoEnrollmentDetails {
    private String accountType;
    private String accountNo;
    private Integer statementType;
}
