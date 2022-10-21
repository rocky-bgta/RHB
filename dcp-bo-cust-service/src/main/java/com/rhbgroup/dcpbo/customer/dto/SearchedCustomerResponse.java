package com.rhbgroup.dcpbo.customer.dto;

import java.util.List;

import com.rhbgroup.dcpbo.customer.contract.BoData;

import lombok.Getter;
import lombok.Setter;

/**
 * Response dto for search customer API
 * 
 * @author Kingsten
 */

@Setter
@Getter
public class SearchedCustomerResponse implements BoData {

	List<SearchedCustomer> customer;
	
	private SearchedCustomerPagination pagination;

}
