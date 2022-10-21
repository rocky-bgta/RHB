package com.rhbgroup.dcpbo.customer.contract;


public interface SearchCustomer {

    /**
     * Get by customer by parameter type and its value
     * @param searchtype customer,account
     * @param value
     * @return
     */
	 BoData getCustomerTypeValue(String searchtype, String value, Integer pageNo);
	 
	 BoData getCustomerValue(String value, Integer pageNo);
}
