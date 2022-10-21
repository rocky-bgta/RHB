package com.rhbgroup.dcp.bo.batch.job.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class DcpFDeStateUpd {
    private String bodyIndicator;
    private String cisNo;
    private String date;
    private String time;
    private String statementType;
    private String filler;

    public DcpFDeStateUpd() {
        this.filler = "";
    }
}
