package com.rhbgroup.dcpbo.customer.service;


import com.jayway.jsonpath.JsonPath;
import com.rhbgroup.dcpbo.customer.dcpbo.BoAuditDetails;
import com.rhbgroup.dcpbo.customer.dcpbo.BoAuditModule;
import com.rhbgroup.dcpbo.customer.dcpbo.BoAuditSummaryConfig;
import com.rhbgroup.dcpbo.customer.dto.BoAuditEvent;
import com.rhbgroup.dcpbo.customer.dto.BoAuditEvents;
import com.rhbgroup.dcpbo.customer.dto.BoAuditPagination;
import com.rhbgroup.dcpbo.customer.model.UserProfile;
import com.rhbgroup.dcpbo.customer.repository.*;
import com.rhbgroup.dcpbo.customer.service.impl.BoAuditServiceImpl;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.*;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {BoAuditServiceTests.class, BoAuditService.class, BoAuditServiceImpl.class})
public class BoAuditServiceTests {

    @Autowired
    BoAuditService boAuditService;

    @MockBean
    BoAuditRepository boAuditRepositoryMock;

    @MockBean
    BoAuditDetailsRepository boAuditDetailsRepositoryMock;

    @MockBean
    BoAuditSummaryConfigRepository boAuditSummaryConfigRepositoryMock;

    @MockBean
    BoConfigFunctionModuleRepository boConfigFunctionModuleRepositoryMock;

    @MockBean
    UserProfileRepository userProfileRepositoryMock;

    public static List<BoAuditModule> boAuditModuleList = new ArrayList<>();
    public static BoAuditDetails boAuditDetailsProvideAssist;
    public static BoAuditDetails boAuditDetailsUserMgmt;
    List<BoAuditSummaryConfig> auditSummaryConfigList_id4 = new ArrayList<>();
    List<BoAuditSummaryConfig> auditSummaryConfigList_id5 = new ArrayList<>();

    BoAuditServiceImpl boAuditServiceImpl = new BoAuditServiceImpl();

    private static UserProfile userProfile;

    @Before
    public void setup() {

        BoAuditSummaryConfig boAuditSummaryConfig = new BoAuditSummaryConfig();
        boAuditSummaryConfig.setId(1);
        boAuditSummaryConfig.setEventId(5);
        boAuditSummaryConfig.setFieldName("");
        boAuditSummaryConfig.setPath("$.response.body.activity.username");
        boAuditSummaryConfig.setType("path");
        auditSummaryConfigList_id5.add(boAuditSummaryConfig);
        boAuditSummaryConfig = new BoAuditSummaryConfig();
        boAuditSummaryConfig.setId(2);
        boAuditSummaryConfig.setEventId(5);
        boAuditSummaryConfig.setFieldName("");
        boAuditSummaryConfig.setPath("$.response.body.activity.eventName");
        boAuditSummaryConfig.setType("path");
        auditSummaryConfigList_id5.add(boAuditSummaryConfig);

        boAuditSummaryConfig = new BoAuditSummaryConfig();
        boAuditSummaryConfig.setId(1);
        boAuditSummaryConfig.setEventId(4);
        boAuditSummaryConfig.setFieldName(null);
        boAuditSummaryConfig.setPath("$.response.body.activity.username");
        boAuditSummaryConfig.setType("path");
        auditSummaryConfigList_id4.add(boAuditSummaryConfig);
        boAuditSummaryConfig = new BoAuditSummaryConfig();
        boAuditSummaryConfig.setId(2);
        boAuditSummaryConfig.setEventId(4);
        boAuditSummaryConfig.setFieldName(null);
        boAuditSummaryConfig.setPath("$.response.body.activity.status");
        boAuditSummaryConfig.setType("path");
        auditSummaryConfigList_id4.add(boAuditSummaryConfig);

        BoAuditModule boAuditModule = new BoAuditModule();
        boAuditModule.setId(1);
        boAuditModule.setEventId(5);
        boAuditModule.setActivityName("User Access");
        boAuditModule.setDetailsTableName("TBL_BO_AUDIT_USERMGMT");
        boAuditModule.setFunctionId(2);
        boAuditModule.setUsername("dcpbo6");
        boAuditModule.setTimestamp("2021-01-20 20:31:03.667");

        boAuditModuleList.add(boAuditModule);

        boAuditModule = new BoAuditModule();
        boAuditModule.setId(2);
        boAuditModule.setEventId(4);
        boAuditModule.setActivityName("View customer's audit event details");
        boAuditModule.setDetailsTableName("TBL_BO_AUDIT_PROVIDE_ASSIST");
        boAuditModule.setFunctionId(2);
        boAuditModule.setUsername("dcpbo2");
        boAuditModule.setTimestamp("2021-01-07 11:41:47.380");

        boAuditModuleList.add(boAuditModule);

        boAuditDetailsProvideAssist = new BoAuditDetails();
        boAuditDetailsProvideAssist.setId(1);
        boAuditDetailsProvideAssist.setAuditId(5);
        boAuditDetailsProvideAssist.setDetails("{ \"response\": { \"body\": { \"activity\": { \"username\": \"dcpbo6\", \"status\": \"User Access\" }}}}");

        boAuditDetailsUserMgmt = new BoAuditDetails();
        boAuditDetailsUserMgmt.setId(2);
        boAuditDetailsUserMgmt.setAuditId(12);
        boAuditDetailsUserMgmt.setDetails("{ response: { body: { activity: { username: \"dcpbo6\", eventName: \"User Access\" } } } }");

        userProfile = new UserProfile();
        userProfile.setId(15);
        userProfile.setUsername("sit015");
        userProfile.setName("SIT DCP15");
        userProfile.setCisNo("00000000000021");
    }

