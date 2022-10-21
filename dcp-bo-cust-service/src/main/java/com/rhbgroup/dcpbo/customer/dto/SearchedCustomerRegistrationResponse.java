package com.rhbgroup.dcpbo.customer.dto;

import java.util.ArrayList;
import java.util.List;

import com.rhbgroup.dcpbo.customer.contract.BoData;
import lombok.Getter;
import lombok.Setter;

/**
 * Response dto for search customer registration API
 * @author Suresh
 */

@Setter
@Getter
public class SearchedCustomerRegistrationResponse implements BoData{
	
	List<SearchedRegistrationCustomer> customer=new ArrayList<>();
	
	private SearchedCustomerPagination pagination;
}
