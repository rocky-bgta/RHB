package com.rhbgroup.dcpbo.customer.service;

import com.rhbgroup.dcpbo.customer.contract.DcpData;
import com.rhbgroup.dcpbo.customer.contract.SearchCustomer;
import com.rhbgroup.dcpbo.customer.dto.SearchedCustomer;
import com.rhbgroup.dcpbo.customer.dto.SearchedCustomerPagination;
import com.rhbgroup.dcpbo.customer.dto.SearchedCustomerRegistrationResponse;
import com.rhbgroup.dcpbo.customer.dto.SearchedCustomerResponse;
import com.rhbgroup.dcpbo.customer.dto.SearchedRegistrationCustomer;
import com.rhbgroup.dcpbo.customer.repository.RegistrationTokenRepo;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {DcpSearchCustomer.class, DcpSearchCustomerTest.testConfiguration.class})
public class DcpSearchCustomerTest {

    @TestConfiguration
    static class testConfiguration {

        @Bean
        @Primary
        public SearchCustomer getSearchCustomer() {
            return new DcpSearchCustomer();
        }
    }

    @Autowired
    private SearchCustomer uut;

    @MockBean(name = "dcpCustomerData")
    private DcpData customerDcpData;

    @MockBean(name = "dcpAccountData")
    private DcpData accountDcpData;
    
    @MockBean(name = "dcpCustomerPaginationData")
    private DcpData customerPaginationData;
    
    @MockBean(name = "dcpRegistrationData")
    private DcpData registrationDcpData;
    
    private List<SearchedCustomer> SearchedCustomers;
    
    private List<SearchedRegistrationCustomer> SearchedRegistrationCustomers;


    private final String PAGINATION_LAST ="L";
 
    @Before 
    public void setup() {
        SearchedCustomers = new ArrayList<>();
        SearchedRegistrationCustomers = new ArrayList<>();
    }

    @Test
    public void getSearchTypeCustomer() {

        SearchedCustomer searchedCustomer = new SearchedCustomer();
        searchedCustomer.setUsername("ali");

        SearchedCustomers.add(searchedCustomer);
        SearchedCustomerResponse response = new SearchedCustomerResponse(); 
    	SearchedCustomerPagination  pagination = new SearchedCustomerPagination();
        response.setCustomer(SearchedCustomers);
        pagination.setPageIndicator(PAGINATION_LAST);
        pagination.setRecordCount(SearchedCustomers.size());
        pagination.setPageNo(1);
        pagination.setTotalPageNo(1);
        response.setPagination(pagination);
        
        BDDMockito.given(customerDcpData.findByValue("mohammad.ali",1)).willReturn(response);

        SearchedCustomerResponse actualResponse = (SearchedCustomerResponse)this.uut.getCustomerTypeValue("customer", "mohammad.ali",1);

        Assert.assertThat(actualResponse.getCustomer().get(0).getUsername(), Matchers.is("ali"));
        BDDMockito.verify(customerDcpData, Mockito.atLeastOnce()).findByValue("mohammad.ali",1);
    }

    @Test
    public void getSearchTypeAccount() {
        SearchedCustomer searchedCustomer = new SearchedCustomer();
        searchedCustomer.setName("ali");

        SearchedCustomers.add(searchedCustomer);
        SearchedCustomerResponse response = new SearchedCustomerResponse(); 
    	SearchedCustomerPagination  pagination = new SearchedCustomerPagination();
        response.setCustomer(SearchedCustomers);
        pagination.setPageIndicator(PAGINATION_LAST);
        pagination.setRecordCount(SearchedCustomers.size());
        pagination.setPageNo(1);
        pagination.setTotalPageNo(1);
        response.setPagination(pagination);
        
        BDDMockito.given(accountDcpData.findByValue("910203065250",1)).willReturn(response);

        SearchedCustomerResponse actualResponse = (SearchedCustomerResponse)this.uut.getCustomerTypeValue("account", "910203065250",1);

        Assert.assertThat(actualResponse.getCustomer().get(0).getName(), Matchers.is("ali"));
        BDDMockito.verify(accountDcpData, Mockito.atLeastOnce()).findByValue("910203065250", 1);
    }
    
    @Test
    public void getSearchRegistrationCustomer() {

        SearchedRegistrationCustomer searchedRegistrationCustomer = new SearchedRegistrationCustomer();
        searchedRegistrationCustomer.setIdNo("123");

        SearchedRegistrationCustomers.add(searchedRegistrationCustomer);
        SearchedCustomerRegistrationResponse response = new SearchedCustomerRegistrationResponse(); 
    	SearchedCustomerPagination  pagination = new SearchedCustomerPagination();
        response.setCustomer(SearchedRegistrationCustomers);
        pagination.setPageIndicator(PAGINATION_LAST);
        pagination.setRecordCount(SearchedCustomers.size());
        pagination.setPageNo(1);
        pagination.setTotalPageNo(1);
        response.setPagination(pagination);
        
        BDDMockito.given(registrationDcpData.findByValue("123",1)).willReturn(response);

        SearchedCustomerRegistrationResponse actualResponse = (SearchedCustomerRegistrationResponse)this.uut.getCustomerValue("123",1);

        Assert.assertThat(actualResponse.getCustomer().get(0).getIdNo(), Matchers.is("123"));
        BDDMockito.verify(registrationDcpData, Mockito.atLeastOnce()).findByValue("123",1);
    }
}