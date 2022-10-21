package com.rhbgroup.dcp.bo.batch.job.model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ExtractDailyPod {
    private String cisNo;
    private String userStatus;
    private String isStaff;
    private String podStatus;
    private String podDate;
    private String podTime;
}
