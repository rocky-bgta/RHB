package com.rhbgroup.dcpbo.customer.service;

import com.rhbgroup.dcp.data.repository.ProfileRepository;
import com.rhbgroup.dcp.util.JsonUtil;
import com.rhbgroup.dcpbo.customer.contract.BoData;
import com.rhbgroup.dcpbo.customer.dcpbo.BoConfigGeneric;
import com.rhbgroup.dcpbo.customer.exception.CommonException;
import com.rhbgroup.dcpbo.customer.model.AuditDetailConfig;
import com.rhbgroup.dcpbo.customer.model.AuditEventConfig;
import com.rhbgroup.dcpbo.customer.model.OlaToken;
import com.rhbgroup.dcpbo.customer.repository.*;
import com.rhbgroup.dcpbo.customer.service.impl.AuditEventsService;
import com.rhbgroup.dcpbo.customer.service.impl.OlaCasaServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {OlaCasaService.class, OlaCasaServiceTest.Config.class})
public class OlaCasaServiceTest {

    @Autowired
    OlaCasaService olaCasaService;

    @MockBean
    OlaTokenRepository olaTokenRepository;

    @MockBean
    DcpAuditEventConfigRepository auditEventConfigRepository;

    @MockBean
    ProfileRepository profileRepository;

    @MockBean
    AuditDetailConfigRepo auditDetailConfigRepository;

    @MockBean
    BoConfigGenericRepository boConfigGenericRepository;

    @MockBean
    OlaTokenGroupRepository olaTokenGroupRepository;

    @MockBean
    AuditSummaryConfigRepository auditSummaryConfigRepositoryMock;

    @MockBean
    AuditEventsService auditEventsService;

    @MockBean
    UserProfileRepository userProfileRepository;
    @MockBean
    private DcpAuditFundTransferRepository dcpAuditFundTransferRepository;
    @MockBean
    private DcpAuditProfileRepository dcpAuditProfileRepository;
    @MockBean
    private DcpAuditBillPaymentRepository dcpAuditBillPaymentRepository;
    @MockBean
    private DcpAuditTopupRepository dcpAuditTopupRepository;
    @MockBean
    private DcpAuditMiscRepository dcpAuditMiscRepository;

    @TestConfiguration
    static class Config {
        @Bean
        @Primary
        public OlaCasaService getOlaCasaService() {
            return new OlaCasaServiceImpl();
        }
    }

    @Before
    public void setUp() {

        AuditEventConfig auditEventConfig = new AuditEventConfig();
        auditEventConfig.setEventCode("10009");
        auditEventConfig.setEventName("Any event");
        when(auditEventConfigRepository.findByEventCode("10009")).thenReturn(auditEventConfig);
        when(profileRepository.getCisNumberByUsername(any())).thenReturn("1234");

        List<AuditDetailConfig> auditDetailConfigList = new ArrayList<>();
        AuditDetailConfig auditDetailConfig = new AuditDetailConfig();
        auditDetailConfig.setFieldName("Full Name");
        auditDetailConfig.setPath("$.additionalData.personalDetails.fullName");
        auditDetailConfigList.add(auditDetailConfig);

        auditDetailConfig = new AuditDetailConfig();
        auditDetailConfig.setFieldName("Application Status");
        auditDetailConfig.setPath("$.additionalData.accountInformation.applicationStatus");
        auditDetailConfigList.add(auditDetailConfig);

        when(auditDetailConfigRepository.findAllByEventCode("10009")).thenReturn(auditDetailConfigList);

        BoConfigGeneric boConfigGeneric = new BoConfigGeneric();
        boConfigGeneric.setConfigDesc("30");
        when(boConfigGenericRepository.findFirstByConfigTypeAndConfigCode("provide_assistance", "dcp_olacasa_validity")).thenReturn(boConfigGeneric);

        OlaToken olaToken = new OlaToken();
        olaToken.setToken("1234");
        olaToken.setAuditAdditionalData("{\n" +
                "    \"additionalData\": {\n" +
                "        \"personalDetails\": {\n" +
                "            \"fullName\": \"xxx\",\n" +
                "            \"mobileNo\": \"999\",\n" +
                "            \"email\": \"xxx@gmail.com\"\n" +
                "        },\n" +
                "        \"contactDetails\": {\n" +
                "            \"addressLine1\": \"xxx\",\n" +
                "            \"city\": \"xxxx\"\n" +
                "        }\n" +
                "    }\n" +
                " }\n");
        olaToken.setStatus("N");
        olaToken.setCreatedTime("1970-01-19 22:34:10.731");
        when(olaTokenRepository.findFirstByToken("1234")).thenReturn(olaToken);
    }

    @Test
    public void getAuditDetailsTestSuccess() {
        String response = "{\"activity\":{\"auditId\":0,\"eventCode\":\"10009\",\"eventName\":\"Any event\",\"timestamp\":\"1970-01-19 22:34:10.731\",\"statusSummary\":\"Failed\",\"cisNo\":\"1234\",\"details\":[{\"fieldName\":\"Full Name\"},{\"fieldName\":\"Application Status\",\"value\":\"Expired_pending application\"}]}}";
        BoData boData = olaCasaService.getAuditDetails("1234");
        assertEquals(response, JsonUtil.objectToJson(boData));
    }

    @Test(expected = CommonException.class)
    public void getAuditDetailsTestInvalidToken() {
        olaCasaService.getAuditDetails("12345");
    }

    @Test(expected = CommonException.class)
    public void getAuditDetailsTestNullAuditEventConfig() {
        when(auditEventConfigRepository.findByEventCode("10009")).thenReturn(null);
        olaCasaService.getAuditDetails("1234");
    }

    @Test(expected = CommonException.class)
    public void getAuditDetailsTestNullAuditDetailEventConfig() {
        when(auditDetailConfigRepository.findAllByEventCode("10009")).thenReturn(null);
        olaCasaService.getAuditDetails("1234");
    }

    @Test(expected = CommonException.class)
    public void getAuditDetailsTestNullBoConfigGeneric() {
        when(boConfigGenericRepository.findFirstByConfigTypeAndConfigCode("provide_assistance", "dcp_olacasa_validity")).thenReturn(null);
        olaCasaService.getAuditDetails("1234");
    }
}
