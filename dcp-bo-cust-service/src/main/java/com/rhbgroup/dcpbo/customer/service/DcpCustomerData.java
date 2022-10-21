package com.rhbgroup.dcpbo.customer.service;

import com.rhbgroup.dcp.data.entity.profiles.UserProfile;
import com.rhbgroup.dcpbo.customer.contract.BoData;
import com.rhbgroup.dcpbo.customer.contract.DcpData;
import com.rhbgroup.dcpbo.customer.dto.SearchedCustomer;
import com.rhbgroup.dcpbo.customer.dto.SearchedCustomerPagination;
import com.rhbgroup.dcpbo.customer.dto.SearchedCustomerResponse;
import com.rhbgroup.dcpbo.customer.exception.SearchCustomerException;
import com.rhbgroup.dcpbo.customer.repository.BoSearchRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

public class DcpCustomerData implements DcpData {

    @Autowired
    BoSearchRepository boSearchRepository;

	private final String PAGINATION_LAST ="L";

    @Override
    public BoData findByValue(String value, Integer pageNo) {
    	
    	SearchedCustomerResponse response = new SearchedCustomerResponse(); 
    	SearchedCustomerPagination  pagination = new SearchedCustomerPagination();
    	
        List<UserProfile> userProfile = this.boSearchRepository.searchUserProfileByValue(value);

        List<SearchedCustomer> searchedCustomerList = new ArrayList<>();

        for(UserProfile singleProfile: userProfile){
            SearchedCustomer searchedCustomer = new SearchedCustomer();
            searchedCustomer.setCustid(singleProfile.getId() == null ? "" : "" + singleProfile.getId());
            searchedCustomer.setUsername(singleProfile.getUsername());
            searchedCustomer.setName(singleProfile.getName());
            searchedCustomer.setEmail(singleProfile.getEmail());
            searchedCustomer.setMobileNo(singleProfile.getMobileNo());
            searchedCustomer.setCisNo(singleProfile.getCisNo());
            searchedCustomer.setAaoip(singleProfile.getUuid());   //not map within UserProfile entity
            searchedCustomer.setIdType(singleProfile.getIdType());
            searchedCustomer.setIdNo(singleProfile.getIdNo());
            searchedCustomer.setStatus(singleProfile.getUserStatus());
            searchedCustomer.setIsPremier(singleProfile.getIsPremier() == null ? "" : "" + singleProfile.getIsPremier());
            searchedCustomer.setLastLogin(singleProfile.getLastLogin() == null ? "" : "" + singleProfile.getLastLogin());

            searchedCustomerList.add(searchedCustomer);
        }

        //Constructing pagination, FE doesn't support pagination, return type changed due to DCPBL-23073
        response.setCustomer(searchedCustomerList);
        pagination.setPageIndicator(PAGINATION_LAST);
        pagination.setRecordCount(searchedCustomerList.size());
        pagination.setPageNo(pageNo);
        pagination.setTotalPageNo(1);
        response.setPagination(pagination);
        return response;
    }
}
