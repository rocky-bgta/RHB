package com.rhbgroup.dcpbo.customer.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TransactionPaymentPayment extends TransactionAudit {
    private String custId;
    private String txnTime;
    private String txnStatus;
    private String refId;
    private String fromAccountNo;
    private String fromCardNo;
    private String billerName;
    private String billerId;
    private String ref1;
    private String ref2;
    private String ref3;
    private String ref4;
    private String amount;
    private String paymentMethod;
    private String serviceCharge;
    private String mainFunction;
    private String subFunction;
    private String nickname;
    private String multiFactorAuth;
    private String isQuickPay;
}
