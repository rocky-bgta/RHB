package com.rhbgroup.dcpbo.customer.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.rhbgroup.dcp.model.Capsule;
import com.rhbgroup.dcp.transformer.ruledriven.util.GSONUtil;
import com.rhbgroup.dcpbo.customer.contract.BoData;
import com.rhbgroup.dcpbo.customer.contract.CapsuleToDto;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class PersonalLoan implements BoData {
    private String remainingAmount;
    private String overdueAmount;
    private String paymentDueDate;
    private String monthlyPayment;
    private String loanAmount;
    private String typeOfTerm;
    private String originalTenure;
    private String remainingTenure;
    private String interestRate;
}
