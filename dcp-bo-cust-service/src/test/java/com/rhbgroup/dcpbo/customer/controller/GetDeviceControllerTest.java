package com.rhbgroup.dcpbo.customer.controller;

import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

import com.rhbgroup.dcpbo.customer.aspect.ApiContext;
import com.rhbgroup.dcpbo.customer.client.ConfigErrorInterface;
import com.rhbgroup.dcpbo.customer.contract.SearchCustomer;
import com.rhbgroup.dcpbo.customer.dto.Device;
import com.rhbgroup.dcpbo.customer.exception.CommonException;
import com.rhbgroup.dcpbo.customer.exception.CommonExceptionAdvice;
import com.rhbgroup.dcpbo.customer.model.response.BoExceptionResponse;
import com.rhbgroup.dcpbo.customer.repository.DeviceProfileRepository;
import com.rhbgroup.dcpbo.customer.service.DeleteDeviceRequestService;
import com.rhbgroup.dcpbo.customer.service.DeleteDeviceService;
import com.rhbgroup.dcpbo.customer.service.GetDevicesService;
import com.rhbgroup.dcpbo.customer.service.GetFavouritesTransferService;
import com.rhbgroup.dcpbo.customer.service.GetTransactionPaymentService;
import com.rhbgroup.dcpbo.customer.service.GetTransactionSearchService;
import com.rhbgroup.dcpbo.customer.service.GetTransactionTopupService;
import com.rhbgroup.dcpbo.customer.service.GetTransactionTransferService;
import com.rhbgroup.dcpbo.customer.service.PutProfileStatusService;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest(classes = { GetDeviceControllerTest.class, GetDeviceControllerTest.Config.class,
		CustController.class })
@EnableWebMvc
public class GetDeviceControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    DeviceProfileRepository deviceProfileRepository;

    @MockBean
    CustController custController;

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
    private DeleteDeviceService deleteDeviceService;
    
    @MockBean
    GetDevicesService getDevicesService;
    
    @MockBean
    PutProfileStatusService putProfileStatusService;

    @MockBean
    DeleteDeviceRequestService deleteDeviceRequestService;
    
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
   		ApiContext apiState() {
   			return new ApiContext();
   		}
   	}
    
    @Test
    public void deleteCustomerDeviceSuccess() throws Exception{
        Device testDeviceProfile = new Device();
        Date now = new Date();

        testDeviceProfile.setId("4321");
        testDeviceProfile.setDeviceId("testid");
        testDeviceProfile.setName("Samsung S8");
        testDeviceProfile.setOs("Android 8.0");
        testDeviceProfile.setPrimaryDevice("true");
        List<Device> deviceList = new ArrayList<>();
        deviceList.add(testDeviceProfile);


        when(getDevicesService.retrieveDevice("4321")).thenReturn(deviceList);

        mockMvc.perform(MockMvcRequestBuilders.get("/bo/cs/customer/devices")
                .header("customerId", "4321"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful());
    }
}
