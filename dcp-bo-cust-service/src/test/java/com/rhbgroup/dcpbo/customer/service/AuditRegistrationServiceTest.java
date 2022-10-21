package com.rhbgroup.dcpbo.customer.service;

import com.rhbgroup.dcpbo.customer.contract.BoData;
import com.rhbgroup.dcpbo.customer.dto.AuditDetailsActivity;
import com.rhbgroup.dcpbo.customer.dto.AuditEvent;
import com.rhbgroup.dcpbo.customer.exception.CommonException;
import com.rhbgroup.dcpbo.customer.model.AuditDetailConfig;
import com.rhbgroup.dcpbo.customer.model.RegistrationToken;
import com.rhbgroup.dcpbo.customer.repository.AuditDetailConfigRepo;
import com.rhbgroup.dcpbo.customer.repository.RegistrationTokenRepo;
import com.rhbgroup.dcpbo.customer.service.impl.AuditRegistrationServiceImpl;
import com.rhbgroup.dcpbo.customer.util.Util;
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

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {AuditRegistrationService.class, AuditRegistrationServiceTest.Config.class})
public class AuditRegistrationServiceTest {

    @Autowired
    AuditRegistrationService auditRegistrationService;

    @MockBean
    private RegistrationTokenRepo registrationTokenRepositoryMock;

    @MockBean
    AuditDetailConfigRepo auditDetailConfigRepositoryMock;

    @TestConfiguration
    static class Config {
        @Bean
        @Primary
        public AuditRegistrationService getAuditRegistrationService() {
            return new AuditRegistrationServiceImpl();
        }
    }

    @Test
    public void getAuditRegistrationDetailsTest() throws IOException {
        String token = "1";
        RegistrationToken registrationToken = new RegistrationToken();
        registrationToken.setChannel("Test");
        String jsonStr = Util.loadJsonResourceFile(getClass(), "AuditRegistrationToken.json");

        registrationToken.setAuditAdditionalData(jsonStr);
        registrationToken.setUpdatedTime(new Date(System.currentTimeMillis()));
        registrationToken.setDeviceId("234");
        registrationToken.setIpAddress("a");
        registrationToken.setUsername("a");
        registrationToken.setCisNo("5678");

        when(registrationTokenRepositoryMock.findByToken(Mockito.any())).thenReturn(registrationToken);

        List<AuditDetailConfig> auditDetailConfigList = new ArrayList<>();
        AuditDetailConfig auditDetailConfig = new AuditDetailConfig();
        auditDetailConfig.setPath("$.additionalData.regCardLoanNo");
        auditDetailConfig.setFieldName("Card/Loan No");
        auditDetailConfigList.add(auditDetailConfig);
        auditDetailConfig = new AuditDetailConfig();
        auditDetailConfig.setPath("$.request.idNo");
        auditDetailConfig.setFieldName("ID Number");
        auditDetailConfigList.add(auditDetailConfig);
        auditDetailConfig = new AuditDetailConfig();
        auditDetailConfig.setPath("$.request.mobileNo");
        auditDetailConfig.setFieldName("Mobile Number");
        auditDetailConfigList.add(auditDetailConfig);

        when(auditDetailConfigRepositoryMock.findAllByEventCode(Mockito.any())).thenReturn(auditDetailConfigList);

        BoData response = (BoData) auditRegistrationService.getAuditRegistrationDetails(token);
        AuditDetailsActivity res = (AuditDetailsActivity) response;
        assertEquals("10000", res.getActivity().getEventCode());
        assertEquals("User Registration", res.getActivity().getEventName());
        assertEquals("234", res.getActivity().getDeviceId());
        assertEquals("Test", res.getActivity().getChannel());
        assertEquals("10000", res.getActivity().getStatusCode());
        assertEquals("Success", res.getActivity().getStatusDescription());
        assertEquals("Success", res.getActivity().getStatusSummary());
        assertEquals("a", res.getActivity().getIp());
        assertEquals("a", res.getActivity().getUsername());
        assertEquals("5678", res.getActivity().getCisNo());

    }

    @Test(expected = CommonException.class)
    public void getAuditRegistrationDetailsInvalidRegistrationTokenFailTest() {
        String token = "1";
        RegistrationToken registrationToken = null;

        when(registrationTokenRepositoryMock.findByToken(Mockito.any())).thenReturn(registrationToken);
        auditRegistrationService.getAuditRegistrationDetails(token);
    }

    @Test(expected = CommonException.class)
    public void getAuditRegistrationDetailsInvalidUpdatedTimeFailTest() throws IOException {
        String token = "1";
        RegistrationToken registrationToken = new RegistrationToken();
        registrationToken.setChannel("Test");
        String jsonStr = Util.loadJsonResourceFile(getClass(), "AuditRegistrationToken.json");

        registrationToken.setAuditAdditionalData(jsonStr);
        registrationToken.setUpdatedTime(null);
        registrationToken.setDeviceId("234");
        registrationToken.setIpAddress("a");
        registrationToken.setUsername("a");
        registrationToken.setCisNo("5678");

        when(registrationTokenRepositoryMock.findByToken(Mockito.any())).thenReturn(registrationToken);
        auditRegistrationService.getAuditRegistrationDetails(token);
    }

    @Test
    public void getAuditRegistrationListTest() throws IOException {
        String cisNo = "1";
        Integer pageNo = 1;
        String frDate = "12-01-20";
        String toDate = "13-01-20";
        Integer customerId = 1;
        Timestamp timeStamp = new Timestamp(System.currentTimeMillis());


        String jsonStr = Util.loadJsonResourceFile(getClass(), "AuditRegistrationToken.json");

        List<RegistrationToken> registrationTokens = new ArrayList<>();
        RegistrationToken registrationToken = new RegistrationToken();

        registrationToken.setAuditAdditionalData(jsonStr);
        registrationToken.setUpdatedTime(timeStamp);
        registrationToken.setChannel("Test");
        registrationToken.setToken("5678");

        registrationTokens.add(registrationToken);

        List<Object> objList = new ArrayList<>();
        for (RegistrationToken singleRegToken : registrationTokens) {
            Object[] obj = new Object[7];
            obj[0] = singleRegToken.getAuditAdditionalData();
            obj[1] = singleRegToken.getChannel();
            obj[2] = singleRegToken.getUpdatedTime();
            obj[3] = singleRegToken.getToken();
            objList.add(obj);
        }

        when(registrationTokenRepositoryMock.findByCisNo(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyInt(), Mockito.anyInt())).thenReturn(objList);
        when(registrationTokenRepositoryMock.getCountByCisNo(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(1);


        BoData response = auditRegistrationService.listing(cisNo, pageNo, frDate, toDate);
        AuditEvent res = (AuditEvent) response;
        assertNotNull(res.getEventCategory());
        assertNotNull(res.getEvent());
    }

    @Test(expected = CommonException.class)
    public void getAuditRegistrationListInvalidRegistrationTokenFailTest() {
        String cisNo = "1";
        Integer pageNo = 1;
        String frDate = "";
        String toDate = "";
        Integer customerId = 1;
        List<Object> objList = new ArrayList<>();

        when(registrationTokenRepositoryMock.findByCisNo(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyInt(), Mockito.anyInt())).thenReturn(objList);
        auditRegistrationService.listing(cisNo, pageNo, frDate, toDate);
    }

}
