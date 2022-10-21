package com.rhbgroup.dcpbo.customer.controller;

import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.rhbgroup.dcpbo.customer.client.ConfigErrorInterface;
import com.rhbgroup.dcpbo.customer.contract.SearchCustomer;
import com.rhbgroup.dcpbo.customer.dto.*;
import com.rhbgroup.dcpbo.customer.exception.CommonExceptionAdvice;
import com.rhbgroup.dcpbo.customer.exception.SearchCustomerException;
import com.rhbgroup.dcpbo.customer.model.response.BoExceptionResponse;
import com.rhbgroup.dcpbo.customer.service.*;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
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
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import com.rhbgroup.dcpbo.customer.aspect.ApiContext;
import com.rhbgroup.dcpbo.customer.client.ConfigErrorInterface;
import com.rhbgroup.dcpbo.customer.contract.SearchCustomer;
import com.rhbgroup.dcpbo.customer.exception.CommonException;
import com.rhbgroup.dcpbo.customer.exception.CommonExceptionAdvice;
import com.rhbgroup.dcpbo.customer.exception.SearchCustomerException;
import com.rhbgroup.dcpbo.customer.model.response.BoExceptionResponse;
import com.rhbgroup.dcpbo.customer.service.DeleteDeviceRequestService;
import com.rhbgroup.dcpbo.customer.service.DeleteDeviceService;
import com.rhbgroup.dcpbo.customer.service.GetDevicesService;
import com.rhbgroup.dcpbo.customer.service.GetFavouritesTransferService;
import com.rhbgroup.dcpbo.customer.service.GetTransactionPaymentService;
import com.rhbgroup.dcpbo.customer.service.GetTransactionSearchService;
import com.rhbgroup.dcpbo.customer.service.GetTransactionTopupService;
import com.rhbgroup.dcpbo.customer.service.GetTransactionTransferService;
import com.rhbgroup.dcpbo.customer.service.PutProfileStatusService;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest(classes = { CustControllerTest.class, CustControllerTest.Config.class,
		CustController.class })
