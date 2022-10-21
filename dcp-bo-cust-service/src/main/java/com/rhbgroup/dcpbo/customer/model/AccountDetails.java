package com.rhbgroup.dcpbo.customer.model;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.rhbgroup.dcp.eai.adaptors.accountinquiry.model.response.DcpAccountStatus;
import com.rhbgroup.dcp.eai.adaptors.accountinquiry.model.response.DcpDebitCard;
import com.rhbgroup.dcp.eai.adaptors.accountinquiry.model.response.DcpProduct;
import com.rhbgroup.dcpbo.customer.contract.BoData;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AccountDetails implements BoData {
    private String nickname;
    private String accountHolderName;
    private String ownershipType;
    private BigDecimal currentBalance;
    private BigDecimal availableBalance;
    private BigDecimal overdraft;
    private BigDecimal float1Day;
    private DcpProduct product;
    private DcpAccountStatus accountStatus;
    private DcpDebitCard debitCard;
}
