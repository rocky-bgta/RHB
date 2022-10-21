package com.rhbgroup.dcpbo.customer.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.rhbgroup.dcpbo.customer.contract.BoData;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreditCardDetailsRequest implements BoData{
	
    private String cardNo;
    private String channelFlag;
    private String connectorCode;
    private String blockCode;
    private String accountBlockCode;

}
