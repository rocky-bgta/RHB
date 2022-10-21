package com.rhbgroup.dcpbo.customer.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.rhbgroup.dcpbo.customer.contract.BoData;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AsbDetails implements BoData{
    private String paymentDueDate;
    private String monthlyPayment;
    private String accountNo;
    private String overdueAmount;
    private String loanAmount;
    private String remainingAmount;
    private String typeOfTerm;
    private String originalTenure;
    private String remainingTenure;
    private String interestRate;
    private String accountOwnership;
    private String accountHolderName;
}