package com.rhbgroup.dcpbo.customer.service;

import com.rhbgroup.dcp.data.entity.profiles.UserProfile;
import com.rhbgroup.dcpbo.customer.contract.DcpData;
import com.rhbgroup.dcpbo.customer.dto.SearchedCustomerPagination;
import com.rhbgroup.dcpbo.customer.dto.SearchedCustomerResponse;
import com.rhbgroup.dcpbo.customer.repository.BoSearchRepository;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit4.SpringRunner;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;


@RunWith(SpringRunner.class)
public class DcpCustomerDataTest {

    private final String PAGINATION_NOT_LAST = "N";

    @TestConfiguration
    static class testConfiguration {

        @Bean
        public DcpData getDcpCustomerData() {
            return new DcpCustomerData();
        }
    }

    @Autowired
    private DcpData uut;

    @MockBean
    BoSearchRepository boSearchRepository;

    @Test
    public void findByValueNotFound() {
        List<UserProfile> userProfile = new ArrayList<>();

        BDDMockito.given(boSearchRepository.searchUserProfileByValue("muhammad.ali")).willReturn(userProfile);

        SearchedCustomerResponse actualResult = (SearchedCustomerResponse) this.uut.findByValue("muhammad.ali", 1);

        Assert.assertThat(actualResult.getCustomer().size(), Matchers.is(0));
        BDDMockito.verify(boSearchRepository, Mockito.atLeastOnce()).searchUserProfileByValue("muhammad.ali");
    }

    @Test
    public void findByValueFoundCustomer() {

        SearchedCustomerResponse response = new SearchedCustomerResponse();
        SearchedCustomerPagination pagination = new SearchedCustomerPagination();
        pagination.setPageIndicator(PAGINATION_NOT_LAST);
        pagination.setRecordCount(0);
        pagination.setTotalPageNo(0);
        pagination.setPageNo(1);
        response.setPagination(pagination);

        List<UserProfile> userProfiles = new ArrayList<>();
        UserProfile userProfile = new UserProfile();

        userProfile.setId(123);
        userProfile.setUsername("agentAli");
        userProfile.setName("muhammad.ali");
        userProfile.setEmail("agentAli@gmail.com");
        userProfile.setMobileNo("0198989898");
        userProfile.setCisNo("1234567890123");
        userProfile.setIdType("KK");
        userProfile.setIdNo("891212001234");
        userProfile.setUserStatus("Kaya");
        userProfile.setIsPremier(true);
        userProfile.setLastLogin(new Timestamp(System.currentTimeMillis()));

        userProfiles.add(userProfile);

        BDDMockito.given(boSearchRepository.searchUserProfileByValue("muhammad.ali")).willReturn(userProfiles);

        SearchedCustomerResponse actualResult = (SearchedCustomerResponse) this.uut.findByValue("muhammad.ali", 1);

        Assert.assertThat(actualResult.getCustomer().get(0).getEmail(), Matchers.is("agentAli@gmail.com"));
        Assert.assertThat(actualResult.getCustomer().get(0).getMobileNo(), Matchers.is("0198989898"));
        BDDMockito.verify(boSearchRepository, Mockito.atLeastOnce()).searchUserProfileByValue("muhammad.ali");
    }
}