package com.rhbgroup.dcpbo.customer.controller;

import com.rhbgroup.dcpbo.customer.dcpbo.BoAuditDetails;
import com.rhbgroup.dcpbo.customer.dcpbo.BoAuditModule;
import com.rhbgroup.dcpbo.customer.dcpbo.BoAuditSummaryConfig;
import com.rhbgroup.dcpbo.customer.repository.*;
import com.rhbgroup.dcpbo.customer.service.BoAuditService;
import com.rhbgroup.dcpbo.customer.service.impl.BoAuditServiceImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest(classes = {
        BoAuditControllerTests.class,
        BoAuditController.class,
        BoAuditService.class,
        BoAuditServiceImpl.class})
@EnableWebMvc
public class BoAuditControllerTests {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    BoAuditService auditMainServiceMock;

    @MockBean
    BoAuditDetailsRepository boAuditDetailsRepositoryMock;

    @MockBean
    BoAuditRepository boAuditRepositoryMock;

    @MockBean
    BoAuditSummaryConfigRepository boAuditSummaryConfigRepositoryMock;

    @MockBean
    BoConfigFunctionModuleRepository boConfigFunctionModuleRepositoryMock;

    @MockBean
    UserProfileRepository userProfileRepositoryMock;


    @Test
    public void getAuditDetailsTest() throws Exception {
        String url = "/bo/audit/list?moduleList=1,2,3&username='dcpbo6'&pageNo=2&selecteddate='11-23-2020'";

        List<BoAuditModule> boAuditModuleList = new ArrayList<>();
        BoAuditModule boAuditModule = new BoAuditModule();
        boAuditModule.setActivityName("View User");
        boAuditModule.setDetailsTableName("TBL_BO_AUDIT_PROVIDE_ASSIST");
        boAuditModule.setEventId(1);
        boAuditModule.setFunctionId(1);
        boAuditModule.setId(1);
        boAuditModule.setTimestamp("01-07-2021");
        boAuditModule.setUsername("dcpbo6");

        boAuditModuleList.add(boAuditModule);

        BoAuditDetails boAuditDetails = new BoAuditDetails();
        boAuditDetails.setAuditId(111);
        boAuditDetails.setDetails("{\n" +
                "  \"request\": {\n" +
                "    \"headers\": {\n" +
                "      \"Accept\": \"application/json\",\n" +
                "      \"x-forwarded-proto\": \"http\",\n" +
                "      \"Connection\": \"Keep-Alive\",\n" +
                "      \"Referer\": \"https://172.30.79.45/app/provideAssistance/ActivityDetails/155013/20021/details\",\n" +
                "      \"User-Agent\": \"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.141 Safari/537.36\",\n" +
                "      \"Sec-Fetch-Site\": \"same-origin\",\n" +
                "      \"x-forwarded-port\": \"8080\",\n" +
                "      \"Sec-Fetch-Dest\": \"empty\",\n" +
                "      \"Host\": \"172.30.79.45:8080\",\n" +
                "      \"Accept-Encoding\": \"gzip\",\n" +
                "      \"x-forwarded-for\": \"10.8.1.74, 172.30.79.45\",\n" +
                "      \"userid\": \"8\",\n" +
                "      \"Sec-Fetch-Mode\": \"cors\",\n" +
                "      \"x-forwarded-host\": \"172.30.79.45,172.30.79.45:8080\",\n" +
                "      \"x-forwarded-prefix\": \"/gateway/api/customer\",\n" +
                "      \"Accept-Language\": \"en-US,en;q=0.9\",\n" +
                "      \"Content-Length\": \"0\",\n" +
                "      \"X-Forwarded-Server\": \"172.30.79.45\"\n" +
                "    },\n" +
                "    \"parameters\": [\n" +
                "      {\n" +
                "        \"name\": \"sAuditId\",\n" +
                "        \"value\": \"155013\"\n" +
                "      },\n" +
                "      {\n" +
                "        \"name\": \"eventCode\",\n" +
                "        \"value\": \"20021\"\n" +
                "      }\n" +
                "    ]\n" +
                "  },\n" +
                "  \"response\": {\n" +
                "    \"body\": {\n" +
                "      \"activity\": {\n" +
                "        \"auditId\": 155013,\n" +
                "        \"eventCode\": \"20021\",\n" +
                "        \"eventName\": \"Secure Plus Generation\",\n" +
                "        \"timestamp\": \"2020-11-17T17:02:43+08:00\",\n" +
                "        \"deviceId\": \"3c0c0ba1-04a5-4ba8-8d2f-5bc35c5a6e23\",\n" +
                "        \"channel\": \"DMB\",\n" +
                "        \"statusCode\": \"38103\",\n" +
                "        \"statusDescription\": \"We are unable to proceed with your authorisation request. Please contact our 24-hour customer service line for further assistance.\",\n" +
                "        \"statusSummary\": \"Failed\",\n" +
                "        \"ip\": \"172.30.79.49\",\n" +
                "        \"username\": \"sit2139\",\n" +
                "        \"cisNo\": \"00000600200324\",\n" +
                "        \"details\": [\n" +
                "          {\n" +
                "            \"fieldName\": \"Rule Name\",\n" +
                "            \"value\": \"TOPUP_TOPUP\"\n" +
                "          },\n" +
                "          {\n" +
                "            \"fieldName\": \"Transaction Token\",\n" +
                "            \"value\": \"9b378d03-9d91-4049-810d-3dbe82249e74\"\n" +
                "          },\n" +
                "          {\n" +
                "            \"fieldName\": \"Resend Secure Plus Token\",\n" +
                "            \"value\": null\n" +
                "          },\n" +
                "          {\n" +
                "            \"fieldName\": \"Secure Plus Token\",\n" +
                "            \"value\": null\n" +
                "          },\n" +
                "          {\n" +
                "            \"fieldName\": \"Secure Plus Status\",\n" +
                "            \"value\": null\n" +
                "          },\n" +
                "          {\n" +
                "            \"fieldName\": \"Signed Message\",\n" +
                "            \"value\": null\n" +
                "          },\n" +
                "          {\n" +
                "            \"fieldName\": \"Signing Device Name\",\n" +
                "            \"value\": null\n" +
                "          },\n" +
                "          {\n" +
                "            \"fieldName\": \"Transaction Time\",\n" +
                "            \"value\": null\n" +
                "          },\n" +
                "          {\n" +
                "            \"fieldName\": \"Retry Count\",\n" +
                "            \"value\": null\n" +
                "          },\n" +
                "          {\n" +
                "            \"fieldName\": \"Secure Plus Status\",\n" +
                "            \"value\": null\n" +
                "          }\n" +
                "        ]\n" +
                "      }\n" +
                "    }\n" +
                "  },\n" +
                "  \"additionalData\": {}\n" +
                "}");
        boAuditDetails.setId(1);

        List<BoAuditSummaryConfig> boAuditSummaryConfigs = new ArrayList<>();
        BoAuditSummaryConfig boAuditSummaryConfig = new BoAuditSummaryConfig();
        boAuditSummaryConfig.setEventId(111);
        boAuditSummaryConfig.setFieldName("customerSignOnId");
        boAuditSummaryConfig.setEventId(11);
        boAuditSummaryConfig.setId(33);
        boAuditSummaryConfig.setPath("$.response.body.activity.username");

        boAuditSummaryConfigs.add(boAuditSummaryConfig);

        boAuditSummaryConfig = new BoAuditSummaryConfig();
        boAuditSummaryConfig.setEventId(111);
        boAuditSummaryConfig.setFieldName("event");
        boAuditSummaryConfig.setEventId(11);
        boAuditSummaryConfig.setId(33);
        boAuditSummaryConfig.setPath("$.response.body.activity.eventName");

        boAuditSummaryConfigs.add(boAuditSummaryConfig);

        Page<BoAuditModule> pagedResponse = new PageImpl(boAuditModuleList);
        when(boAuditRepositoryMock.getBoAudit(anyList(), anyInt(), anyString(), anyInt(), anyString(), anyInt(), anyObject())).thenReturn(pagedResponse);
        when(boAuditDetailsRepositoryMock.getAuditProvideAssistanceDetails(anyInt())).thenReturn(boAuditDetails);
        when(boAuditSummaryConfigRepositoryMock.findByEventId(anyInt())).thenReturn(boAuditSummaryConfigs);

        mockMvc.perform(MockMvcRequestBuilders.get(url))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

}
