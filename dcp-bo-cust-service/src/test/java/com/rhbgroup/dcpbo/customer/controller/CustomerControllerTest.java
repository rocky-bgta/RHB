package com.rhbgroup.dcpbo.customer.controller;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.rhbgroup.dcpbo.customer.exception.CommonExceptionAdvice;


import com.rhbgroup.dcpbo.customer.model.response.BoExceptionResponse;
import com.rhbgroup.dcpbo.customer.service.GetTransactionTransferService;
import com.rhbgroup.dcpbo.customer.service.CustomerProfileService;


import org.junit.Before;
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

import com.rhbgroup.dcpbo.customer.dto.CustomerProfile;
import com.rhbgroup.dcpbo.customer.exception.CommonException;
import com.rhbgroup.dcpbo.customer.service.ConfigService;
import com.rhbgroup.dcpbo.customer.service.CustomerAccountsService;
import com.rhbgroup.dcpbo.customer.vo.CustomerProfileVo;
import com.rhbgroup.dcpbo.customer.vo.CustomerTrxLimitVo;
import com.rhbgroup.dcpbo.customer.vo.MainFunctionLimitsVo;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { CustomerControllerTest.class, CustomerControllerTest.Config.class,
		CustomerController.class })
@AutoConfigureMockMvc
@EnableWebMvc
public class CustomerControllerTest {

	@Autowired
	MockMvc mockMvc;

	@MockBean
	FeignContext feignContext;

	@MockBean
	ConfigErrorInterface configErrorInterface;

	@MockBean
	CommonExceptionAdvice commonExceptionAdvice;

	@MockBean
	private ConfigService configService;
	
	@MockBean
    private CustomerAccountsService customerAccountsService;
	
	@MockBean
    private CustomerProfileService userProfileServiceMock;

	CustomerProfileVo customerProfileVoMock;
	
	List<CustomerTrxLimitVo> mockCustomerTrxLimitVoList;
	List<MainFunctionLimitsVo> mainFunctionLimitsVoList;
	
	List<CustomerProfile> CustomerProfilelist;
	
	@MockBean
	GetTransactionTransferService getTransactionTransferService;

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

	@Before
	public void setup() {

		this.mockCustomerTrxLimitVoList = givenResult();
		this.customerProfileVoMock =getCustomerProfileData();
	}

	private List<CustomerTrxLimitVo> givenResult() {
		mockCustomerTrxLimitVoList = new ArrayList<CustomerTrxLimitVo>();
		CustomerTrxLimitVo mockCustomerTrxLimitVo = new CustomerTrxLimitVo();
		mockCustomerTrxLimitVo.setTxnType("TRANSFER");
		mockCustomerTrxLimitVo.setMainFunctionLimits(getMainFuncListResult1());
		mockCustomerTrxLimitVoList.add(mockCustomerTrxLimitVo);

		mockCustomerTrxLimitVo = new CustomerTrxLimitVo();
		mockCustomerTrxLimitVo.setTxnType("PAYMENT");
		mockCustomerTrxLimitVo.setMainFunctionLimits(getMainFuncListResult2());
		mockCustomerTrxLimitVoList.add(mockCustomerTrxLimitVo);

		return this.mockCustomerTrxLimitVoList;
	}

	private List<MainFunctionLimitsVo> getMainFuncListResult1() {
		this.mainFunctionLimitsVoList = new ArrayList<MainFunctionLimitsVo>();

		MainFunctionLimitsVo mainFunctionLimitsVo = new MainFunctionLimitsVo();
		mainFunctionLimitsVo.setMainFunction("IBG");
		mainFunctionLimitsVo.setAmount(new BigDecimal(5000.00));
		mainFunctionLimitsVoList.add(mainFunctionLimitsVo);

		mainFunctionLimitsVo = new MainFunctionLimitsVo();
		mainFunctionLimitsVo.setMainFunction("INSTANT");
		mainFunctionLimitsVo.setAmount(new BigDecimal(5000.00));
		mainFunctionLimitsVoList.add(mainFunctionLimitsVo);

		return this.mainFunctionLimitsVoList;
	}

