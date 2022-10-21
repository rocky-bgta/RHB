package com.rhbgroup.dcpbo.customer.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.rhbgroup.dcp.data.entity.PaginatedResult;
import com.rhbgroup.dcp.data.entity.profiles.UserProfile;
import com.rhbgroup.dcpbo.customer.contract.BoData;
import com.rhbgroup.dcpbo.customer.contract.DcpData;
import com.rhbgroup.dcpbo.customer.dto.SearchedCustomer;
import com.rhbgroup.dcpbo.customer.dto.SearchedCustomerPagination;
import com.rhbgroup.dcpbo.customer.dto.SearchedCustomerResponse;
import com.rhbgroup.dcpbo.customer.exception.SearchCustomerException;
import com.rhbgroup.dcpbo.customer.repository.BoSearchRepository;

public class DcpCustomerPaginationData implements DcpData {

	@Autowired
	BoSearchRepository boSearchRepository;

	private final String PAGINATION_LAST = "L";
	private final String PAGINATION_NOT_LAST = "N";
	private final Integer PAGE_SIZE = 15;

	@Override
	public BoData findByValue(String value, Integer pageNo) {
		// DCPBL-23073
		List<SearchedCustomer> searchedCustomerList = new ArrayList<SearchedCustomer>();
		SearchedCustomerResponse response = new SearchedCustomerResponse();
		SearchedCustomerPagination pagination = new SearchedCustomerPagination();
		pagination.setPageIndicator(PAGINATION_NOT_LAST);
		pagination.setRecordCount(0);
		pagination.setTotalPageNo(0);
		pagination.setPageNo(pageNo);
		response.setPagination(pagination);
		response.setCustomer(searchedCustomerList);

		if (StringUtils.isBlank(value)) {
			throw new SearchCustomerException();
		}
		PaginatedResult<UserProfile> paginatedResult = this.boSearchRepository
				.searchUserProfileByValueWithPagination(pageNo, PAGE_SIZE, Optional.ofNullable(value));

		if (!paginatedResult.getResult().isEmpty()) {
			boolean isLastPage = (paginatedResult.getTotalPageCount() == pageNo) ? true : false;
			if (isLastPage) {
				pagination.setPageIndicator(PAGINATION_LAST);
			}
			pagination.setTotalPageNo(paginatedResult.getTotalPageCount());
			pagination.setRecordCount(paginatedResult.getTotalRowCount());
			for (UserProfile userProfile : paginatedResult.getResult()) {
				SearchedCustomer searchedCustomer = new SearchedCustomer();
				searchedCustomer.setCustid(String.valueOf(userProfile.getId()));
				searchedCustomer.setUsername(userProfile.getUsername());
				searchedCustomer.setName(userProfile.getName());
				searchedCustomer.setEmail(userProfile.getEmail());
				searchedCustomer.setMobileNo(userProfile.getMobileNo());
				searchedCustomer.setCisNo(userProfile.getCisNo());
				searchedCustomer.setAaoip(userProfile.getUuid());
				searchedCustomer.setIdNo(userProfile.getIdNo());
				searchedCustomer.setIdType(userProfile.getIdType());
				searchedCustomer.setStatus(userProfile.getUserStatus());
				// userProfile.getIsPremier() can be null in existing data.
				if (userProfile.getIsPremier() == null || userProfile.getIsPremier() == false) {
					searchedCustomer.setIsPremier(Boolean.FALSE.toString());
				} else {
					searchedCustomer.setIsPremier(Boolean.TRUE.toString());
				}

				searchedCustomer
						.setLastLogin(userProfile.getLastLogin() == null ? "" : "" + userProfile.getLastLogin());
				searchedCustomerList.add(searchedCustomer);
			}
		}
		return response;
	}

}
