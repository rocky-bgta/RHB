package com.rhbgroup.dcpbo.customer.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.rhbgroup.dcpbo.customer.dto.TransactionAudit;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TransactionTopupTopup extends TransactionAudit {
    private String custId;
    private String txnTime;
    private String txnStatus;
    private String refId;
    private String fromAccountNo;
    private String fromCardNo;
    private String billerName;
    private String nickname;
    private String ref1;
    private String amount;
    private String serviceCharge;
    private String mainFunction;
    private String isQuickPay;
    private String multiFactorAuth;
}
