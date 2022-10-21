package com.rhbgroup.dcpbo.customer.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class SearchedCustomerPagination {

	private String pageIndicator;
	private int recordCount;
	private int pageNo;
	private int totalPageNo;
}
