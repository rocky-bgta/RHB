package com.rhbgroup.dcpbo.customer.service;

import com.rhbgroup.dcp.data.entity.profiles.UserProfile;
import com.rhbgroup.dcpbo.customer.contract.DcpData;
import com.rhbgroup.dcpbo.customer.dto.SearchedCustomerPagination;
import com.rhbgroup.dcpbo.customer.dto.SearchedCustomerRegistrationResponse;
import com.rhbgroup.dcpbo.customer.exception.SearchCustomerException;
import com.rhbgroup.dcpbo.customer.model.RegistrationToken;
import com.rhbgroup.dcpbo.customer.repository.BoSearchRepository;
import com.rhbgroup.dcpbo.customer.repository.CustomerVerificationRepo;
import com.rhbgroup.dcpbo.customer.repository.RegistrationTokenRepo;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {DcpRegistrationData.class, RegistrationTokenRepo.class})
public class DcpRegistrationDataTest {

    private final String PAGINATION_NOT_LAST = "N";

    @Autowired
    private DcpData dcpData;

    @MockBean
    RegistrationTokenRepo registrationTokenRepo;

    @MockBean
    BoSearchRepository boSearchRepository;

    @MockBean
    CustomerVerificationRepo customerVerificationRepo;

    @Test
    public void findByAccountNumberNotFound() {
        List<Object> objList = new ArrayList<>();

        BDDMockito.given(registrationTokenRepo.findByAccountNumberOrIdNo("910", "910")).willReturn(objList);

        SearchedCustomerRegistrationResponse actualResult = (SearchedCustomerRegistrationResponse) this.dcpData.findByValue("910", 1);

        Assert.assertThat(actualResult.getCustomer().size(), Matchers.is(0));
        BDDMockito.verify(registrationTokenRepo, Mockito.atLeastOnce()).findByAccountNumberOrIdNo("910", "910");
    }

    @Test
    public void findByAccountNumberFoundCustomer() {

        SearchedCustomerRegistrationResponse response = new SearchedCustomerRegistrationResponse();
        SearchedCustomerPagination pagination = new SearchedCustomerPagination();
        pagination.setPageIndicator(PAGINATION_NOT_LAST);
        pagination.setRecordCount(0);
        pagination.setTotalPageNo(0);
        pagination.setPageNo(1);
        response.setPagination(pagination);

        List<RegistrationToken> registrationTokens = new ArrayList<>();
        RegistrationToken registrationToken = new RegistrationToken();
        Date date = new Date();

        registrationToken.setAccountNumber("RHB123");
        registrationToken.setName("RHB");
        registrationToken.setMobileNo("123456789");
        registrationToken.setCisNo("56789");
        registrationToken.setIdType("RHBID");
        registrationToken.setIdNo("RHB456");
        registrationToken.setIsPremier(Boolean.TRUE);
        registrationToken.setToken("RHBToken");
        registrationToken.setUpdatedTime(date);
        registrationToken.setUsername("RHBUser");
        registrationToken.setEmail("RHBUser@gmail.com");
        registrationToken.setId(1);

        RegistrationToken registrationToken1 = new RegistrationToken();

        registrationToken1.setAccountNumber("RHB123");
        registrationToken1.setName("RHB");
        registrationToken1.setMobileNo("123456789");
        registrationToken1.setCisNo("56789");
        registrationToken1.setIdType("RHBID");
        registrationToken1.setIdNo("RHB456");
        registrationToken1.setIsPremier(Boolean.TRUE);
        registrationToken1.setToken("RHBToken");
        registrationToken1.setUpdatedTime(date);
        registrationToken1.setUsername("RHBUser");
        registrationToken1.setEmail("RHBUser@gmail.com");
        registrationToken1.setId(1);

        registrationTokens.add(registrationToken);

        List<Object> objList = new ArrayList<>();
        for (RegistrationToken singleRegToken : registrationTokens) {
            Object[] obj = new Object[7];
            obj[0] = singleRegToken.getAccountNumber();
            obj[1] = singleRegToken.getMobileNo();
            obj[2] = singleRegToken.getName();
            obj[3] = singleRegToken.getCisNo();
            obj[4] = singleRegToken.getIdNo();
            obj[5] = singleRegToken.getIdType();
            obj[6] = singleRegToken.getIsPremier();

            objList.add(obj);
        }

        List<UserProfile> userProfiles = new ArrayList<>();
        UserProfile userProfile = new UserProfile();

        userProfile.setId(123);
        userProfile.setUsername("agentAli");
        userProfile.setName("muhammad.ali");
        userProfile.setEmail("agentAli@gmail.com");
        userProfile.setMobileNo("0198989898");
        userProfile.setCisNo("56789");
        userProfile.setIdType("KK");
        userProfile.setIdNo("891212001234");
        userProfile.setUserStatus("Kaya");
        userProfile.setIsPremier(true);
        userProfile.setLastLogin(new Timestamp(System.currentTimeMillis()));

        userProfiles.add(userProfile);


        BDDMockito.given(registrationTokenRepo.findByAccountNumberOrIdNo("RHB123", "RHB123")).willReturn(objList);
        BDDMockito.given(registrationTokenRepo.findByAcctNumberForTopValue("RHB123")).willReturn(registrationToken);
        BDDMockito.given(boSearchRepository.searchByCisno("56789")).willReturn(userProfiles);

        SearchedCustomerRegistrationResponse actualResult = (SearchedCustomerRegistrationResponse) this.dcpData.findByValue("RHB123", 1);

        Assert.assertThat(actualResult.getCustomer().size(), Matchers.is(1));
        Assert.assertThat(actualResult.getCustomer().get(0).getEmail(), Matchers.is("RHBUser@gmail.com"));
        Assert.assertThat(actualResult.getCustomer().get(0).getMobileNo(), Matchers.is("123456789"));

        BDDMockito.verify(registrationTokenRepo, Mockito.atLeastOnce()).findByAccountNumberOrIdNo("RHB123", "RHB123");
        BDDMockito.verify(registrationTokenRepo, Mockito.atLeastOnce()).findByAcctNumberForTopValue("RHB123");
        BDDMockito.verify(boSearchRepository, Mockito.atLeastOnce()).searchByCisno("56789");
    }
}
