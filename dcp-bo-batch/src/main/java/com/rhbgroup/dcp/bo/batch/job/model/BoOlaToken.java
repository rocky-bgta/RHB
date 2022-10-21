package com.rhbgroup.dcp.bo.batch.job.model;

import java.math.BigDecimal;
import java.sql.Timestamp;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class BoOlaToken{
    private Integer id;
    private String productType;
    private String channel;
    private String activationOption;
    private String accountBranch;
    private String region;
}