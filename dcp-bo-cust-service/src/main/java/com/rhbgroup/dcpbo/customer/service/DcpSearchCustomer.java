package com.rhbgroup.dcpbo.customer.service;

import com.rhbgroup.dcpbo.customer.contract.BoData;
import com.rhbgroup.dcpbo.customer.contract.DcpData;
import com.rhbgroup.dcpbo.customer.contract.SearchCustomer;
import com.rhbgroup.dcpbo.customer.dto.SearchedCustomerResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class DcpSearchCustomer implements SearchCustomer {

    static final String CUSTOMER = "customer";
    static final String ACCOUNT = "account";
    static final String WHITELIST = "whitelist";

    @Autowired
    @Qualifier(value = "dcpCustomerData")
    private DcpData customerDcpData;

    @Autowired
    @Qualifier(value = "dcpAccountData")
    private DcpData accountDcpData;
    
    @Autowired
    @Qualifier(value = "dcpCustomerPaginationData")
    private DcpData customerPaginationData;
    
    @Autowired
    @Qualifier(value = "dcpRegistrationData")
    private DcpData registrationDcpData;
    
   
    @Override
    public BoData getCustomerTypeValue(String searchtype, String value, Integer pageNo) {
    	BoData response = new SearchedCustomerResponse();
        if(searchtype.equalsIgnoreCase(CUSTOMER)) {
        	response = this.customerDcpData.findByValue(value, 1);
        } else if(searchtype.equalsIgnoreCase(ACCOUNT)) {
        	response = this.accountDcpData.findByValue(value, 1);
        } else if(searchtype.equalsIgnoreCase(WHITELIST)) {
        	response = this.customerPaginationData.findByValue(value, pageNo);
        }
        return response;
    }
    
    @Override
    public BoData getCustomerValue(String value, Integer pageNo) {
    	BoData response;
    	response = this.registrationDcpData.findByValue(value, 1);
        return response;
    }
}
