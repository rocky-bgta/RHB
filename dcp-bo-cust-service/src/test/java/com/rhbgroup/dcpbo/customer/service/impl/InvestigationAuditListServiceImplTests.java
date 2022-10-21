package com.rhbgroup.dcpbo.customer.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rhbgroup.dcpbo.customer.dto.Investigation;
import com.rhbgroup.dcpbo.customer.dto.InvestigationEvent;
import com.rhbgroup.dcpbo.customer.dto.InvestigationPagination;
import com.rhbgroup.dcpbo.customer.exception.CommonException;
import com.rhbgroup.dcpbo.customer.model.Audit;
import com.rhbgroup.dcpbo.customer.model.AuditEventConfig;
import com.rhbgroup.dcpbo.customer.repository.*;
import com.rhbgroup.dcpbo.customer.service.InvestigationAuditListService;
import org.json.JSONException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
        InvestigationAuditListService.class,
        InvestigationAuditListServiceImpl.class,
        InvestigationAuditListServiceImplTests.class,
        AuditEventsService.class
})
public class InvestigationAuditListServiceImplTests {
    @Autowired
    InvestigationAuditListService investigationAuditListService;
    @MockBean
    DcpAuditCategoryConfigRepository dcpAuditCategoryConfigRepository;
    @MockBean
    DcpAuditRepository dcpAuditRepository;
    @MockBean
    DcpAuditEventConfigRepository dcpAuditEventConfigRepository;
    @MockBean
    AuditSummaryConfigRepository auditSummaryConfigRepository;
    @MockBean
    DcpAuditFundTransferRepository dcpAuditFundTransferRepository;
    @MockBean
    DcpAuditBillPaymentRepository dcpAuditBillPaymentRepository;
    @MockBean
    DcpAuditMiscRepository dcpAuditMiscRepository;
    @MockBean
    DcpAuditProfileRepository dcpAuditProfileRepository;
    @MockBean
    DcpAuditTopupRepository dcpAuditTopupRepository;
    @MockBean
    LookupStatusRepository lookupStatusRepository;
    @MockBean
    BoRepositoryHelper boRepositoryHelper;

