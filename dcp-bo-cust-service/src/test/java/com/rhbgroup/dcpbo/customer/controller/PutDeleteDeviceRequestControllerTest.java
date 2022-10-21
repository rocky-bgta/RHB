package com.rhbgroup.dcpbo.customer.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.rhbgroup.dcpbo.customer.aspect.ApiContext;
import com.rhbgroup.dcpbo.customer.client.ConfigErrorInterface;
import com.rhbgroup.dcpbo.customer.contract.SearchCustomer;
import com.rhbgroup.dcpbo.customer.exception.CommonException;
import com.rhbgroup.dcpbo.customer.exception.CommonExceptionAdvice;
import com.rhbgroup.dcpbo.customer.model.DelApprovalRequest;
import com.rhbgroup.dcpbo.customer.model.DelApprovalResponse;
import com.rhbgroup.dcpbo.customer.model.response.BoExceptionResponse;
import com.rhbgroup.dcpbo.customer.service.*;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.netflix.feign.FeignContext;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest(classes = {PutDeleteDeviceRequestControllerTest.class, PutDeleteDeviceRequestControllerTest.Config.class, CustController.class})
@EnableWebMvc
public class PutDeleteDeviceRequestControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    FeignContext feignContext;

    @MockBean
    SearchCustomer searchCustomer;

    @MockBean
    private GetTransactionTopupService getTransactionTopupService;

    @MockBean
    private GetTransactionTransferService getTransactionTransferService;

    @MockBean
    private GetTransactionSearchService getTransactionSearchService;

    @MockBean
    private GetTransactionPaymentService getTransactionPaymentService;

    @MockBean
    private GetFavouritesTransferService getFavouritesTransferService;

    @MockBean
    GetDevicesService getDevicesService;

    @MockBean
    PutProfileStatusService putProfileStatusService;

    @MockBean
    PutUnlockFacilityService putUnlockFacilityServiceMock;

    @MockBean
    DeleteDeviceRequestService deleteDeviceRequestService;
    
    @MockBean
    DeleteDeviceService deleteDeviceServiceMock;

    @TestConfiguration
    static class Config {
        @Bean
        public CommonException getCommonException() {
            return new CommonException(CommonException.GENERIC_ERROR_CODE);
        }

        @Bean
        public CommonExceptionAdvice getCommonExceptionAdvice() {
            return new CommonExceptionAdvice();
        }

        @Bean
        public ConfigErrorInterface getConfigErrorInterface() {
            return new ConfigInterfaceImpl();
        }

        class ConfigInterfaceImpl implements ConfigErrorInterface {
            static final String DESC_50001 = "No matching customer";

            @Override
            public BoExceptionResponse getConfigError(String errorCode) {
                String description = "Not found !!!";
                if (errorCode.equals("50001")) {
                    description = DESC_50001;
                }
                return new BoExceptionResponse(errorCode, description);
            }
        }

        @Bean
        ApiContext apiState() {
            return new ApiContext();
        }
    }


    private DelApprovalRequest delApprovalRequest;

    private DelApprovalResponse approvalRequired;

    private DelApprovalResponse approvalNotRequired;

    @Before
    public void setup() {

        delApprovalRequest = new DelApprovalRequest();

        delApprovalRequest.setCustomerId("CustomerId");
        delApprovalRequest.setUsername("Username");
        delApprovalRequest.setDeviceId(123);
        delApprovalRequest.setName("Name");
        delApprovalRequest.setOs("OS");
        delApprovalRequest.setFunctionId(678);

        approvalRequired = new DelApprovalResponse();
        approvalNotRequired = new DelApprovalResponse();

        approvalRequired.setApprovalId(987);
        approvalRequired.setIsWritten("N");

        approvalNotRequired.setApprovalId(0);
        approvalNotRequired.setIsWritten("Y");
    }

    @Test
    public void deleteDeviceRequestApprovalRequired() throws Exception {

        when(deleteDeviceRequestService.deleteDevice(eq(123), eq(456), any(DelApprovalRequest.class))).thenReturn(approvalRequired);

        mockMvc.perform(MockMvcRequestBuilders.put("/bo/cs/customer/device/" + delApprovalRequest.getDeviceId() + "/delete")
                .header("userid", 456)
                .contentType(MediaType.APPLICATION_JSON).content(asJsonString(delApprovalRequest)))
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andExpect(MockMvcResultMatchers.jsonPath("approvalId", Matchers.is(987)))
                .andExpect(MockMvcResultMatchers.jsonPath("isWritten", Matchers.is("N")));

    }

    @Test
    public void deleteDeviceRequestApprovalNotRequired() throws Exception {

        when(deleteDeviceRequestService.deleteDevice(eq(123), eq(456), any(DelApprovalRequest.class))).thenReturn(approvalNotRequired);

        mockMvc.perform(MockMvcRequestBuilders.put("/bo/cs/customer/device/" + delApprovalRequest.getDeviceId() + "/delete")
                .header("userid", 456)
                .contentType(MediaType.APPLICATION_JSON).content(asJsonString(delApprovalRequest)))
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andExpect(MockMvcResultMatchers.jsonPath("approvalId", Matchers.is(0)))
                .andExpect(MockMvcResultMatchers.jsonPath("isWritten", Matchers.is("Y")));
    }

    public static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}