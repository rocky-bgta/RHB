package com.rhbgroup.dcpbo.customer.service;

import com.rhbgroup.dcpbo.customer.audit.collector.AdditionalDataHolder;
import com.rhbgroup.dcpbo.customer.audit.collector.BoException;
import com.rhbgroup.dcpbo.customer.audit.collector.PutUnlockFacilityAdditionalDataRetriever;
import com.rhbgroup.dcpbo.customer.dto.UnlockData;
import com.rhbgroup.dcpbo.customer.dto.UnlockStatus;
import com.rhbgroup.dcpbo.customer.exception.CommonException;
import com.rhbgroup.dcpbo.customer.model.RegistrationToken;
import com.rhbgroup.dcpbo.customer.repository.CustomerVerificationRepo;
import com.rhbgroup.dcpbo.customer.repository.RegistrationTokenRepo;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;


@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest(classes = {PutUnlockFacilityService.class, PutUnlockFacilityServiceTest.Config.class})
public class PutUnlockFacilityServiceTest {

    @Autowired
    PutUnlockFacilityService putUnlockFacilityService;

    @MockBean
    CustomerVerificationRepo customerVerificationRepository;

    @MockBean
    RegistrationTokenRepo registrationTokenRepository;

    @TestConfiguration
    static class Config {

        @Bean
        @Primary
        public PutUnlockFacilityService getPutUnlockFacilityService() {
            return new PutUnlockFacilityService();
        }

        @Bean
        public AdditionalDataHolder getAdditionalDataHolder() {
            return new AdditionalDataHolder();
        }

        @Bean
        public PutUnlockFacilityAdditionalDataRetriever getPutUnlockFacilityAdditionalDataRetriever() {
            return new PutUnlockFacilityAdditionalDataRetriever(getAdditionalDataHolder());
        }
    }

    @Test
    public void updateUnlockSuccess() {
        RegistrationToken registrationToken = new RegistrationToken();
        UnlockStatus responseBody = new UnlockStatus();

        responseBody.setIsSuccess("1");

        registrationToken.setAccountNumber("2");
        registrationToken.setName("RHB");
        registrationToken.setIdNo("RHB123");
        registrationToken.setMobileNo("123456789");

        when(customerVerificationRepository.updateUnlockStatus(Mockito.anyString(), Mockito.anyBoolean(), Mockito.any(Date.class))).thenReturn(1);
        when(customerVerificationRepository.findByAcctNumberByUpdatedTime("1")).thenReturn("123");
        when(registrationTokenRepository.findByToken("123")).thenReturn(registrationToken);
        when(registrationTokenRepository.findByAccountNumber(Mockito.any())).thenReturn(registrationToken);

        UnlockData unlockData = new UnlockData();
        unlockData.setIdNo("1");
        unlockData.setName("Name");
        unlockData.setMobileNo("+9189");
        when(customerVerificationRepository.retrieveUnblockData(Mockito.any())).thenReturn(unlockData);

        assertEquals("1", putUnlockFacilityService.writeUnlockFacility("1").getIsSuccess());
    }

    @Test(expected = BoException.class)
    public void updateUnlockFail() {
        putUnlockFacilityService.writeUnlockFacility("1").getIsSuccess();
    }

}
