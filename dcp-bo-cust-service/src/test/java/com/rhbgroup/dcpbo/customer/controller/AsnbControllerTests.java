package com.rhbgroup.dcpbo.customer.controller;

import com.rhbgroup.dcpbo.customer.client.ConfigErrorInterface;
import com.rhbgroup.dcpbo.customer.dto.AsnbTransactions;
import com.rhbgroup.dcpbo.customer.exception.CommonException;
import com.rhbgroup.dcpbo.customer.exception.CommonExceptionAdvice;
import com.rhbgroup.dcpbo.customer.model.response.BoExceptionResponse;
import com.rhbgroup.dcpbo.customer.service.AsnbTransactionsService;
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
@SpringBootTest(classes = {AsnbControllerTests.class, AsnbControllerTests.Config.class, AsnbController.class})
@EnableWebMvc
public class AsnbControllerTests {
    @Autowired
    MockMvc mockMvc;

    @MockBean
    AsnbTransactionsService asnbTransactionsService;

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

    private static Logger logger = LogManager.getLogger(AsnbControllerTests.class);

    @Test
    public void getAsnbTransactionsTest() throws Exception {
        logger.debug("getAsnbTransactions()");
        logger.debug("    asnbTransactionsService: " + asnbTransactionsService);

        int customerId = 1;
        String accountNo = "1123";
        String fundId = "AASSGD";
        String identificationNumber = "751112111011";
        String identificationType = "W";
        String membershipNumber = "000012858169";
        String guardianIdNumber = "";
        boolean isMinor = false;
        String url = "/bo/cs/customer/asnb/" + accountNo + "/transactions?fundId='" + fundId
                + "'&identificationNumber='" + identificationNumber + "'&identificationType='" + identificationType
                + "'&membershipNumber='" + membershipNumber + "'&guardianIdNumber='" + guardianIdNumber
                + "'&isMinor=" + isMinor;

        AsnbTransactions asnbTransactions = new AsnbTransactions();

        when(asnbTransactionsService.getAsnbTransactions(Mockito.anyInt(),
                Mockito.anyString(),
                Mockito.anyString(),
                Mockito.anyString(),
                Mockito.anyString(),
                Mockito.anyBoolean(),
                Mockito.anyString())).thenReturn(asnbTransactions);

        mockMvc.perform(MockMvcRequestBuilders.get(url).header("customerId", customerId))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful());
    }

    @Test
    public void getAsnbDetailsTest_notFound() throws Exception {
        logger.debug("getAsbDetailsTest_notFound()");
        logger.debug("    asbDetailsServiceMock: " + asnbTransactionsService);

        int customerId = 1;
        String accountNo = "1123";
        String fundId = "AASSGD";
        String identificationNumber = "751112111011";
        String identificationType = "W";
        String membershipNumber = "000012858169";
        String guardianIdNumber = "";
        boolean isMinor = false;
        String url = "/bo/cs/customer/asnb/" + accountNo + "/transactions?fundId='" + fundId
                + "'&identificationNumber='" + identificationNumber + "'&identificationType='" + identificationType
                + "'&membershipNumber='" + membershipNumber + "'&guardianIdNumber='" + guardianIdNumber
                + "'&isMinor=" + isMinor;

        when(asnbTransactionsService.getAsnbTransactions(
                Mockito.anyInt(),
                Mockito.anyString(),
                Mockito.anyString(),
                Mockito.anyString(),
                Mockito.anyString(),
                Mockito.anyBoolean(),
                Mockito.anyString())
        ).thenThrow(new CommonException(CommonException.GENERIC_ERROR_CODE));

        mockMvc.perform(MockMvcRequestBuilders.get(url).header("customerId", customerId))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(MockMvcResultMatchers.status().is5xxServerError());
    }
}
