package com.rhbgroup.dcp.bo.batch.job.model;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class StagedOtherBiller {

    private Integer txnCount;
    private BigDecimal grossAmount;
    private BigDecimal merchantCharge;
    private BigDecimal tax;
    private String channel;
    private String paymentMethod;
    private String paymentMode;
    private String categoryName;
    private String billerCode;

}
