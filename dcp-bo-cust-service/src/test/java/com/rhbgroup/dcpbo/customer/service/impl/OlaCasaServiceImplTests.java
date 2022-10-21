package com.rhbgroup.dcpbo.customer.service.impl;

import com.rhbgroup.dcp.data.repository.ProfileRepository;
import com.rhbgroup.dcpbo.customer.dcpbo.BoConfigGeneric;
import com.rhbgroup.dcpbo.customer.dto.OlaCasaAuditEventResponse;
import com.rhbgroup.dcpbo.customer.model.AuditSummaryConfig;
import com.rhbgroup.dcpbo.customer.model.OlaToken;
import com.rhbgroup.dcpbo.customer.repository.*;
import com.rhbgroup.dcpbo.customer.service.OlaCasaService;
import org.apache.http.util.Asserts;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;
import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
        OlaCasaService.class,
        OlaCasaServiceImpl.class,
        AuditEventsService.class
})
public class OlaCasaServiceImplTests {

    @Autowired
    AuditEventsService auditEventsService;

    @Autowired
    OlaCasaService olaCasaService;

    @MockBean
    AuditDetailConfigRepo auditDetailConfigRepoMock;

    @MockBean
    AuditSummaryConfigRepository auditSummaryConfigRepositoryMock;

    @MockBean
    DcpAuditEventConfigRepository dcpAuditEventConfigRepositoryMock;

    @MockBean
    BoConfigGenericRepository boConfigGenericRepositoryMock;

    @MockBean
    OlaTokenGroupRepository olaTokenGroupRepository;

    @MockBean
    OlaTokenRepository olaTokenRepositoryMock;

    @MockBean
    UserProfileRepository userProfileRepository;

    @MockBean
    ProfileRepository profileRepositoryMock;

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

    @Before
    public void setup() {

        List<OlaToken> olaTokens = new ArrayList<>();
        generateOlaTokens(olaTokens);

        BoConfigGeneric configGeneric = new BoConfigGeneric();
        configGeneric.setConfigType("provide_assistance");
        configGeneric.setConfigCode("dcp_olacasa_validity");
        configGeneric.setConfigDesc("30");

        List<AuditSummaryConfig> auditSummaryConfigs = new ArrayList<>();
        AuditSummaryConfig auditSummaryConfig = new AuditSummaryConfig();
        auditSummaryConfig.setPath("$.additionalData.accountDetails.productName");
        auditSummaryConfigs.add(auditSummaryConfig);
        auditSummaryConfig = new AuditSummaryConfig();
        auditSummaryConfig.setPath("$.additionalData.accountDetails.accountNo");
        auditSummaryConfigs.add(auditSummaryConfig);
        auditSummaryConfig = new AuditSummaryConfig();
        auditSummaryConfig.setPath("$.additionalData.accountDetails.productType");
        auditSummaryConfigs.add(auditSummaryConfig);

        Page<OlaToken> olaTokenGroupPage = new PageImpl(olaTokens);
        when(olaTokenRepositoryMock.findByIdNoAndCreatedTimeAfterAndCreatedTimeBeforeOrderByCreatedTimeDesc(
                anyString(), anyString(), anyString(), any(Pageable.class)))
                .thenReturn(olaTokenGroupPage);
        when(boConfigGenericRepositoryMock.findFirstByConfigTypeAndConfigCode("provide_assistance", "dcp_olacasa_validity"))
                .thenReturn(configGeneric);
        when(auditSummaryConfigRepositoryMock.findPathsByEventCode(anyString())).thenReturn(auditSummaryConfigs);
    }

    @Test
    public void olaTokenStatusTest() {
        OlaCasaAuditEventResponse test = (OlaCasaAuditEventResponse) olaCasaService.listOlaCasaEvents(
                "880501455883", "2021-04-01T00:00:00 08:00", "2021-04-01T23:59:59 08:00", 1);
        assertEquals("Expired_pending application", test.getEvent().get(0).getStatus());
        assertEquals("Fail", test.getEvent().get(1).getStatus());
        assertEquals("Fail", test.getEvent().get(2).getStatus());
        assertEquals("Expired_pending application", test.getEvent().get(3).getStatus());
        assertEquals("Expired_pending activation", test.getEvent().get(4).getStatus());
        assertEquals("Successful", test.getEvent().get(5).getStatus());
        assertEquals("Expired_pending activation", test.getEvent().get(6).getStatus());
    }

    @Test
    public void olaTokenDescriptionTest() {
        OlaCasaAuditEventResponse test = (OlaCasaAuditEventResponse) olaCasaService.listOlaCasaEvents(
                "880501455883", "2021-04-01T00:00:00 08:00", "2021-04-01T23:59:59 08:00", 1);
        assertEquals("Expired_pending activation", test.getEvent().get(6).getStatus());
        assertTrue(!test.getEvent().get(6).getDescription().isEmpty());
        assertTrue(test.getEvent().get(6).getDescription().contains("162233589015"));
        assertTrue(test.getEvent().get(6).getDescription().contains("SAVINGS"));
    }

