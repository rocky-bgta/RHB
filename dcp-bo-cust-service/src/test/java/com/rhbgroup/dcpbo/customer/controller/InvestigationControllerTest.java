//package com.rhbgroup.dcpbo.customer.controller;
//
//import com.rhbgroup.dcpbo.customer.contract.SearchCustomer;
//import com.rhbgroup.dcpbo.customer.dto.*;
//import com.rhbgroup.dcpbo.customer.exception.CommonExceptionAdvice;
//import com.rhbgroup.dcpbo.customer.exception.SearchCustomerException;
//import com.rhbgroup.dcpbo.customer.service.*;
//import com.rhbgroup.dcpbo.customer.service.impl.InvestigationAuditListServiceImpl;
//import org.hamcrest.Matchers;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.mockito.BDDMockito;
//import org.mockito.Mock;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.cloud.netflix.feign.FeignContext;
//import org.springframework.http.MediaType;
//import org.springframework.test.context.junit4.SpringRunner;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
//import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
//import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
//import org.springframework.web.servlet.config.annotation.EnableWebMvc;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import static org.mockito.Mockito.when;
//
//@RunWith(SpringRunner.class)
//@AutoConfigureMockMvc
//@SpringBootTest(classes = {
//        InvestigationController.class,
//        InvestigationControllerTest.class
//})
//@EnableWebMvc
//public class InvestigationControllerTest {
//
//    @Autowired
//    MockMvc mockMvc;
//    @MockBean
//    InvestigationAuditListServiceImpl investigationAuditListService;
//
//    @Test
//    public void successCustomerSearch() throws Exception {
//        Investigation investigation = new Investigation();
//        List<InvestigationEvent> eventList = new ArrayList<>();
//        InvestigationEvent event = new InvestigationEvent();
//        eventList.add(event);
//        InvestigationPagination investigationPagination = new InvestigationPagination();
//        investigationPagination.setTotalPageNum(1);
//        investigation.setEvent(eventList);
//        investigation.setPagination(investigationPagination);
//
//        BDDMockito.given(this.investigationAuditListService.listing(0, 1, "","","all"))
//                .willReturn(investigation);
//
//        mockMvc.perform(MockMvcRequestBuilders.get("/bo/investigation/audit/list"))
//                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful());
//    }
//}