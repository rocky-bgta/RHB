package com.rhbgroup.dcp.bo.batch.job.model;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class McaTermSummary{
	
    private Integer id;
    private BigDecimal mcaAmt;
    private Integer count;
    private String channel;
    private String txnStatus;
    private String subFunction;
    private String toCcy;
    
}
