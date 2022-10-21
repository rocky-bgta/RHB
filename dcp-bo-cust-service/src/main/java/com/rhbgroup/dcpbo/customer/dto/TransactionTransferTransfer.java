package com.rhbgroup.dcpbo.customer.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TransactionTransferTransfer extends TransactionAudit{
    private String eventName;
    private String txnTime;
    private String txnStatus;
    private String refId;
    private String mainFunction;
    private String fromAccountNo;
    private String duitnowCountryCode;
    private String toAccountNo;
    private String toAccountName;
    private String bank;
    private String subFunction;
    private String residentStatus;
    private String idType;
    private String idNo;
    private String amount;
    private String serviceCharge;
    private String recipientRef;
    private String otherDetails;
    private String multiFactorAuth;
    private String isQuickPay;
}
