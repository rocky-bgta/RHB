package com.rhbgroup.dcpbo.customer.service.impl;

import com.rhbgroup.dcpbo.customer.model.Audit;
import com.rhbgroup.dcpbo.customer.model.AuditEventConfig;
import com.rhbgroup.dcpbo.customer.model.DcpAuditCategoryConfig;
import com.rhbgroup.dcpbo.customer.repository.*;
import com.rhbgroup.dcpbo.customer.service.DcpCustomerAuditService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.mockito.Matchers.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
        DcpCustomerAuditService.class,
        DcpCustomerAuditServiceImpl.class,
        AuditEventsService.class,
})
public class DcpCustomerAuditServiceImplTests {

    @Autowired
    DcpCustomerAuditService dcpCustomerAuditService;

    @MockBean
    AuditSummaryConfigRepository auditSummaryConfigRepository;
    @MockBean
    DcpAuditBillPaymentRepository dcpAuditBillPaymentRepository;
    @MockBean
    DcpAuditCategoryConfigRepository dcpAuditCategoryConfigRepository;
    @MockBean
    DcpAuditEventConfigRepository dcpAuditEventConfigRepository;
    @MockBean
    DcpAuditFundTransferRepository dcpAuditFundTransferRepository;
    @MockBean
    DcpAuditMiscRepository dcpAuditMiscRepository;
    @MockBean
    DcpAuditProfileRepository dcpAuditProfileRepository;
    @MockBean
    DcpAuditRepository dcpAuditRepository;
    @MockBean
    DcpAuditTopupRepository dcpAuditTopupRepository;
    @MockBean
    LookupStatusRepository lookupStatusRepository;

    @MockBean
    BoRepositoryHelper boRepositoryHelper;

    Date dateNow = new Date();

    @Test
    public void testBoDataListingSuccess() {
        String frDateStr = "2019-04-18T00:00:00+08:00";
        String toDateStr = "2019-04-19T00:00:00+08:00";
        int customerId = 1;
        Integer pageNo = 10;

        Audit audit = new Audit();
        audit.setId(1);
        audit.setTimestamp(dateNow);
        audit.setEventCode("0");
        audit.setStatusCode("10000");
        List<Audit> auditList = Arrays.asList(audit);

        List<String> eventCodesList = Arrays.asList("0");

        AuditEventConfig auditEventConfig = new AuditEventConfig();
        auditEventConfig.setEventName("Event 0");
        auditEventConfig.setEventCode("0");
        auditEventConfig.setDetailsTableName("DCP_AUDIT_PROFILE");
        List<AuditEventConfig> auditEventConfigs = Arrays.asList(auditEventConfig);

        DcpAuditCategoryConfig dcpAuditCategoryConfig = new DcpAuditCategoryConfig();
        dcpAuditCategoryConfig.setId(1);
        dcpAuditCategoryConfig.setCategoryName("Category 01");
        List<DcpAuditCategoryConfig> eventCategories = Arrays.asList(dcpAuditCategoryConfig);

        when(dcpAuditRepository.getCustomerAuditEvents(anyInt(), any(), any(), anyInt(), anyInt())).thenReturn(auditList);
        when(boRepositoryHelper.constructEventCodesFromAudits(auditList)).thenReturn(eventCodesList);
        when(dcpAuditEventConfigRepository.getMappingsAll(eventCodesList)).thenReturn(auditEventConfigs);
        when(dcpAuditCategoryConfigRepository.findAll()).thenReturn(eventCategories);

        dcpCustomerAuditService.listing(frDateStr, toDateStr, customerId, "all", pageNo);
    }

}
