package com.rhbgroup.dcpbo.customer.controller;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hamcrest.Matchers;
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
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.rhbgroup.dcpbo.customer.aspect.ApiContext;
import com.rhbgroup.dcpbo.customer.client.ConfigErrorInterface;
import com.rhbgroup.dcpbo.customer.exception.CommonException;
import com.rhbgroup.dcpbo.customer.exception.CommonExceptionAdvice;
import com.rhbgroup.dcpbo.customer.model.response.BoExceptionResponse;
import com.rhbgroup.dcpbo.customer.service.GetTransactionTransferService;
import com.rhbgroup.dcpbo.customer.service.ProfileFavouriteService;
import com.rhbgroup.dcpbo.customer.vo.ProfileFavouriteListVo;
import com.rhbgroup.dcpbo.customer.vo.ProfileFavouriteVo;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest(classes = { CustomerFavouriteControllerTest.class, CustomerFavouriteControllerTest.Config.class,
		CustomerFavouriteController.class })
@EnableWebMvc
public class CustomerFavouriteControllerTest {

	private static Logger logger = LogManager.getLogger(CustomerFavouriteControllerTest.class);

	@Autowired
	MockMvc mockMvc;

	@MockBean
	FeignContext feignContext;

	@MockBean
	ConfigErrorInterface configErrorInterface;

	@MockBean
	CommonExceptionAdvice commonExceptionAdvice;

	@MockBean
	private ProfileFavouriteService profileFavouriteService;

	ProfileFavouriteListVo mockProfileFavouriteListVo;
	List<ProfileFavouriteVo> mainProfileFavouriteVoList;

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
	    ApiContext apiState(){
	        return new ApiContext();
	    }
	}
	
	@Before
	public void setup() {

		this.mockProfileFavouriteListVo = givenResult();
	}

	private ProfileFavouriteListVo givenResult() {
		mockProfileFavouriteListVo = new ProfileFavouriteListVo();
		ProfileFavouriteVo mainProfileFavouriteVo = new ProfileFavouriteVo();
		mainProfileFavouriteVoList = new ArrayList<ProfileFavouriteVo>();

		mainProfileFavouriteVo.setId("1");
		mainProfileFavouriteVo.setTxnType("TRANSFER");
		mainProfileFavouriteVo.setMainFunction("OWN");
		mainProfileFavouriteVo.setPayeeName("RHB Bank");
		mainProfileFavouriteVo.setToAccountNo("12345678901234");
		mainProfileFavouriteVo.setNickname("My House Fund");
		mainProfileFavouriteVo.setRef1("moms allowance");
		mainProfileFavouriteVo.setIsQuickLink(true);
		mainProfileFavouriteVo.setIsQuickPay(true);
		mainProfileFavouriteVo.setMainLabel("My House Fund");
		mainProfileFavouriteVoList.add(mainProfileFavouriteVo);

		mainProfileFavouriteVo = new ProfileFavouriteVo();
		mainProfileFavouriteVo.setId("2");
		mainProfileFavouriteVo.setTxnType("PAYMENT");
		mainProfileFavouriteVo.setMainFunction("OTHER_BILLER");
		mainProfileFavouriteVo.setPayeeName("TNB Sdn Bhd");
		mainProfileFavouriteVo.setNickname("Dad's TNB Bill");
		mainProfileFavouriteVo.setRef1("89073907");
		mainProfileFavouriteVo.setIsQuickLink(true);
		mainProfileFavouriteVo.setIsQuickPay(false);
		mainProfileFavouriteVo.setMainLabel("TNB Sdn Bhd");
		mainProfileFavouriteVoList.add(mainProfileFavouriteVo);

		mockProfileFavouriteListVo.setFavourites(mainProfileFavouriteVoList);

		return this.mockProfileFavouriteListVo;
	}

	@Test
	public void successGetCustomerTxnLimitTest() throws Exception {
		BDDMockito.given(this.profileFavouriteService.getProfileFavourites("1"))
				.willReturn(this.mockProfileFavouriteListVo);

		XmlMapper xmlMapper = new XmlMapper();
		String aaa = xmlMapper.writeValueAsString(this.mockProfileFavouriteListVo);

		logger.debug("|" + aaa + "|");

		this.mockProfileFavouriteListVo = givenResult();

		this.mockMvc
				.perform(MockMvcRequestBuilders.get("/bo/cs/customer/favourites/list").header("customerId",
						"1"))
				.andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
				.andExpect(MockMvcResultMatchers.jsonPath("favourites[0].txnType", Matchers.is("TRANSFER")))
				.andExpect(MockMvcResultMatchers.jsonPath("favourites[0].mainFunction", Matchers.is("OWN")));
	}
}
