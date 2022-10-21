package com.rhbgroup.dcpbo.customer.controller;

import com.rhbgroup.dcpbo.customer.model.AuditEventFunctionCategoryVo;
import com.rhbgroup.dcpbo.customer.model.AuditEventsResponseVo;
import com.rhbgroup.dcpbo.customer.model.EventsDetails;
import com.rhbgroup.dcpbo.customer.service.AuditEventsFunctionService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest(classes = {AuditEventsFunctionControllerTest.class, AuditEventsFunctionController.class})
@EnableWebMvc
public class AuditEventsFunctionControllerTest {
    @Autowired
    MockMvc mockMvc;

    @MockBean
    AuditEventsFunctionService auditEventsFunctionServiceMock;

    @MockBean(name = "auditEventsFunctionService")
    private AuditEventsFunctionService auditEventsFunctionService;

    private static Logger logger = LogManager.getLogger(AuditEventsFunctionControllerTest.class);

    AuditEventsResponseVo response;

    @Before
    public void setup() {
        this.response = givenResult();
    }

    private AuditEventsResponseVo givenResult() {

        response = new AuditEventsResponseVo();

        List<EventsDetails> eventsDetailsList = new ArrayList<>();

        EventsDetails eventsDetails = new EventsDetails();
        eventsDetails.setEventId(6);
        eventsDetails.setEventCode("20000");
        eventsDetails.setEventName("Remove Device Access");
        eventsDetailsList.add(eventsDetails);

        eventsDetails = new EventsDetails();
        eventsDetails.setEventId(7);
        eventsDetails.setEventName("Change Password");
        eventsDetailsList.add(eventsDetails);

        List<AuditEventFunctionCategoryVo> auditEventFunctionCategoryVoList = new ArrayList<>();

        AuditEventFunctionCategoryVo auditEventFunctionCategoryVo = new AuditEventFunctionCategoryVo();
        auditEventFunctionCategoryVo.setCategoryId(1);
        auditEventFunctionCategoryVo.setCategoryName("Profile Management");
        auditEventFunctionCategoryVo.setEvents(eventsDetailsList);
        auditEventFunctionCategoryVoList.add(auditEventFunctionCategoryVo);


        List<EventsDetails> eventsDetailsList1 = new ArrayList<>();
        EventsDetails eventsDetails1 = new EventsDetails();
        eventsDetails1.setEventId(15);
        eventsDetails1.setEventCode("20009");
        eventsDetails1.setEventName("DuitNow Updates");
        eventsDetailsList1.add(eventsDetails1);

        auditEventFunctionCategoryVo = new AuditEventFunctionCategoryVo();
        auditEventFunctionCategoryVo.setCategoryId(4);
        auditEventFunctionCategoryVo.setCategoryName("Duit Now");
        auditEventFunctionCategoryVo.setEvents(eventsDetailsList1);
        auditEventFunctionCategoryVoList.add(auditEventFunctionCategoryVo);

        response.setCategory(auditEventFunctionCategoryVoList);

        return this.response;
    }

    @Test
    public void successGetAuditEventsFunction() throws Exception {
        AuditEventsResponseVo response = givenResult();
        BDDMockito.given(auditEventsFunctionService.getAuditEventslisting(Mockito.anyString()))
                .willReturn(response);

        this.response = givenResult();

        this.mockMvc.perform(MockMvcRequestBuilders.get("/bo/investigation/audit/events"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful());

    }

}
