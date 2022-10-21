package com.rhbgroup.dcpbo.customer.model;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.rhbgroup.dcpbo.customer.contract.BoData;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HirePurchaseDetails implements BoData {
    private BigDecimal totalOutstandingBalance;
    private String vehicleNo;
    private BigDecimal overdueAmount;
    private String paymentDueDate;
    private BigDecimal monthlyPayment;
    private BigDecimal loanAmount;
    private String typeOfTerm;
    private Integer originalTenure;
    private Integer remainingTenure;
    private BigDecimal interestRate;
    private BigDecimal overdueInterest;
}
