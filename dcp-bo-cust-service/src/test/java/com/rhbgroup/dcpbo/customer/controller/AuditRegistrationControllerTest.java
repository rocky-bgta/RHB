package com.rhbgroup.dcpbo.customer.controller;

import com.rhbgroup.dcpbo.customer.client.ConfigErrorInterface;
import com.rhbgroup.dcpbo.customer.dto.AuditDetails;
import com.rhbgroup.dcpbo.customer.dto.AuditDetailsActivity;
import com.rhbgroup.dcpbo.customer.dto.AuditEvent;
import com.rhbgroup.dcpbo.customer.exception.CommonException;
import com.rhbgroup.dcpbo.customer.exception.CommonExceptionAdvice;
import com.rhbgroup.dcpbo.customer.model.response.BoExceptionResponse;
import com.rhbgroup.dcpbo.customer.service.AuditRegistrationService;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
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

import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest(classes = {AuditRegistrationControllerTest.class, AuditRegistrationController.class})
@EnableWebMvc
public class AuditRegistrationControllerTest {
    @Autowired
    MockMvc mockMvc;

    @MockBean
    AuditRegistrationService auditRegistrationServiceMock;


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
            @Override
            public BoExceptionResponse getConfigError(String errorCode) {
                return new BoExceptionResponse(errorCode, "Not found !!!");
            }
        }
    }

    String token = "1";
    String eventCode = "10000";

    private static Logger logger = LogManager.getLogger(AuditRegistrationControllerTest.class);

    @Test
    public void getAuditRegistrationDetailsTest() throws Exception {
        logger.debug("getAuditRegistrationDetailsTest()");

        AuditDetails auditDetails = new AuditDetails();
        auditDetails.setAuditId(1);
        auditDetails.setEventCode("10000");
        auditDetails.setEventName("Transfer - IBG");
        auditDetails.setTimestamp("2017-03-14T00:00:12+08:00");
        auditDetails.setDeviceId("b21ded15-3bb0-4e50-bfec-dded6079a873");
        auditDetails.setChannel("DMB");
        auditDetails.setStatusCode("10000");
        auditDetails.setStatusDescription("successful");
        auditDetails.addDetails("CardLoanNo", "10102468475712");
        auditDetails.addDetails("Id No", "1234");
        auditDetails.addDetails("Mobile No", "987654321");

        AuditDetailsActivity auditDetailsActivity = new AuditDetailsActivity();
        auditDetailsActivity.setActivity(auditDetails);

        when(auditRegistrationServiceMock.getAuditRegistrationDetails(Mockito.any())).thenReturn(auditDetailsActivity);

        String url = "/bo/cs/customer/registration/audit/" + token + "/details";
        logger.debug("    url: " + url);

        mockMvc.perform(MockMvcRequestBuilders.get(url))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful());
    }

    @Test
    public void getAuditRegistrationDetails_notFound() throws Exception {
        logger.debug("getAuditRegistrationDetails_notFound()");

        when(auditRegistrationServiceMock.getAuditRegistrationDetails(Mockito.any())).thenThrow(new CommonException(CommonException.GENERIC_ERROR_CODE));

        String url = "/bo/cs/customer/registration/audit/" + token + "/details";
        logger.debug("    url: " + url);

        mockMvc.perform(MockMvcRequestBuilders.get(url))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(MockMvcResultMatchers.status().is5xxServerError());
    }

    @Test
    public void getListTest() throws Exception {
        logger.debug("getListTest()");
        logger.debug("    auditRegistrationService: " + auditRegistrationServiceMock);

        AuditEvent auditEvent = new AuditEvent();
        when(auditRegistrationServiceMock.listing(Mockito.anyString(), Mockito.anyInt(), Mockito.anyString(), Mockito.anyString()))
                .thenReturn(auditEvent);

        String url = "/bo/cs/customer/registration/audit/list?cisNo='1'";
        logger.debug("    url: " + url);

        mockMvc.perform(MockMvcRequestBuilders.get(url))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.content().contentType("application/xml;charset=UTF-8"))
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful());
    }

    @Test
    public void getListTest_notFound() throws Exception {
        logger.debug("getListTest()");
        logger.debug("    auditRegistrationService: " + auditRegistrationServiceMock);

        String cisNo = "1";
        when(auditRegistrationServiceMock.listing(
                Mockito.anyString(),
                Mockito.anyInt(),
                Mockito.anyString(),
                Mockito.anyString())
        ).thenThrow(
                new CommonException(CommonException.GENERIC_ERROR_CODE, "Cannot find RegistrationToken for cisNo: " + cisNo)
        );

        String url = "/bo/cs/customer/registration/audit/list?cisNo='" + cisNo + "'";
        logger.debug("    url: " + url);

        mockMvc.perform(MockMvcRequestBuilders.get(url))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.content().contentType("application/xml;charset=UTF-8"))
                .andExpect(MockMvcResultMatchers.status().is5xxServerError());
    }
}
