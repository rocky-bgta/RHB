package com.rhbgroup.dcp.bo.batch.job.model;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class StagedJompayBiller {

    private Integer txnCount;
    private BigDecimal grossAmount;
    private BigDecimal merchantCharge;
    private BigDecimal tax;
    private String channel;
    private String paymentMethod;
    private String mainFunction;
    private String bankCodeIbg;
    private String billerCode;

}
