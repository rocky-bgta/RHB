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
public class CustomerFavourites implements BoData {

    private Integer id;
    private String txnType;
    private String mainFunction;
    private Integer payeeId;
    private String payeeName;
    private String nickname;
    private BigDecimal amount;
    private String ref1;
    private String ref2;
    private String ref3;
    private String ref4;
    private Boolean isQuickLink;
    private Boolean isQuickPay;
    private String fundName;
    private String fundShortName;
    private String toAccountNumber;
    private String membersName;
    private String membershipNumber;


    public void setAmount(BigDecimal amount){

        if (String.format("%.2f",amount).equals(DefaultValue.FAVOURITE_EMPTY_AMOUNT.getValue())){
            this.amount = null;
        }else{
            this.amount = amount;
        }
    }
}