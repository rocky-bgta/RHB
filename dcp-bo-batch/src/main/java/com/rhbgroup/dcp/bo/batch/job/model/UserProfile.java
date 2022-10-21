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
public class UserProfile{
	private Integer id;
    private String name;
    private String cisNo;
    private String username;
    private String residentialState;
    private String branchIncentiveCode;
    private Boolean isStaff;
    private Boolean isPremier;
}