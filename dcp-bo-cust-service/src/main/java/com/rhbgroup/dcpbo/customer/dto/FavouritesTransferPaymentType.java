package com.rhbgroup.dcpbo.customer.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FavouritesTransferPaymentType{
    
    public String code;
    public String description;
}
