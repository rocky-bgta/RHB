package com.rhbgroup.dcpbo.customer.service.impl;

import com.rhbgroup.dcp.data.entity.profiles.UserProfile;
import com.rhbgroup.dcp.data.repository.ProfileRepository;
import com.rhbgroup.dcpbo.customer.dto.CustomerProfile;
import com.rhbgroup.dcpbo.customer.model.AppConfig;
import com.rhbgroup.dcpbo.customer.model.BoLookUp;
import com.rhbgroup.dcpbo.customer.model.CardProduct;
import com.rhbgroup.dcpbo.customer.model.CardProfile;
import com.rhbgroup.dcpbo.customer.repository.AppConfigRepository;
import com.rhbgroup.dcpbo.customer.repository.BoLookupRepository;
import com.rhbgroup.dcpbo.customer.repository.CardProductRepository;
import com.rhbgroup.dcpbo.customer.repository.CardProfileRepository;
import com.rhbgroup.dcpbo.customer.service.CustomerProfileService;
import com.rhbgroup.dcpbo.customer.utils.CustomerServiceConstant;
import com.rhbgroup.dcpbo.customer.vo.CustomerProfileVo;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
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

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {CustomerProfileService.class, CustomerProfileServiceImplTests.Config.class})
public class CustomerProfileServiceImplTests {

    private static Logger logger = LogManager.getLogger(CustomerProfileServiceImplTests.class);

    @Autowired
    CustomerProfileService customerProfileService;

    @MockBean
    ProfileRepository profileRepositoryMock;

    @MockBean
    BoLookupRepository bolookupRepositoryMock;

    @MockBean
    AppConfigRepository appConfigRepositoryMock;

    @MockBean
    CardProfileRepository cardProfileRepositoryMock;

    @MockBean
    CardProductRepository cardProductRepositoryMock;

    @TestConfiguration
    static class Config {
        @Bean
        @Primary
        public CustomerProfileService getCustomerProfileService() {
            return new CustomerProfileServiceImpl();
        }
    }

    int customerId = 1;

    @Test
    public void getCustomerProfileTest_Successful() throws Throwable {
        String lcode = CustomerServiceConstant.L_CODE;
        String tpin = CustomerServiceConstant.TPIN_CODE;
        String creditCard = CustomerServiceConstant.CREDIT_CARD;
        String prePaidCard = CustomerServiceConstant.PREPAID_CARD;
        String debitCrd = CustomerServiceConstant.DEBIT_CARD;

        getCustomerProfileTest_Success(customerId, lcode, creditCard);
        getCustomerProfileTest_Success(customerId, tpin, debitCrd);
        getCustomerProfileTest_Success(customerId, tpin, prePaidCard);
    }

    public void getCustomerProfileTest_Success(int customerId, String code, String cardType) {
        logger.debug("getCustomerProfileTest_LCode_Success()");
        logger.debug("    customerProfileService: " + customerProfileService);
        logger.debug("    profileRepositoryMock: " + profileRepositoryMock);
        logger.debug("    lookupStatusRepositoryMock: " + bolookupRepositoryMock);
        logger.debug("    appConfigRepositoryMock: " + appConfigRepositoryMock);
        logger.debug("    cardProfileRepositoryMock: " + cardProfileRepositoryMock);
        logger.debug("    cardProductRepositoryMock: " + cardProductRepositoryMock);

        UserProfile userProfile = getUserProfile("1234567890", new Integer(1), "XYZ", code);
        when(profileRepositoryMock.getUserProfileByUserId(Mockito.anyInt())).thenReturn(userProfile);
        assertNotNull(userProfile);
        assertEquals("1234567890", userProfile.getCisNo());
        assertEquals(new Integer(1), userProfile.getId());
        assertEquals("XYZ", userProfile.getUsername());
        assertEquals(code, userProfile.getUserStatus());

        BoLookUp lookupSt = getBoLookUpdata(CustomerServiceConstant.ACTION_REQUIRED, code, "Success!", "Success!", new Integer(2));
        when(bolookupRepositoryMock.getLookUp(Mockito.anyString(), Mockito.anyString())).thenReturn(lookupSt);
        assertNotNull(lookupSt);
        assertEquals(CustomerServiceConstant.ACTION_REQUIRED, lookupSt.getType());
        assertEquals(code, lookupSt.getCode());
        assertEquals("Success!", lookupSt.getTitleEn());
        assertEquals("Success!", lookupSt.getDescriptionEn());
        assertEquals("Unblock", lookupSt.getButtonEn());
        assertEquals(new Integer(2), lookupSt.getId());
        assertEquals("action-required", CustomerServiceConstant.ACTION_REQUIRED);

        AppConfig appConfig = getAppConfig("pay.transaction.pollCount", "120");
        when(appConfigRepositoryMock.getParameterValue(Mockito.anyString())).thenReturn(appConfig);
        assertNotNull(appConfig);
        assertEquals("pay.transaction.pollCount", appConfig.getParameterKey());
        assertEquals("120", appConfig.getParameterValue());
        assertEquals(new Integer(2), appConfig.getId());


        List<CardProfile> crdPrflList = getCardProfileList();
        when(cardProfileRepositoryMock.getCardProfile(Mockito.anyInt(), Mockito.anyInt())).thenReturn(crdPrflList);
        assertNotNull(crdPrflList);
        boolean checkVal = false;

        for (CardProfile crdPrfle : crdPrflList) {
            assertNotNull(crdPrfle.getId());
            assertNotNull(crdPrfle.getFailedCardTpinCount());
            assertNotNull(crdPrfle.getCardNo());
            assertNotNull(crdPrfle.getUserId());
            assertNotNull(crdPrfle.getNickName());
            assertTrue(crdPrfle.getIsDefaultAccount());
            assertFalse(crdPrfle.getIsHidden());
            assertNotNull(crdPrfle.getCardProductId());
            assertNotNull(crdPrfle.getConnectorCode());
            assertTrue(crdPrfle.getIsActvBlocked());
            assertNotNull(crdPrfle.getAccountNo());

            checkVal = true;
        }
        assertTrue(checkVal);

        CardProduct cardProduct = getCardProduct(new Integer(2), cardType);
        when(cardProductRepositoryMock.getProductCategory(Mockito.anyString())).thenReturn(cardProduct);
        assertNotNull(cardProduct);
        assertEquals(new Integer(2), cardProduct.getId());
        assertEquals(cardType, cardProduct.getCategory());

        CustomerProfileVo rtn = (CustomerProfileVo) customerProfileService.getCustomerProfile(customerId);
        assertNotNull(rtn);
        List<CustomerProfile> list = rtn.getActions();
        logger.debug("    CustomerProfileVo: " + list);
        assertNotNull(list);

    }


