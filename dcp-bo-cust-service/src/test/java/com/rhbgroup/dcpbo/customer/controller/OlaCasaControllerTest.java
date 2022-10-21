package com.rhbgroup.dcpbo.customer.controller;

import com.rhbgroup.dcpbo.customer.client.ConfigErrorInterface;
import com.rhbgroup.dcpbo.customer.dto.AuditDetails;
import com.rhbgroup.dcpbo.customer.dto.AuditDetailsActivity;
import com.rhbgroup.dcpbo.customer.exception.CommonException;
import com.rhbgroup.dcpbo.customer.model.response.BoExceptionResponse;
import com.rhbgroup.dcpbo.customer.service.OlaCasaService;
import com.rhbgroup.dcpbo.customer.service.impl.OlaCasaServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest(classes = {
        OlaCasaControllerTest.class,
        OlaCasaController.class,
        OlaCasaService.class,
        OlaCasaServiceImpl.class})
@EnableWebMvc
public class OlaCasaControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    OlaCasaService olaCasaServiceMock;

    @TestConfiguration
    static class Config {
        @Bean
        public CommonException getCommonException() {
            return new CommonException(CommonException.GENERIC_ERROR_CODE);
        }

        @Bean
        public ConfigErrorInterface getConfigErrorInterface() {
            return new OlaCasaControllerTest.Config.ConfigInterfaceImpl();
        }

        class ConfigInterfaceImpl implements ConfigErrorInterface {
            @Override
            public BoExceptionResponse getConfigError(String errorCode) {
                return new BoExceptionResponse(errorCode, "Not found !!!");
            }
        }
    }

    @Before
    public void setup() {

        AuditDetailsActivity auditDetailsActivity = new AuditDetailsActivity();
        AuditDetails auditDetails = new AuditDetails();
        auditDetails.setAuditId(0);
        auditDetails.setEventCode("10009");
        auditDetails.setEventName("Some event");
        auditDetails.setTimestamp("2017-03-14T00:00:12+08:00");
        auditDetails.setDeviceId("b21ded15-3bb0-4e50-bfec-dded6079a873");
        auditDetails.setChannel("DMB");
        auditDetails.setStatusCode("10000");
        auditDetails.setStatusDescription("successful");
        auditDetails.setRefId("A1234");
        auditDetails.setStatusSummary("summary");
        auditDetails.setIp("192.175.123.12");
        auditDetails.setUsername("John");
        auditDetails.setCisNo("123456");

        auditDetails.addDetails("Full Name", "John Doe");
        auditDetails.addDetails("Mobile Number", "0123456789");
        auditDetails.addDetails("Email Address", "abc@gmail.com");
        auditDetailsActivity.setActivity(auditDetails);

        when(olaCasaServiceMock.getAuditDetails(any())).thenReturn(auditDetailsActivity);

    }

    @Test
    public void getAuditDetailsTestSuccess() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.get("/bo/cs/customer/olacasa/audit/5kdfj3432JWEFWFrrr3333/details"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful());
    }

}
