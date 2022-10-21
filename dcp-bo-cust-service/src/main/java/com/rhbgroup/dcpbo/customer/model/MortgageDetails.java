package com.rhbgroup.dcpbo.customer.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.rhbgroup.dcpbo.customer.contract.BoData;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Json Format Structure:
 *
 * {
 * 	"paymentDueDate": null,
 * 	"monthlyPayment": null,
 * 	"accountNo": "1111",
 * 	"overdueAmount": null,
 * 	"loanAmount": null,
 * 	"remainingAmount": null,
 * 	"typeOfTerm": null,
 * 	"originalTenure": null,
 * 	"remainingTenure": null,
 * 	"interestRate": null,
 * 	"isRedrawalAvailable": null,
 * 	"redrawalAmount": null,
 * 	"accountOwnership": null,
 * 	"accountHolder": [
 *                {
 * 			"name": "Azizul"
 *        },
 *        {
 * 			"name": "Faisal"
 *        }
 * 	]
 * }
 */

@Setter
@Getter
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MortgageDetails implements BoData {
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
    private Boolean isRedrawalAvailable;
    private String redrawalAmount;
    private String accountOwnership;
    private List<AccountHolder> accountHolder;
}