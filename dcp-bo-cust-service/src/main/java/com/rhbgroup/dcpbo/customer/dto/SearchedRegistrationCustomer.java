package com.rhbgroup.dcpbo.customer.dto;

import com.rhbgroup.dcpbo.customer.contract.BoData;
import lombok.Getter;
import lombok.Setter;
import java.util.Date;

/**
 * Response dto for search customer API
 * @author Suresh
 */

@Setter
@Getter
public class SearchedRegistrationCustomer implements BoData{
	
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
	private Boolean isPremier;
    private Date lastLogin;
    private Boolean isRegistered;
    private Date lastRegistrationAttempt;
    private Boolean isLocked;
    private String acctNumber;

}
