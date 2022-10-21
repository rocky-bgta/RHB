package com.rhbgroup.dcpbo.customer.dto;


import java.math.BigDecimal;
import java.util.List;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.rhbgroup.dcp.model.Capsule;
import com.rhbgroup.dcp.transformer.ruledriven.util.GSONUtil;
import com.rhbgroup.dcpbo.customer.contract.BoData;
import com.rhbgroup.dcpbo.customer.contract.CapsuleToDto;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class TermDepositDetails implements BoData {

    private String accountNo;
    private int visualPercentage;
    private String effectiveDate;
    private String maturityDate;
    @Setter(AccessLevel.NONE)
    private BigDecimal currentBalance;
    @Setter(AccessLevel.NONE)
    private BigDecimal projectedValue;
    private double interestRate;
    private String ownershipType;
    private String typeOfTenure;
    private int tenure;
    private String lastRenewalDate;
    @Setter(AccessLevel.NONE)
    private BigDecimal accruedInterest;
    private List<AccountHolder> accountHolder;

    public void setCurrentBalance(String currentBalance) {
        this.currentBalance = new BigDecimal(currentBalance);
    }

    public void setProjectedValue(String projectedValue) {
        this.projectedValue = new BigDecimal(projectedValue);
    }

    public void setAccruedInterest(String accruedInterest) {
        this.accruedInterest = new BigDecimal(accruedInterest);
    }

}