    @Test
    public void fetchAuditListByTest() {

        Page<BoAuditModule> page = new PageImpl(boAuditModuleList);

        when(boAuditRepositoryMock.getBoAudit(
                anyList(), anyInt(), anyString(), anyInt(), anyString(), anyInt(),
                isA(PageRequest.class))).thenReturn(page);

        when(boAuditDetailsRepositoryMock.getAuditUsermgmtDetails(anyInt())).thenReturn(boAuditDetailsUserMgmt);
        when(boAuditDetailsRepositoryMock.getAuditProvideAssistanceDetails(anyInt())).thenReturn(boAuditDetailsProvideAssist);
        when(boAuditSummaryConfigRepositoryMock.findByEventId(4)).thenReturn(auditSummaryConfigList_id4);
        when(boAuditSummaryConfigRepositoryMock.findByEventId(5)).thenReturn(auditSummaryConfigList_id5);


        List<Integer> moduleList = Arrays.asList(2, 3);
        BoAuditEvents events = (BoAuditEvents) boAuditService.fetchAuditListBy(moduleList, "dcpbo6", "2020-11-23", 1);

        BoAuditPagination boAuditPagination = events.getPagination();
        List<BoAuditEvent> boAuditEventList = events.getEvent();
        System.out.println("boAuditPagination ---> " + boAuditPagination);
        System.out.println("boAuditEventList ---> " + boAuditEventList);
    }

    @Test
    public void getPayloadFromDetailsTest(){

        String auditDetails = "{ \"response\": { \"body\": { \"activity\": { \"username\": \"dcpbo6\", \"status\": \"User Access\" }}}}";
        Map<String, String> attributes = boAuditServiceImpl.getPayloadFromDetails(auditSummaryConfigList_id5, auditDetails);
        if (auditDetails == null || auditSummaryConfigList_id5 == null || auditSummaryConfigList_id5.isEmpty()) {
            Assert.assertThat(attributes, Matchers.is(new LinkedHashMap<>()));
        }

        auditDetails = "{ \"response\": { \"body\": { \"activity\": { \"details\": [ { \"username\": \"dcpbo6\", \"name\": \"customerId\" }]}}}}";
        boAuditServiceImpl.extractParameterName(auditDetails, attributes, "parameters", "$.response.body.activity.details[*]","parameter");
        String key = (String) attributes.get("Username");
        Assert.assertThat(key, Matchers.is("dcpbo6"));

        auditDetails = "{ \"response\": { \"body\": { \"activity\": { \"details\": [ { \"fieldName\": \"username\", \"value\": \"dcpbo6\" }]}}}}";
        boAuditServiceImpl.extractParameterFieldName(auditDetails, attributes, "parameters", "$.response.body.activity.details[*]","parameter");
        String details = (String) attributes.get("details").replace("\r\n", "");
        Assert.assertThat(details, Matchers.is("username: dcpbo6"));

        auditDetails = "{ \"response\": { \"body\": { \"details\": { \"username\": \"dcpbo6\" }}}}";
        boAuditServiceImpl.extractAdditionalData(attributes, auditDetails, "additionalData", "$.response.body.details");
        details = (String) attributes.get("details");
        Assert.assertThat(details, Matchers.is("username: dcpbo6"));

        Map<String, Object> parameter = new LinkedHashMap<>();
        parameter.put("value", "11401360045831");
        boAuditServiceImpl.getAdditionalDetails(attributes, "accountNo", parameter);
        details = (String) attributes.get("details");
        Assert.assertThat(details, Matchers.is("11401360045831"));

        boAuditServiceImpl.getAdditionalDetails(attributes, "sCardId", parameter);
        details = (String) attributes.get("details");
        Assert.assertThat(details, Matchers.is("11401360045831"));

    }


}
