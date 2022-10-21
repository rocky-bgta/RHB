package com.rhbgroup.dcpbo.customer.controller;

import com.rhbgroup.dcpbo.customer.aspect.ApiContext;
import com.rhbgroup.dcpbo.customer.client.ConfigErrorInterface;
import com.rhbgroup.dcpbo.customer.contract.SearchCustomer;
import com.rhbgroup.dcpbo.customer.exception.CommonException;
import com.rhbgroup.dcpbo.customer.exception.CommonExceptionAdvice;
import com.rhbgroup.dcpbo.customer.model.DeviceProfile;
import com.rhbgroup.dcpbo.customer.model.UserProfile;
import com.rhbgroup.dcpbo.customer.model.response.BoExceptionResponse;
import com.rhbgroup.dcpbo.customer.service.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.netflix.feign.FeignContext;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.util.Date;

import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest(classes = {DeleteDeviceControllerTest.class, CustControllerTest.Config.class, CustController.class})
@EnableWebMvc
public class DeleteDeviceControllerTest {

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
    DeleteDeviceRequestService deleteDeviceRequestService;

    @MockBean
    DeleteDeviceService deleteDeviceServiceMock;

    @MockBean
    PutProfileStatusService putProfileStatusService;

    @MockBean
    PutUnlockFacilityService putUnlockFacilityServiceMock;

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

    @Test
    public void deleteCustomerDeviceSuccess() throws Exception {
        DeviceProfile testDeviceProfile = new DeviceProfile();
        Date now = new Date();

        testDeviceProfile.setId(1);
        testDeviceProfile.setUserId(123001);
        testDeviceProfile.setDeviceId("testid");
        testDeviceProfile.setDeviceName("Samsung S8");
        testDeviceProfile.setOs("Android 8.0");
        testDeviceProfile.setQuickLoginRefreshToken("eyJhbGciOiJIUzI1NiIsInR5cGUiOiJKV1QifQ");
        testDeviceProfile.setPushNotificationSubscriptionToken("eyJhbGciOiJIUzI1NiIsInR5cGUiOiJKV1QifQ");
        testDeviceProfile.setPushNotificationPlatform("ANDROID");
        testDeviceProfile.setLastLogin(now);
        testDeviceProfile.setCreatedTime(now);
        testDeviceProfile.setIsQuickLoginBioEnabled(false);
        testDeviceProfile.setSubscriberId("1408953838000");
        testDeviceProfile.setSecurePlusSequenceNo(123123);
        testDeviceProfile.setSecurePlusSetup(false);

        UserProfile testUserProfile = new UserProfile();

        testUserProfile.setId(123001);
        testUserProfile.setUsername("TEST USERNAME");
        testUserProfile.setTxnSigningDevice(1);

        when(deleteDeviceServiceMock.deleteDevice(1)).thenReturn(true);

        mockMvc.perform(MockMvcRequestBuilders.delete("/bo/cs/customer/device/" + testDeviceProfile.getId())
                .header("staffId", "4321")
                .header("department", "Admin")
                .header("ip", "156.32.21.73")
                .header("customerId", 123001))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful());
    }
}
