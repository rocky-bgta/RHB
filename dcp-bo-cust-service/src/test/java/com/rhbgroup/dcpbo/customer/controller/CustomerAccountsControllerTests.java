package com.rhbgroup.dcpbo.customer.controller;

import com.rhbgroup.dcp.profiles.model.DcpCurrentsAccount;
import com.rhbgroup.dcp.profiles.model.DcpGetAccountsProfileResponse;
import com.rhbgroup.dcp.profiles.model.DcpSavingsAccount;
import com.rhbgroup.dcpbo.customer.aspect.ApiContext;
import com.rhbgroup.dcpbo.customer.client.ConfigErrorInterface;
import com.rhbgroup.dcpbo.customer.dto.CustomerAccounts;
import com.rhbgroup.dcpbo.customer.exception.CommonException;
import com.rhbgroup.dcpbo.customer.exception.CommonExceptionAdvice;
import com.rhbgroup.dcpbo.customer.model.response.BoExceptionResponse;
import com.rhbgroup.dcpbo.customer.service.ConfigService;
import com.rhbgroup.dcpbo.customer.service.CustomerAccountsService;
import com.rhbgroup.dcpbo.customer.service.CustomerProfileService;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.util.LinkedList;
import java.util.List;

import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest(classes = {CustomerAccountsControllerTests.class, CustomerAccountsControllerTests.Config.class, CustomerController.class})
@EnableWebMvc
public class CustomerAccountsControllerTests {
    @Autowired
    MockMvc mockMvc;

    @MockBean
    CustomerAccountsService customerAccountsServiceMock;

    @MockBean
    ConfigService configServiceMock;

    @MockBean
    CustomerProfileService customerProfileServiceMock;

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

        @Bean
        @Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
        ApiContext apiState() {
            return new ApiContext();
        }
    }

    private static Logger logger = LogManager.getLogger(CustomerAccountsControllerTests.class);

    @Test
    public void getCustomerAccountsDetailsTest() throws Exception {
        logger.debug("getCustomerAccountsDetailsTest()");
        logger.debug("    customerAccountsServiceMock: " + customerAccountsServiceMock);

        int customerId = 1;

        DcpSavingsAccount savingsAccount = new DcpSavingsAccount();
        savingsAccount.setAccountNo("12345678901234");
        savingsAccount.setProductCode("1234");
        savingsAccount.setNickname("My Little Bonobos");
        savingsAccount.setProductName("RHB Personal Saving");
        savingsAccount.setConnectorCode("IND");
        savingsAccount.setIsDefaultAccount(true);
        savingsAccount.setIsHidden(false);
        savingsAccount.setPermission("ACCOUNT_DASHBOARD");

        List<DcpSavingsAccount> savings = new LinkedList<DcpSavingsAccount>();
        savings.add(savingsAccount);

        DcpCurrentsAccount currentAccount = new DcpCurrentsAccount();
        currentAccount.setAccountNo("23456789012345");
        currentAccount.setProductCode("2345");
        currentAccount.setNickname("My Little Pangolin");
        currentAccount.setProductName("RHB Personal Current");
        currentAccount.setConnectorCode("JON");
        currentAccount.setIsDefaultAccount(false);
        currentAccount.setIsHidden(true);
        currentAccount.setPermission("TRANSFER_SOURCE");

        List<DcpCurrentsAccount> currents = new LinkedList<DcpCurrentsAccount>();
        currents.add(currentAccount);

        DcpGetAccountsProfileResponse accounts = new DcpGetAccountsProfileResponse();
        accounts.setSavings(savings);
        accounts.setCurrents(currents);

        CustomerAccounts customerAccounts = new CustomerAccounts();
        customerAccounts.setAccounts(accounts);

        when(customerAccountsServiceMock.getCustomerAccounts(customerId)).thenReturn(customerAccounts);

        mockMvc.perform(MockMvcRequestBuilders.get("/bo/cs/customer/accounts").header("customerId", customerId))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful());
    }

    @Test
    public void getCustomerAccountsDetailsTest_notFound() throws Exception {
        logger.debug("getCustomerAccountsDetailsTest_notFound()");
        logger.debug("    customerAccountsServiceMock: " + customerAccountsServiceMock);

        int customerId = 1;

        when(customerAccountsServiceMock.getCustomerAccounts(customerId)).thenThrow(new CommonException(CommonException.GENERIC_ERROR_CODE));

        mockMvc.perform(MockMvcRequestBuilders.get("/bo/cs/customer/accounts").header("customerId", customerId))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(MockMvcResultMatchers.status().is5xxServerError());
    }
}
