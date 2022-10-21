package com.rhbgroup.dcp.bo.batch.job.model;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class DcpAudit{
	
    private Integer id;
    private String eventCode;
    private Integer userId;
    private String updatedBy;
    private String username;
    private String cisNo;
    private String deviceId;
    private String statusCode;
    private String statusDescription;
    private String channel;
    private String ipAddress;
    private Timestamp timestamp;
}