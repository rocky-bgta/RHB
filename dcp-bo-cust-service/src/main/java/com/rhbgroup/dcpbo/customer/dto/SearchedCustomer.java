package com.rhbgroup.dcpbo.customer.dto;

import com.rhbgroup.dcpbo.customer.contract.BoData;
import lombok.Getter;
import lombok.Setter;

/**
 * Response dto for search customer API
 * @author Faisal
 */

@Setter
@Getter
public class SearchedCustomer implements BoData {

    private String custid;
    private String username;
    private String name;
    private String email;
    private String mobileNo;
    private String cisNo;
    private String aaoip;
    private String idType;
    private String idNo;
    private String status;
    private String isPremier;
    private String lastLogin;
}
