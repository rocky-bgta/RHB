package com.rhbgroup.dcp.bo.batch.job.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
@ToString
public class BatchStagedIBKPaymentTxnDetail extends BatchStagedIBKPaymentTxn {
    private int jobExecutionId;
    private String processDate;
    private String billerAccountNo;
    private String billerAccountName;
    private String billerCode;
    private String txnId;
    private String txnDate;
    private String txnAmount;
    private String txnType;
    private String txnDescription;
    private String billerRefNo1;
    private String billerRefNo2;
    private String billerRefNo3;
    private String txnTime;
    private String fileName;
    private Date createdTime;


    //  newly added field
    private String billerRefNo4;
    private String idNo;
    private String policyNo;
    private String userAddress1;
    private String userAddress2;
    private String userAddress3;
    private String userAddress4;
    private String userState;
    private String userCity;
    private String userPostcode;
    private String userCountry;

    private String username;
    private int lineNo;

}