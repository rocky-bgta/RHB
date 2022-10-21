package com.rhbgroup.dcpbo.customer.service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

import com.rhbgroup.dcp.data.entity.PaginatedResult;
import com.rhbgroup.dcp.data.entity.profiles.UserProfile;
import com.rhbgroup.dcpbo.customer.contract.DcpData;
import com.rhbgroup.dcpbo.customer.dto.SearchedCustomerResponse;
import com.rhbgroup.dcpbo.customer.exception.SearchCustomerException;
import com.rhbgroup.dcpbo.customer.repository.BoSearchRepository;

@RunWith(SpringRunner.class)
public class DcpCustomerPaginationDataTest {

	@TestConfiguration
	static class testConfiguration {

		@Bean
		public DcpData getDcpCustomerPaginationData() {
			return new DcpCustomerPaginationData();
		}
	}

	@Autowired
	private DcpData uut;

	@MockBean
	BoSearchRepository boSearchRepository;

	@Test(expected = SearchCustomerException.class)
    public void findByValueNotFound() {

        this.uut.findByValue("", 1);
    }
	
	@Test 
	public void findByValueFoundCustomer() {

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
		
		PaginatedResult<UserProfile> pageResult = new PaginatedResult<UserProfile>(userProfiles, 1, 1);

		BDDMockito.given(boSearchRepository.searchUserProfileByValueWithPagination(1, 15, Optional.ofNullable("muhammad.ali"))).willReturn(pageResult);

		SearchedCustomerResponse actualResult = (SearchedCustomerResponse) this.uut.findByValue("muhammad.ali", 1);

		Assert.assertThat(actualResult.getCustomer().get(0).getEmail(), Matchers.is("agentAli@gmail.com"));
		Assert.assertThat(actualResult.getCustomer().get(0).getMobileNo(), Matchers.is("0198989898"));
		BDDMockito.verify(boSearchRepository, Mockito.atLeastOnce()).searchUserProfileByValueWithPagination(1, 15, Optional.ofNullable("muhammad.ali"));
	}
	
	@Test
	public void findByValueFoundNonPremierCustomer() {

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
		userProfile.setIsPremier(false);
		userProfile.setLastLogin(new Timestamp(System.currentTimeMillis()));

		userProfiles.add(userProfile);
		
		PaginatedResult<UserProfile> pageResult = new PaginatedResult<UserProfile>(userProfiles, 1, 1);

		BDDMockito.given(boSearchRepository.searchUserProfileByValueWithPagination(1, 15, Optional.ofNullable("muhammad.ali"))).willReturn(pageResult);

		SearchedCustomerResponse actualResult = (SearchedCustomerResponse) this.uut.findByValue("muhammad.ali", 1);

		Assert.assertThat(actualResult.getCustomer().get(0).getEmail(), Matchers.is("agentAli@gmail.com"));
		Assert.assertThat(actualResult.getCustomer().get(0).getMobileNo(), Matchers.is("0198989898"));
		BDDMockito.verify(boSearchRepository, Mockito.atLeastOnce()).searchUserProfileByValueWithPagination(1, 15, Optional.ofNullable("muhammad.ali"));
	}
	
	@Test
	public void findByValueNotFoundCustomer() {

		List<UserProfile> userProfiles = new ArrayList<>();
		
		PaginatedResult<UserProfile> pageResult = new PaginatedResult<UserProfile>(userProfiles, 1, 1);

		BDDMockito.given(boSearchRepository.searchUserProfileByValueWithPagination(1, 15, Optional.ofNullable("muhammad.ali"))).willReturn(pageResult);

		this.uut.findByValue("muhammad.ali", 1);

		BDDMockito.verify(boSearchRepository, Mockito.atLeastOnce()).searchUserProfileByValueWithPagination(1, 15, Optional.ofNullable("muhammad.ali"));
	}
}