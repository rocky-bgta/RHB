package com.rhbgroup.dcpbo.customer.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.rhbgroup.dcpbo.customer.contract.BoData;
import com.rhbgroup.dcpbo.customer.enums.DefaultValue;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FavouritesTransfer implements BoData{
    
    private String id;
    private String txnType;
    private String mainFunction;
    private String subFunction;
    private FavouritesTransferPaymentType paymentType;
    private String payeeName;
    private String toAccountNo;
    private String nickname;
    private String mobileNo;
    private String email;
    private String amount;
    private String recipientRef;
    private String isQuickLink;
    private String isQuickPay;
    private String toResidentStatus;
    private String duitnowCountryName;
    private String toldType;
    private String toldNo;

}
