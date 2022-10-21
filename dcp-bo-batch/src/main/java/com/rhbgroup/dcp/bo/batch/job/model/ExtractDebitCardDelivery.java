package com.rhbgroup.dcp.bo.batch.job.model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ExtractDebitCardDelivery {
    private String accountNo;
    private String accountOrg;
    private String accountType;
    private String requestedDate;
    private String requestedTime;
    private String creator;
    private String offloadDate;
    private String category;
    private String requestedBy;
    private String memoLine;
    private String address1;
    private String address2;
    private String address3;
    private String city;
    private String state;
    private String postcode;
    private String country;
    private String accountCreationDate;
}
