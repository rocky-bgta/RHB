package com.rhbgroup.dcpbo.customer.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TransactionSearchCustomer {
    private Integer custId;
    private String username;
    private String name;
    private String email;
    private String mobileNo;
    private String cisNo;
    private String aaoip;
    private String idType;
    private String idNo;
    private String status;
    private String premier;
}
