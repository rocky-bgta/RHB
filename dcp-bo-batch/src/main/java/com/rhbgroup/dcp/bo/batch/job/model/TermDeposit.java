package com.rhbgroup.dcp.bo.batch.job.model;

import java.math.BigDecimal;
import java.sql.Timestamp;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class TermDeposit{
	
    private BigDecimal sum;
    private int count;
    private String channel;
    private String txnStatus;
    private String paymentMethod;
    private String subFunction;    
}