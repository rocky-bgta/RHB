package com.rhbgroup.dcpbo.customer.service.impl;

import com.rhbgroup.dcpbo.customer.dcpbo.AuditDetailsVW;
import com.rhbgroup.dcpbo.customer.model.AuditSummaryConfig;
import com.rhbgroup.dcpbo.customer.repository.*;
import com.rhbgroup.dcpbo.customer.service.InvestigationAuditListCSVService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
        InvestigationAuditListCSVService.class,
        InvestigationAuditListCsvServiceImpl.class,
        InvestigationAuditListCsvServiceImplTests.class,
        AuditEventsService.class
})
public class InvestigationAuditListCsvServiceImplTests {

    @Autowired
    InvestigationAuditListCSVService investigationAuditListCsvService;

    @MockBean
    AuditDetailsVWRepository auditDetailsVWRepository;
    @MockBean
    AuditSummaryConfigRepository auditSummaryConfigRepository;
    @MockBean
    DcpAuditEventConfigRepository dcpAuditEventConfigRepository;
    @MockBean
    DcpAuditFundTransferRepository dcpAuditFundTransferRepository;
    @MockBean
    DcpAuditProfileRepository dcpAuditProfileRepository;
    @MockBean
    DcpAuditBillPaymentRepository dcpAuditBillPaymentRepository;
    @MockBean
    DcpAuditTopupRepository dcpAuditTopupRepository;
    @MockBean
    DcpAuditMiscRepository dcpAuditMiscRepository;
    @MockBean
    LookupStatusRepository lookupStatusRepository;

    @MockBean
    BoRepositoryHelper boRepositoryHelper;

    @Test
    public void getInvestigationAuditListServiceImplTestsSuccess() throws IOException {
        MockHttpServletResponse mockHttpServletResponse = new MockHttpServletResponse();

        AuditDetailsVW auditDetailsVW = new AuditDetailsVW();
        auditDetailsVW.setEvent_code("0");
        auditDetailsVW.setDetails("Audit Details");
        List<AuditDetailsVW> auditDetailsVWList = Arrays.asList(auditDetailsVW);

        AuditSummaryConfig auditSummaryConfig = new AuditSummaryConfig();
        auditSummaryConfig.setEventCode("0");
        auditSummaryConfig.setPath("path");
        List<AuditSummaryConfig> auditSummaryConfigList = Arrays.asList(auditSummaryConfig);

        when(auditDetailsVWRepository.getCustomerAuditDetailsByAuditIds(any(), any(), any(), any())).thenReturn(auditDetailsVWList);
        when(auditSummaryConfigRepository.findAll()).thenReturn(auditSummaryConfigList);


        investigationAuditListCsvService.listing(mockHttpServletResponse, "0", "", "", "all");
    }

}
