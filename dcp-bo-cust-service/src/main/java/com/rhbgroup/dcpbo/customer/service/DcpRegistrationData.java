package com.rhbgroup.dcpbo.customer.service;

import com.rhbgroup.dcp.data.entity.profiles.UserProfile;
import com.rhbgroup.dcpbo.customer.contract.BoData;
import com.rhbgroup.dcpbo.customer.contract.DcpData;
import com.rhbgroup.dcpbo.customer.dto.SearchedCustomerPagination;
import com.rhbgroup.dcpbo.customer.dto.SearchedCustomerRegistrationResponse;
import com.rhbgroup.dcpbo.customer.dto.SearchedRegistrationCustomer;
import com.rhbgroup.dcpbo.customer.dto.TransformToRegistrationToken;
import com.rhbgroup.dcpbo.customer.model.RegistrationToken;
import com.rhbgroup.dcpbo.customer.repository.BoSearchRepository;
import com.rhbgroup.dcpbo.customer.repository.CustomerVerificationRepo;
import com.rhbgroup.dcpbo.customer.repository.RegistrationTokenRepo;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DcpRegistrationData implements DcpData {

    @Autowired
    RegistrationTokenRepo registrationTokenRepository;

    @Autowired
    BoSearchRepository boSearchRepository;

    @Autowired
    CustomerVerificationRepo customerVerificationRepository;

    public static final String PAGINATION_LAST = "L";

    @Override
    public BoData findByValue(String value, Integer pageNo) {

        SearchedCustomerRegistrationResponse response = new SearchedCustomerRegistrationResponse();
        SearchedCustomerPagination pagination = new SearchedCustomerPagination();
        String aaoip = null;
        Date updatedTime = new Date();
        String userName = null;
        String email = null;

        List<Object> registrationToken = registrationTokenRepository.findByAccountNumberOrIdNo(value, value);

        if (registrationToken.isEmpty()) return response;

        List<TransformToRegistrationToken> transformToRegistrationToken = new ArrayList<>();
        for (Object singleRegToken : registrationToken) {
            Object[] obj = (Object[]) singleRegToken;
            TransformToRegistrationToken transformToSingleRegistrationToken = new TransformToRegistrationToken();
            transformToSingleRegistrationToken.setAccountNumber((String) obj[0]);
            transformToSingleRegistrationToken.setMobileNo((String) obj[1]);
            transformToSingleRegistrationToken.setName((String) obj[2]);
            transformToSingleRegistrationToken.setCisNo((String) obj[3]);
            transformToSingleRegistrationToken.setIdNo((String) obj[4]);
            transformToSingleRegistrationToken.setIdType((String) obj[5]);
            transformToSingleRegistrationToken.setIsPremier((Boolean) obj[6]);
            transformToRegistrationToken.add(transformToSingleRegistrationToken);
        }

        if (!registrationToken.isEmpty()) {
            RegistrationToken regTopToken = registrationTokenRepository.findByAcctNumberForTopValue(transformToRegistrationToken.get(0).getAccountNumber());
            aaoip = regTopToken.getToken();
            updatedTime = regTopToken.getUpdatedTime();
            userName = regTopToken.getUsername();
            email = regTopToken.getEmail();
        }

        List<SearchedRegistrationCustomer> searchedRegistrationCustomerList = new ArrayList<>();

        for (TransformToRegistrationToken singleRegistrationToken : transformToRegistrationToken) {
            SearchedRegistrationCustomer searchedRegistrationCustomer = new SearchedRegistrationCustomer();
            searchedRegistrationCustomer.setAcctNumber(singleRegistrationToken.getAccountNumber());
            searchedRegistrationCustomer.setName(singleRegistrationToken.getName());
            searchedRegistrationCustomer.setMobileNo(singleRegistrationToken.getMobileNo());
            searchedRegistrationCustomer.setCisNo(singleRegistrationToken.getCisNo());
            searchedRegistrationCustomer.setIdType(singleRegistrationToken.getIdType());
            searchedRegistrationCustomer.setIdNo(singleRegistrationToken.getIdNo());
            searchedRegistrationCustomer.setIsPremier(singleRegistrationToken.getIsPremier());

            searchedRegistrationCustomer.setAaoip(aaoip);
            searchedRegistrationCustomer.setLastRegistrationAttempt(updatedTime);
            searchedRegistrationCustomer.setUsername(userName);
            searchedRegistrationCustomer.setEmail(email);

            List<UserProfile> userProfile = this.boSearchRepository.searchByCisno(singleRegistrationToken.getCisNo());

            if (!userProfile.isEmpty()) {
                for (UserProfile singleProfile : userProfile) {
                    searchedRegistrationCustomer.setStatus(singleProfile.getUserStatus());
                    searchedRegistrationCustomer.setLastLogin(singleProfile.getLastLogin());
                    searchedRegistrationCustomer.setIsRegistered(Boolean.TRUE);
                }
            } else {
                searchedRegistrationCustomer.setIsRegistered(Boolean.FALSE);
            }

            String customerAttemptCount = this.customerVerificationRepository.findByInputNumber(searchedRegistrationCustomer.getAcctNumber());

            if (customerAttemptCount != null) {
                int count = Integer.parseInt(customerAttemptCount);
                if (count >= 3) {
                    searchedRegistrationCustomer.setIsLocked(Boolean.TRUE);
                } else {
                    searchedRegistrationCustomer.setIsLocked(Boolean.FALSE);
                }
            } else {
                searchedRegistrationCustomer.setIsLocked(Boolean.FALSE);
            }

            searchedRegistrationCustomerList.add(searchedRegistrationCustomer);
        }

        response.setCustomer(searchedRegistrationCustomerList);
        pagination.setPageIndicator(PAGINATION_LAST);
        pagination.setRecordCount(searchedRegistrationCustomerList.size());
        pagination.setPageNo(pageNo);
        pagination.setTotalPageNo(1);
        response.setPagination(pagination);
        return response;
    }

}