    Date dateNow = new Date();
    ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void getInvestigationAuditListServiceImplTestsSuccess() {
        Investigation investigation = new Investigation();
        InvestigationEvent investigationEvent = new InvestigationEvent();
        investigationEvent.setAuditId("1");
        investigationEvent.setEventCode("0");
        investigationEvent.setEventName("Event 0");
        investigationEvent.setStatusDescription("Success");
        investigationEvent.setTimestamp(String.valueOf(dateNow));
        InvestigationPagination investigationPagination = new InvestigationPagination();
        investigationPagination.setPageNum(1);
        investigationPagination.setPageIndicator("L");
        investigation.setPagination(investigationPagination);
        List<InvestigationEvent> investigationEventList = new ArrayList<>();
        investigationEventList.add(investigationEvent);
        investigation.setEvent(investigationEventList);
        investigation.setPagination(investigationPagination);

        Audit audit = new Audit();
        audit.setId(1);
        audit.setTimestamp(dateNow);
        audit.setEventCode("0");
        audit.setStatusCode("10000");
        List<Audit> auditList = new ArrayList<>();
        auditList.add(audit);
        List<String> eventCodesList = Arrays.asList("0");
        AuditEventConfig auditEventConfig = new AuditEventConfig();
        auditEventConfig.setEventName("Event 0");
        auditEventConfig.setEventCode("0");
        auditEventConfig.setDetailsTableName("DCP_AUDIT_FUND_TRANSFER");
        List<AuditEventConfig> auditEventConfigs = Arrays.asList(auditEventConfig);

        when(dcpAuditRepository.getCustomerAuditEventsByStatusCode(any(), any(), any(), any(), any())).thenReturn(auditList);
        when(boRepositoryHelper.constructEventCodesFromAudits(auditList)).thenReturn(eventCodesList);
        when(dcpAuditEventConfigRepository.getMappingsAll(eventCodesList)).thenReturn(auditEventConfigs);
        when(lookupStatusRepository.getSuccessStatusCount(anyString())).thenReturn(1);

        Investigation resp = (Investigation) investigationAuditListService.listing("0", 1, "", "", "all");
        try {
            JSONAssert.assertEquals(
                    objectMapper.writeValueAsString(investigation),
                    objectMapper.writeValueAsString(resp),
                    false
            );
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void listingByEventCodesTestsSuccess() {
        Investigation investigation = new Investigation();
        InvestigationEvent investigationEvent = new InvestigationEvent();
        investigationEvent.setAuditId("1");
        investigationEvent.setEventCode("0");
        investigationEvent.setEventName("Event 0");
        investigationEvent.setStatusDescription("Success");
        investigationEvent.setTimestamp(String.valueOf(dateNow));
        InvestigationPagination investigationPagination = new InvestigationPagination();
        investigationPagination.setPageNum(1);
        investigationPagination.setPageIndicator("L");
        investigation.setPagination(investigationPagination);
        List<InvestigationEvent> investigationEventList = new ArrayList<>();
        investigationEventList.add(investigationEvent);
        investigation.setEvent(investigationEventList);
        investigation.setPagination(investigationPagination);

        Audit audit = new Audit();
        audit.setId(1);
        audit.setTimestamp(dateNow);
        audit.setEventCode("0");
        audit.setStatusCode("10000");
        List<Audit> auditList = new ArrayList<>();
        auditList.add(audit);
        List<String> eventCodesList = Arrays.asList("0");
        AuditEventConfig auditEventConfig = new AuditEventConfig();
        auditEventConfig.setEventName("Event 0");
        auditEventConfig.setEventCode("0");
        auditEventConfig.setDetailsTableName("DCP_AUDIT_BILL_PAYMENT");
        List<AuditEventConfig> auditEventConfigs = Arrays.asList(auditEventConfig);

        when(dcpAuditRepository.getCustomerAuditEventsByEventCodeAndStatusCode(any(), any(), any(), any(), any(), any())).thenReturn(auditList);
        when(boRepositoryHelper.constructEventCodesFromAudits(auditList)).thenReturn(eventCodesList);
        when(dcpAuditEventConfigRepository.getMappingsAll(eventCodesList)).thenReturn(auditEventConfigs);
        when(lookupStatusRepository.getSuccessStatusCount(anyString())).thenReturn(1);

        Investigation resp = (Investigation) investigationAuditListService.listing("1", 1, "", "", "all");
        try {
            JSONAssert.assertEquals(
                    objectMapper.writeValueAsString(investigation),
                    objectMapper.writeValueAsString(resp),
                    false
            );
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void getInvestigationAuditListServiceImplTestsInitialFormatSuccess() {
        Investigation investigation = new Investigation();
        InvestigationPagination investigationPagination = new InvestigationPagination();
        investigationPagination.setPageNum(1);
        investigationPagination.setPageIndicator("L");
        investigation.setPagination(investigationPagination);
        List<InvestigationEvent> investigationEventList = new ArrayList<>();
        investigation.setEvent(investigationEventList);

        try {
            JSONAssert.assertEquals(
                    objectMapper.writeValueAsString(investigation),
                    objectMapper.writeValueAsString(investigationAuditListService.listing("0", 1, "", "", "all")),
                    false
            );
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test(expected = CommonException.class)
    public void getInvestigationAuditListServiceImplTestsInvalidStatusFail() {
        Investigation investigation = new Investigation();

        try {
            JSONAssert.assertEquals(
                    objectMapper.writeValueAsString(investigation),
                    objectMapper.writeValueAsString(investigationAuditListService.listing("0", 1, "", "", "x")),
                    false
            );
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
