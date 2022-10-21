package com.rhbgroup.dcpbo.customer.service.impl;

import com.rhbgroup.dcpbo.customer.exception.CommonException;
import com.rhbgroup.dcpbo.customer.model.*;
import com.rhbgroup.dcpbo.customer.repository.DcpAuditCategoryConfigRepository;
import com.rhbgroup.dcpbo.customer.repository.DcpAuditEventConfigRepository;
import com.rhbgroup.dcpbo.customer.service.AuditEventsFunctionService;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
        DcpAuditEventConfigRepository.class,
        DcpAuditCategoryConfigRepository.class,
        AuditEventsFunctionService.class,
        AuditEventsFunctionServiceImpl.class
})
public class AuditEventsFunctionServiceImplTest {

    @MockBean
    DcpAuditEventConfigRepository dcpAuditEventConfigRepository;

    @MockBean
    DcpAuditCategoryConfigRepository dcpAuditCategoryConfigRepository;

    @Autowired
    AuditEventsFunctionService service;

    private static Logger logger = LogManager.getLogger(AuditDetailsServiceImplTests.class);

    @Test
    public void SuccessGetAuditEventslisting() throws Exception {

        String eventCode = "20000";
        AuditEventConfig auditEventConfig = new AuditEventConfig();
        auditEventConfig.setId(6);
        auditEventConfig.setEventCode("20000");
        auditEventConfig.setEventName("Remove Device Access");
        auditEventConfig.setEventCategoryId(1);

        DcpAuditCategoryConfig dcpAuditCategoryConfig = new DcpAuditCategoryConfig();
        dcpAuditCategoryConfig.setCategoryName("Profile Management");

        when(dcpAuditEventConfigRepository.findByEventCode(Mockito.anyString())).thenReturn(auditEventConfig);

        when(dcpAuditCategoryConfigRepository.findOne(Mockito.anyInt())).thenReturn(dcpAuditCategoryConfig);

        service.getAuditEventslisting(eventCode);
    }

    @Test
    public void SuccessGetAuditEventslisting_emptyEventCode() throws Exception {

        String eventCode = "";
        List<DcpAuditCategoryConfig> dcpAuditCategoryConfigList = new ArrayList<>();

        DcpAuditCategoryConfig dcpAuditCategoryConfig = new DcpAuditCategoryConfig();
        dcpAuditCategoryConfig.setId(5);
        dcpAuditCategoryConfig.setCategoryName("Fund Transfer");
        dcpAuditCategoryConfigList.add(dcpAuditCategoryConfig);

        when(dcpAuditCategoryConfigRepository.getAll()).thenReturn(dcpAuditCategoryConfigList);

        List<AuditEventConfig> auditEventConfigList = new ArrayList<>();

        AuditEventConfig auditEventConfig = new AuditEventConfig();
        auditEventConfig.setId(51);
        auditEventConfig.setEventCode("31000");
        auditEventConfig.setEventName("Transfer - Intrabank");
        auditEventConfigList.add(auditEventConfig);

        auditEventConfig = new AuditEventConfig();
        auditEventConfig.setId(52);
        auditEventConfig.setEventCode("31001");
        auditEventConfig.setEventName("Transfer - IBG");
        auditEventConfigList.add(auditEventConfig);

        when(dcpAuditEventConfigRepository.findByCategoryId(Mockito.anyInt())).thenReturn(auditEventConfigList);

        service.getAuditEventslisting(eventCode);
    }

}
