package com.rhbgroup.dcp.bo.batch.job.model;

import java.math.BigDecimal;
import java.sql.Timestamp;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class McaCurrency{
	
    private Integer id;
    private BigDecimal investAmt;
    private BigDecimal mcaAmt;
    private Integer count;
    private String channel;
    private String txnStatus;
    private String subFunction;
    private String toCcy;
    private String txnCcy;
    private Timestamp createdTime;
    
}
