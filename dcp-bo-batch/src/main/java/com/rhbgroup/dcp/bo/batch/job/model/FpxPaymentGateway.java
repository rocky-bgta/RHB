package com.rhbgroup.dcp.bo.batch.job.model;

import java.math.BigDecimal;
import java.sql.Timestamp;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class FpxPaymentGateway{
	
    private BigDecimal txnAmount;
    private BigDecimal merchantCharge;
    private BigDecimal tax;
    private int txnCount;
    private String channel;
    private String txnStatus;   
}