    @Test(expected = Exception.class)
    public void getCustomerProfileTest_UnSuccessful() throws Throwable {

        when(profileRepositoryMock.getUserProfileByUserId(Mockito.anyInt())).thenReturn(null);
        when(bolookupRepositoryMock.getLookUp(Mockito.anyString(), Mockito.anyString())).thenReturn(null);
        when(appConfigRepositoryMock.getParameterValue(Mockito.anyString())).thenReturn(null);
        when(cardProfileRepositoryMock.getCardProfile(Mockito.anyInt(), Mockito.anyInt())).thenReturn(null);
        when(cardProductRepositoryMock.getProductCategory(Mockito.anyString())).thenReturn(null);
        when(profileRepositoryMock.getUserProfileByUserId(null)).thenReturn(null);
        customerProfileService.getCustomerProfile(23);
    }

    @Test(expected = Exception.class)
    public void getCustomerProfileCustomerIdTest_UnSuccessful() throws Throwable {
        customerProfileService.getCustomerProfile(null);
    }

    public UserProfile getUserProfile(String cisNo, Integer id, String uName, String uStatus) {
        UserProfile userProfile = new UserProfile();
        userProfile.setCisNo(cisNo);
        userProfile.setId(id);
        userProfile.setUsername(uName);
        userProfile.setUserStatus(uStatus);
        return userProfile;
    }

    public BoLookUp getBoLookUpdata(String type, String code, String tit, String dec, Integer id) {
        BoLookUp lookupSt = new BoLookUp();
        lookupSt.setType(type);
        lookupSt.setCode(code);
        lookupSt.setTitleEn(tit);
        lookupSt.setDescriptionEn(dec);
        lookupSt.setId(id);
        lookupSt.setButtonEn("Unblock");
        return lookupSt;
    }

    public CardProfile getCardProfile(Integer id, Integer pincnt, String crdNo, Integer userId) {
        CardProfile cardProfile = new CardProfile();
        cardProfile.setId(id);
        cardProfile.setFailedCardTpinCount(pincnt);
        cardProfile.setCardNo(crdNo);
        cardProfile.setUserId(new Integer(userId));
        cardProfile.setNickName("Credit Card");
        cardProfile.setIsDefaultAccount(true);
        cardProfile.setIsHidden(false);
        cardProfile.setCardProductId(72);
        cardProfile.setConnectorCode("345");
        cardProfile.setIsActvBlocked(true);
        cardProfile.setAccountNo("3434");

        return cardProfile;
    }

    public List<CardProfile> getCardProfileList() {
        List<CardProfile> crdPrflList = new ArrayList<>();
        CardProfile cardProfile1 = getCardProfile(customerId, 5, "4363452300007100", new Integer("3194"));
        CardProfile cardProfile2 = getCardProfile(customerId, 6, "5401190730002000", new Integer("3193"));
        crdPrflList.add(cardProfile1);
        crdPrflList.add(cardProfile2);
        return crdPrflList;
    }

    public CardProduct getCardProduct(Integer id, String categry) {
        CardProduct cardProduct = new CardProduct();
        cardProduct.setCategory(categry);
        cardProduct.setId(id);
        return cardProduct;
    }

    public AppConfig getAppConfig(String parameterKey, String parameterValue) {
        AppConfig appConfig = new AppConfig();
        appConfig.setParameterKey(parameterKey);
        appConfig.setParameterValue(parameterValue);
        appConfig.setId(new Integer(2));
        return appConfig;
    }

}