@EnableWebMvc
public class CustControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    FeignContext feignContext;

    @MockBean
    CommonExceptionAdvice advice;
 
    @MockBean
    ConfigErrorInterface configErrorInterface;

    @MockBean(name = "searchCustomer")
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
    
	@MockBean
	PutUnlockFacilityService putUnlockFacilityService;
    
    private final String PAGINATION_LAST ="L";
    
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
				if(errorCode.equals("50001"))
				{
					description = DESC_50001;
				}
				return new BoExceptionResponse(errorCode, description);
			}
		}
		
		@Bean
	    ApiContext apiState(){
	        return new ApiContext();
	    }
	}

   @Test
    public void successCustomerSearch() throws Exception {
        List<SearchedCustomer> searchedCustomers = new ArrayList<>();
        SearchedCustomer customerAli = new SearchedCustomer();

        customerAli.setCustid("1");
        customerAli.setUsername("agentAli");
        customerAli.setEmail("agentAli@gmail.com");
        customerAli.setName("ali");
        customerAli.setMobileNo("0129098776");
        customerAli.setCisNo("12345678901234");
        customerAli.setAaoip("123456-12-1234");
        customerAli.setIdType("MK");
        customerAli.setIdNo("A1234567");
        customerAli.setStatus("A");
        customerAli.setIsPremier("TRUE");
        customerAli.setLastLogin("2017-05-14T00:00:12 +08:00");

        searchedCustomers.add(customerAli);
        
        SearchedCustomerResponse response = new SearchedCustomerResponse(); 
    	SearchedCustomerPagination  pagination = new SearchedCustomerPagination();
        response.setCustomer(searchedCustomers);
        pagination.setPageIndicator(PAGINATION_LAST);
        pagination.setRecordCount(searchedCustomers.size());
        pagination.setPageNo(1);
        pagination.setTotalPageNo(1);
        response.setPagination(pagination);

        BDDMockito.given(this.searchCustomer.getCustomerTypeValue("customer", "muhammad.ali",1))
                .willReturn(response);

        this.mockMvc.perform(MockMvcRequestBuilders.get("/bo/cs/customer/search?searchtype=customer&value=muhammad.ali"))
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andExpect(MockMvcResultMatchers.jsonPath("customer[0].custid", Matchers.is("1")))
                .andExpect(MockMvcResultMatchers.jsonPath("customer[0].username", Matchers.is("agentAli")))
                .andExpect(MockMvcResultMatchers.jsonPath("customer[0].email", Matchers.is("agentAli@gmail.com")));
    }

    @Test
    public void retrieveTransactionTopupTest() throws Exception {

        String refId="2";
        TransactionTopup responseBody=new TransactionTopup();
        TransactionTopupTopup topup=new TransactionTopupTopup();


        topup.setTxnTime("2017-03-14 23:00:00");
        topup.setRefId(refId);

        topup.setChannel("MBK");
        topup.setStatusDescription("Transaction Successful");

        responseBody.setTopup(topup);

        when(getTransactionTopupService.retrieveTransactionTopup(refId)).thenReturn(responseBody);
        mockMvc.perform(MockMvcRequestBuilders.get("/bo/cs/transaction/topup/details/" + refId).header("userId","123"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andExpect(MockMvcResultMatchers.jsonPath("topup.txnTime", Matchers.comparesEqualTo("2017-03-14 23:00:00")))
                .andExpect(MockMvcResultMatchers.jsonPath("topup.refId", Matchers.comparesEqualTo(refId)))
                .andExpect(MockMvcResultMatchers.jsonPath("topup.channel", Matchers.comparesEqualTo("MBK")))
                .andExpect(MockMvcResultMatchers.jsonPath("topup.statusDescription", Matchers.comparesEqualTo("Transaction Successful")));
    }

    @Test
    public void retrieveTransactionTransferTest() throws Exception {

        String refId="2";
        TransactionTransfer responseBody=new TransactionTransfer();
        TransactionTransferTransfer transfer1=new TransactionTransferTransfer();


        transfer1.setEventName("Intrabank Transfer");
        transfer1.setTxnTime("2017-03-14 23:00:00");
        transfer1.setRefId(refId);
        transfer1.setChannel("MBK");
        transfer1.setStatusCode("123213214124");

        responseBody.setTransfer(transfer1);

        when(getTransactionTransferService.retrieveTransactionTransfer(refId)).thenReturn(responseBody);
        mockMvc.perform(MockMvcRequestBuilders.get("/bo/cs/transaction/transfer/details/" + refId).header("userId","123"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andExpect(MockMvcResultMatchers.jsonPath("transfer.eventName", Matchers.comparesEqualTo("Intrabank Transfer")))
                .andExpect(MockMvcResultMatchers.jsonPath("transfer.txnTime", Matchers.comparesEqualTo("2017-03-14 23:00:00")))
                .andExpect(MockMvcResultMatchers.jsonPath("transfer.refId", Matchers.comparesEqualTo(refId)))
                .andExpect(MockMvcResultMatchers.jsonPath("transfer.channel", Matchers.comparesEqualTo("MBK")))
                .andExpect(MockMvcResultMatchers.jsonPath("transfer.statusCode", Matchers.comparesEqualTo("123213214124")));

    }

    @Test
    public void retrieveFavouritesTransfer() throws Exception {
        FavouritesTransferPaymentType favouritesTransferPaymentType = new FavouritesTransferPaymentType();
        favouritesTransferPaymentType.setDescription("Loan Payment");
        favouritesTransferPaymentType.setCode("LP");
        FavouritesTransfer responseBody = new FavouritesTransfer();
        responseBody.setMainFunction("IBG");
        responseBody.setTxnType("TRANSFER");
        responseBody.setSubFunction("LP");
        responseBody.setPayeeName("ASTRO SDN BHD");
        responseBody.setPaymentType(favouritesTransferPaymentType);

        when(getFavouritesTransferService.retrieveFavouritesTransfer(123)).thenReturn(responseBody);
        mockMvc.perform(MockMvcRequestBuilders.get("/bo/cs/favourites/123/transfer" ))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andExpect(MockMvcResultMatchers.jsonPath("mainFunction", Matchers.comparesEqualTo("IBG")))
                .andExpect(MockMvcResultMatchers.jsonPath("payeeName", Matchers.comparesEqualTo("ASTRO SDN BHD")))
                .andExpect(MockMvcResultMatchers.jsonPath("subFunction", Matchers.comparesEqualTo("LP")))
                .andExpect(MockMvcResultMatchers.jsonPath("paymentType.code", Matchers.comparesEqualTo("LP")))
                .andExpect(MockMvcResultMatchers.jsonPath("paymentType.description", Matchers.comparesEqualTo("Loan Payment")));

    }
    
    @Test
    public void successRegistrationCustomerSearch() throws Exception {
        List<SearchedRegistrationCustomer> searchedRegistrationCustomers = new ArrayList<>();
        SearchedRegistrationCustomer customerAli = new SearchedRegistrationCustomer();
        Date date = new Date();

        customerAli.setCustid("1");
        customerAli.setUsername("agentAli");
        customerAli.setEmail("agentAli@gmail.com");
        customerAli.setName("ali");
        customerAli.setMobileNo("0129098776");
        customerAli.setCisNo("12345678901234");
        customerAli.setAaoip("123456-12-1234");
        customerAli.setIdType("MK");
        customerAli.setIdNo("A1234567");
        customerAli.setStatus("A");
        customerAli.setIsPremier(Boolean.TRUE);
        customerAli.setLastLogin(date);
        customerAli.setIsRegistered(Boolean.TRUE);
        customerAli.setLastRegistrationAttempt(date);
        customerAli.setIsLocked(Boolean.TRUE);
        customerAli.setAcctNumber("RHB123");
       
        searchedRegistrationCustomers.add(customerAli);
        
        SearchedCustomerRegistrationResponse response = new SearchedCustomerRegistrationResponse(); 
    	SearchedCustomerPagination  pagination = new SearchedCustomerPagination();
        response.setCustomer(searchedRegistrationCustomers);
        pagination.setPageIndicator(PAGINATION_LAST);
        pagination.setRecordCount(searchedRegistrationCustomers.size());
        pagination.setPageNo(1);
        pagination.setTotalPageNo(1);
        response.setPagination(pagination);

        BDDMockito.given(this.searchCustomer.getCustomerValue( "RHB123",1))
                .willReturn(response);

        this.mockMvc.perform(MockMvcRequestBuilders.get("/bo/cs/customer/registration/search?value=RHB123"))
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andExpect(MockMvcResultMatchers.jsonPath("customer[0].custid", Matchers.is("1")))
                .andExpect(MockMvcResultMatchers.jsonPath("customer[0].username", Matchers.is("agentAli")))
                .andExpect(MockMvcResultMatchers.jsonPath("customer[0].email", Matchers.is("agentAli@gmail.com")));
    }
    
	@Test
    public void updateUnlockFacility() throws Exception {
    	UnlockStatus responseBody = new UnlockStatus();
    	responseBody.setIsSuccess("1");

        BDDMockito.given(putUnlockFacilityService.writeUnlockFacility( "1")).willReturn(responseBody);

        this.mockMvc.perform(MockMvcRequestBuilders.put("/bo/cs/customer/1/unlock")).andExpect(MockMvcResultMatchers.status().is2xxSuccessful());
    }
}