	private List<MainFunctionLimitsVo> getMainFuncListResult2() {
		this.mainFunctionLimitsVoList = new ArrayList<MainFunctionLimitsVo>();

		MainFunctionLimitsVo mainFunctionLimitsVo = new MainFunctionLimitsVo();
		mainFunctionLimitsVo.setMainFunction("OTHER_BILLER");
		mainFunctionLimitsVo.setAmount(new BigDecimal(15000.00));
		mainFunctionLimitsVoList.add(mainFunctionLimitsVo);

		mainFunctionLimitsVo = new MainFunctionLimitsVo();
		mainFunctionLimitsVo.setMainFunction("JOMPAY_BILLER");
		mainFunctionLimitsVo.setAmount(new BigDecimal(10000.00));
		mainFunctionLimitsVoList.add(mainFunctionLimitsVo);

		return this.mainFunctionLimitsVoList;
	}
	  private  CustomerProfileVo getCustomerProfileData(){
		  
		  customerProfileVoMock=new CustomerProfileVo();
		  List<CustomerProfile> list = new ArrayList<>();
		  CustomerProfile responseData = new CustomerProfile();
		  responseData.setTitleEn("Success!");
		  responseData.setDescriptionEn("Success!");
		  responseData.setActionURL("/bo/cs/customer/reset/3/C");
		  responseData.setButton("Unlock");
		  responseData.setCardType("CREDIT_CARD");
		  responseData.setCardNo("4363452300007898");
		  
		  list.add(responseData);
		  
		  responseData = new CustomerProfile();
		  responseData.setTitleEn("Success!");
		  responseData.setDescriptionEn("Success!");
		  responseData.setActionURL("/bo/cs/customer/reset/3/L");
		  responseData.setButton("Unlock");
		  responseData.setCardType("CREDIT_CARD");
		  responseData.setCardNo("4363452300007452");
		  
		  list.add(responseData);
		  
		  responseData = new CustomerProfile();
		  responseData.setTitleEn("Success!");
		  responseData.setDescriptionEn("Success!");
		  responseData.setActionURL("/bo/cs/customer/reset/2919/TPIN");
		  responseData.setButton("Unlock");
		  responseData.setCardType("CREDIT_CARD");
		  responseData.setCardNo("4363452300003241");
		  
		  list.add(responseData);
		  customerProfileVoMock.setActions(list);
		  return customerProfileVoMock;
	  }
	   @Test
	   public void getCustomerProfileTestSuccess() throws Exception{
		  
		   String url = "/bo/cs/customer/status";
			
		   when(userProfileServiceMock.getCustomerProfile(3)).thenReturn(customerProfileVoMock);
		   
		   mockMvc.perform(MockMvcRequestBuilders.get(url).header("customerId", 3)
	                .contentType(MediaType.APPLICATION_JSON))
					.andDo(MockMvcResultHandlers.print())
	                .andExpect(MockMvcResultMatchers.status().isOk());

	  }
	
	@Test
	public void successGetCustomerTxnLimit() throws Exception {
		System.out.println("==========================" + this.mockCustomerTrxLimitVoList.size());
		BDDMockito.given(this.configService.getCustomerTrxLimits("15")).willReturn(this.mockCustomerTrxLimitVoList);
        assertNotNull(mockCustomerTrxLimitVoList);
		this.mockMvc.perform(MockMvcRequestBuilders.get("/bo/cs/customer/limits?customerID=15").header("sessiontoken",
				"thisisasessiontokensampleblablabla"));
		// .andExpect(MockMvcResultMatchers.jsonPath("$..txnType",
		// Matchers.is("TRANSFER")));
	}

	/*@Test
	public void notFoundCustomerSearch() throws Exception {
		BDDMockito.given(this.configService.getCustomerTrxLimits("15")).willThrow(new CommonException(2));

		this.mockMvc
				.perform(MockMvcRequestBuilders.get("/bo/cs/customer/limits?customerID=15").header("sessiontoken",
						"thisisasessiontokensampleblablabla"))
				.andExpect(MockMvcResultMatchers.jsonPath("errorCode", Matchers.is(2)))
				.andExpect(MockMvcResultMatchers.jsonPath("errorDesc", Matchers.is("User Not existed!")));
		;
	}*/

}