    private void generateOlaTokens(List<OlaToken> olaTokens) {

        OlaToken olaToken = new OlaToken();
        olaToken.setId(1000);
        olaToken.setUsername("batak123");
        olaToken.setToken("5kdfj3432JWEFWFrrr33335");
        olaToken.setDeviceId("0005");
        olaToken.setChannel("MBK");
        olaToken.setName("Karim Musa");
        olaToken.setEmail("test@gmail.com");
        olaToken.setMobileNo("0192355597");
        olaToken.setIdType("IC");
        olaToken.setIdNo("880501455883");
        olaToken.setStatus("N");
        olaToken.setCreatedTime("2021-02-25 16:30:43.443");
        olaToken.setUpdatedTime("2021-02-25 16:30:43.443");
        olaTokens.add(olaToken);

        olaToken = new OlaToken();
        olaToken.setId(1000);
        olaToken.setUsername("batak123");
        olaToken.setToken("ewrwerERerrq233w32rewWEee2");
        olaToken.setDeviceId("0005");
        olaToken.setChannel("MBK");
        olaToken.setName("Karim Musa");
        olaToken.setEmail("test@gmail.com");
        olaToken.setMobileNo("0192355597");
        olaToken.setIdType("IC");
        olaToken.setIdNo("880501455883");
        olaToken.setStatus("P");
        olaToken.setAmlScreeningResult("Positive");
        olaToken.setCreatedTime("2021-03-25 16:30:43.443");
        olaToken.setUpdatedTime("2021-03-25 16:30:43.443");
        olaTokens.add(olaToken);

        olaToken = new OlaToken();
        olaToken.setId(1000);
        olaToken.setUsername("batak123");
        olaToken.setToken("ewrwerERerrq233w32rewWEee2");
        olaToken.setDeviceId("0005");
        olaToken.setChannel("MBK");
        olaToken.setName("Karim Musa");
        olaToken.setEmail("test@gmail.com");
        olaToken.setMobileNo("0192355597");
        olaToken.setIdType("IC");
        olaToken.setIdNo("880501455883");
        olaToken.setStatus("P");
        olaToken.setAssessmentRiskLevel("High");
        olaToken.setAmlScreeningResult("Positive");
        olaToken.setCreatedTime("2021-03-25 16:30:43.443");
        olaToken.setUpdatedTime("2021-03-25 16:30:43.443");
        olaTokens.add(olaToken);

        olaToken = new OlaToken();
        olaToken.setId(1000);
        olaToken.setUsername("batak123");
        olaToken.setToken("ewrwerERerrq233w32rewWEee2");
        olaToken.setDeviceId("0005");
        olaToken.setChannel("MBK");
        olaToken.setName("Karim Musa");
        olaToken.setEmail("test@gmail.com");
        olaToken.setMobileNo("0192355597");
        olaToken.setIdType("IC");
        olaToken.setIdNo("880501455883");
        olaToken.setStatus("N");
        olaToken.setCreatedTime("2021-03-25 16:30:43.443");
        olaToken.setUpdatedTime("2021-03-25 16:30:43.443");
        olaTokens.add(olaToken);

        olaToken = new OlaToken();
        olaToken.setId(1000);
        olaToken.setUsername("batak123");
        olaToken.setToken("ewrwerERerrq233w32rewWEee2");
        olaToken.setDeviceId("0005");
        olaToken.setChannel("MBK");
        olaToken.setName("Karim Musa");
        olaToken.setEmail("test@gmail.com");
        olaToken.setMobileNo("0192355597");
        olaToken.setIdType("IC");
        olaToken.setIdNo("880501455883");
        olaToken.setStatus("I");
        olaToken.setCreatedTime("2021-03-25 16:30:43.443");
        olaToken.setUpdatedTime("2021-03-25 16:30:43.443");
        olaTokens.add(olaToken);

        olaToken = new OlaToken();
        olaToken.setId(1000);
        olaToken.setUsername("batak123");
        olaToken.setToken("ewrwerERerrq233w32rewWEee2");
        olaToken.setDeviceId("0005");
        olaToken.setChannel("MBK");
        olaToken.setName("Karim Musa");
        olaToken.setEmail("test@gmail.com");
        olaToken.setMobileNo("0192355597");
        olaToken.setIdType("IC");
        olaToken.setIdNo("880501455883");
        olaToken.setStatus("C");
        olaToken.setCreatedTime("2021-03-25 16:30:43.443");
        olaToken.setUpdatedTime("2021-03-25 16:30:43.443");
        olaTokens.add(olaToken);

        olaToken = new OlaToken();
        olaToken.setId(1000);
        olaToken.setUsername("batak123");
        olaToken.setToken("ewrwerERerrq233w32rewWEee2");
        olaToken.setDeviceId("0005");
        olaToken.setChannel("MBK");
        olaToken.setName("Karim Musa");
        olaToken.setEmail("test@gmail.com");
        olaToken.setMobileNo("0192355597");
        olaToken.setIdType("IC");
        olaToken.setIdNo("880501455883");
        olaToken.setAuditAdditionalData("{      \"accountDetails\": {        \"productName\": \"SAVINGS\",        \"accountNo\": 162233589015,        \"productType\": \"MK\"      }    }");
        olaToken.setStatus("I");
        olaToken.setCreatedTime("2021-01-25 16:30:43.443");
        olaToken.setUpdatedTime("2021-01-25 16:30:43.443");
        olaTokens.add(olaToken);

    }
}
