package com.rhbgroup.dcpbo.system.epullenrollment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rhbgroup.dcp.estatement.bizlogic.EPullAutoEnrollmentLogic;
import com.rhbgroup.dcp.estatement.model.EPullAutoEnrollmentAccountRequest;
import com.rhbgroup.dcp.estatement.model.EPullAutoEnrollmentCardRequest;
import com.rhbgroup.dcp.estatement.model.EPullAutoEnrollmentRequest;
import com.rhbgroup.dcp.model.Capsule;
import com.rhbgroup.dcpbo.common.exception.CommonException;
import com.rhbgroup.dcpbo.common.exception.ConfigErrorInterface;
import com.rhbgroup.dcpbo.common.exception.ErrorResponse;
import com.rhbgroup.dcpbo.system.epullenrollment.service.EpullEnrollmentService;
import com.rhbgroup.dcpbo.system.epullenrollment.service.EpullEnrollmentServiceImpl;
import com.rhbgroup.dcpbo.system.exception.EpullEnrollmentControllerExceptionAdvice;
import com.rhbgroup.dcpbo.system.exception.EpullEnrollmentMissingException;
import org.hamcrest.Matchers;
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

import java.util.ArrayList;
import java.util.Arrays;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest(classes = {
        EpullEnrollmentControllerTest.class,
        EpullEnrollmentController.class,
        EpullEnrollmentService.class,
        EpullEnrollmentServiceImpl.class,
        EpullEnrollmentControllerExceptionAdvice.class,
        EpullEnrollmentMissingException.class})
@EnableWebMvc
public class EpullEnrollmentControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    EpullEnrollmentService epullEnrollmentService;

    @MockBean
    EPullAutoEnrollmentLogic ePullAutoEnrollmentLogic;

    private EPullAutoEnrollmentRequest ePullAutoEnrollmentRequest;

    @TestConfiguration
    static class Config {
        @Bean
        public CommonException getCommonException() {
            return new CommonException(CommonException.GENERIC_ERROR_CODE, "NA");
        }

        @Bean
        public ConfigErrorInterface getConfigErrorInterface() {
            return new EpullEnrollmentControllerTest.Config.ConfigInterfaceImpl();
        }

        class ConfigInterfaceImpl implements ConfigErrorInterface {
            @Override
            public ErrorResponse getConfigError(String errorCode) {
                return new ErrorResponse(errorCode, "Invalid date format");
            }
        }
    }

    @Before
    public void setup() {
        ePullAutoEnrollmentRequest = new EPullAutoEnrollmentRequest();
        ePullAutoEnrollmentRequest.setSavings(Arrays.asList(
                new EPullAutoEnrollmentAccountRequest("00000111")
                ));
        ePullAutoEnrollmentRequest.setMca(Arrays.asList(
                new EPullAutoEnrollmentAccountRequest("00000222")
        ));
        ePullAutoEnrollmentRequest.setCreditCards(Arrays.asList(
                new EPullAutoEnrollmentCardRequest("00000333")
        ));
    }

    @Test
    public void executeEpullEnrollmentSuccessTest() throws Exception {
        Capsule response = new Capsule();
        response.setOperationSuccess(Boolean.TRUE);
        response.updateCurrentMessage("{}");

        when(ePullAutoEnrollmentLogic.executeBusinessLogic(any(Capsule.class))).thenReturn(response);

        mockMvc.perform(MockMvcRequestBuilders.post("/bo/system/epull/enrollment").header("userProfileId","5")
                .contentType(MediaType.APPLICATION_JSON).content(new ObjectMapper().writeValueAsString(ePullAutoEnrollmentRequest)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful());
    }

    @Test
    public void executeEpullEnrollmentMissingUserIdFailTest() throws Exception {
        Capsule response = new Capsule();
        response.setOperationSuccess(Boolean.TRUE);
        response.updateCurrentMessage("{}");

        when(ePullAutoEnrollmentLogic.executeBusinessLogic(any(Capsule.class))).thenReturn(response);

        mockMvc.perform(MockMvcRequestBuilders.post("/bo/system/epull/enrollment").header("userProfileId","")
                .contentType(MediaType.APPLICATION_JSON).content(new ObjectMapper().writeValueAsString(ePullAutoEnrollmentRequest)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errorCode", Matchers.is("41002")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.errorDesc", Matchers.is("Missing User Id")));
    }

    @Test
    public void executeEpullEnrollmentNullPayloadFailTest() throws Exception {
        Capsule response = new Capsule();
        response.setOperationSuccess(Boolean.TRUE);
        response.updateCurrentMessage("{}");

        when(ePullAutoEnrollmentLogic.executeBusinessLogic(any(Capsule.class))).thenReturn(response);

        EPullAutoEnrollmentRequest payload = new EPullAutoEnrollmentRequest();

        mockMvc.perform(MockMvcRequestBuilders.post("/bo/system/epull/enrollment").header("userProfileId","1")
                .contentType(MediaType.APPLICATION_JSON).content(new ObjectMapper().writeValueAsString(payload)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errorCode", Matchers.is("41003")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.errorDesc", Matchers.is("Request payload is empty")));
    }

    @Test
    public void executeEpullEnrollmentEmptyPayloadFailTest() throws Exception {
        Capsule response = new Capsule();
        response.setOperationSuccess(Boolean.TRUE);
        response.updateCurrentMessage("{}");

        when(ePullAutoEnrollmentLogic.executeBusinessLogic(any(Capsule.class))).thenReturn(response);

        EPullAutoEnrollmentRequest payload = new EPullAutoEnrollmentRequest(
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>());

        mockMvc.perform(MockMvcRequestBuilders.post("/bo/system/epull/enrollment").header("userProfileId","1")
                .contentType(MediaType.APPLICATION_JSON).content(new ObjectMapper().writeValueAsString(payload)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errorCode", Matchers.is("41003")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.errorDesc", Matchers.is("Request payload is empty")));
    }

    @Test
    public void executeEpullEnrollmentFailExecuteSDKTest() throws Exception {
        Capsule response = new Capsule();
        response.setOperationSuccess(Boolean.FALSE);
        response.updateCurrentMessage("{}");

        when(ePullAutoEnrollmentLogic.executeBusinessLogic(any(Capsule.class))).thenReturn(response);

        mockMvc.perform(MockMvcRequestBuilders.post("/bo/system/epull/enrollment").header("userProfileId","1")
                .contentType(MediaType.APPLICATION_JSON).content(new ObjectMapper().writeValueAsString(ePullAutoEnrollmentRequest)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errorCode", Matchers.is("40010")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.errorDesc", Matchers.is("Error executing epull auto enrollment logic.")));
    }

    @Test
    public void executeEpullEnrollmentInternalErrorTest() throws Exception {

        when(ePullAutoEnrollmentLogic.executeBusinessLogic(any(Capsule.class))).thenThrow(Exception.class);

        mockMvc.perform(MockMvcRequestBuilders.post("/bo/system/epull/enrollment").header("userProfileId","1")
                .contentType(MediaType.APPLICATION_JSON).content(new ObjectMapper().writeValueAsString(ePullAutoEnrollmentRequest)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().is5xxServerError());
    }
}