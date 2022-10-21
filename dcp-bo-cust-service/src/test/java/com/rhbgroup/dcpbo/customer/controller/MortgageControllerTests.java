package com.rhbgroup.dcpbo.customer.controller;

import com.rhbgroup.dcp.util.JsonUtil;
import com.rhbgroup.dcpbo.customer.client.ConfigErrorInterface;
import com.rhbgroup.dcpbo.customer.exception.CommonException;
import com.rhbgroup.dcpbo.customer.exception.CommonExceptionAdvice;
import com.rhbgroup.dcpbo.customer.model.MortgageDetails;
import com.rhbgroup.dcpbo.customer.model.response.BoExceptionResponse;
import com.rhbgroup.dcpbo.customer.service.MortgageDetailsService;
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
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest(classes = {MortgageControllerTests.class, MortgageControllerTests.Config.class, MortgageController.class})
@EnableWebMvc
public class MortgageControllerTests {
	@Autowired
	MockMvc mockMvc;
	
	@MockBean
	MortgageDetailsService mortgageDetailsServiceMock;
	
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
	
	private static Logger logger = LogManager.getLogger(MortgageControllerTests.class);

//	@Test
//	public void getMortgageDetailsTest() throws Exception {
//		logger.debug("getMortgageDetailsTest()");
//		logger.debug("    mortgageDetailsServiceMock: " + mortgageDetailsServiceMock);
//		
//		int customerId = 1;
//		String accountNo = "1";
//		logger.debug("    accountNo: " + accountNo);
//		
//        InputStream is = getClass().getClassLoader().getResourceAsStream("GetMortgageDetailsLogic.json");
//        BufferedReader br = new BufferedReader(new InputStreamReader(is));
//        StringBuilder sbld = new StringBuilder();
//        String line = null;
//        while ((line = br.readLine()) != null)
//                sbld.append(line);
//        br.close();
//        is.close();
//
//        String jsonStr = sbld.toString();
//        logger.debug("    jsonStr: " + jsonStr);
//
//        MortgageDetails mortgageDetails = JsonUtil.jsonToObject(jsonStr, MortgageDetails.class);
//        logger.debug("    mortgageDetails: " + mortgageDetails);
//		
//		when(mortgageDetailsServiceMock.getMortgageDetails(customerId, accountNo)).thenReturn(mortgageDetails);
//		
//		mockMvc.perform(MockMvcRequestBuilders.get("/bo/cs/customer/loan/mortgage/" + accountNo + "/details").header("customerId", customerId))
//				.andDo(MockMvcResultHandlers.print())
//				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
//				.andExpect(MockMvcResultMatchers.status().is2xxSuccessful());
//	}
	
	@Test
	public void getMortgageWithFlexiDetailsTest() throws Exception {
		logger.debug("getMortgageFlexiDetailsTest()");
		logger.debug("    mortgageDetailsServiceMock: " + mortgageDetailsServiceMock);
		
		int customerId = 1;
		String accountNo = "1";
		logger.debug("    accountNo: " + accountNo);
		
        InputStream is = getClass().getClassLoader().getResourceAsStream("GetMortgageWithFlexiDetailsLogic.json");
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        StringBuilder sbld = new StringBuilder();
        String line = null;
        while ((line = br.readLine()) != null)
                sbld.append(line);
        br.close();
        is.close();

        String jsonStr = sbld.toString();
        logger.debug("    jsonStr: " + jsonStr);

        MortgageDetails mortgageDetails = JsonUtil.jsonToObject(jsonStr, MortgageDetails.class);
        logger.debug("    mortgageDetails: " + mortgageDetails);
		
		when(mortgageDetailsServiceMock.getMortgageDetails(customerId, accountNo)).thenReturn(mortgageDetails);
		
		mockMvc.perform(MockMvcRequestBuilders.get("/bo/cs/customer/loan/mortgage/" + accountNo + "/details").header("customerId", customerId))
				.andDo(MockMvcResultHandlers.print())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
				.andExpect(MockMvcResultMatchers.status().is2xxSuccessful());
	}
	
	@Test
	public void getMortgageWithoutFlexiDetailsTest() throws Exception {
		logger.debug("getMortgageWithoutFlexiDetailsTest()");
		logger.debug("    mortgageDetailsServiceMock: " + mortgageDetailsServiceMock);
		
		int customerId = 1;
		String accountNo = "1";
		logger.debug("    accountNo: " + accountNo);
		
        InputStream is = getClass().getClassLoader().getResourceAsStream("GetMortgageWithoutFlexiDetailsLogic.json");
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        StringBuilder sbld = new StringBuilder();
        String line = null;
        while ((line = br.readLine()) != null)
                sbld.append(line);
        br.close();
        is.close();

        String jsonStr = sbld.toString();
        logger.debug("    jsonStr: " + jsonStr);

        MortgageDetails mortgageDetails = JsonUtil.jsonToObject(jsonStr, MortgageDetails.class);
        logger.debug("    mortgageFlexiDetails: " + mortgageDetails);
		
		when(mortgageDetailsServiceMock.getMortgageDetails(customerId, accountNo)).thenReturn(mortgageDetails);
		
		mockMvc.perform(MockMvcRequestBuilders.get("/bo/cs/customer/loan/mortgage/" + accountNo + "/details").header("customerId", customerId))
				.andDo(MockMvcResultHandlers.print())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
				.andExpect(MockMvcResultMatchers.status().is2xxSuccessful());
	}

	@Test
	public void getMortgageDetailsTest_notFound() throws Exception {
		logger.debug("getMortgageDetailsTest_notFound()");
		logger.debug("    mortgageDetailsServiceMock: " + mortgageDetailsServiceMock);
		
		int customerId = 1;
		String accountNo = "1";
		
		when(mortgageDetailsServiceMock.getMortgageDetails(customerId, accountNo)).thenThrow(new CommonException(CommonException.GENERIC_ERROR_CODE));

		mockMvc.perform(MockMvcRequestBuilders.get("/bo/cs/customer/loan/mortgage/" + accountNo + "/details").header("customerId", customerId))
				.andDo(MockMvcResultHandlers.print())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
				.andExpect(MockMvcResultMatchers.status().is5xxServerError());
	}

	@Test
	public void getMortgageDetailsAdditionalField() throws Exception {
		logger.debug("getMortgageWithoutFlexiDetailsTest()");
		logger.debug("    mortgageDetailsServiceMock: " + mortgageDetailsServiceMock);

		int customerId = 1;
		String accountNo = "1";
		logger.debug("    accountNo: " + accountNo);

		InputStream is = getClass().getClassLoader().getResourceAsStream("GetMortgageDetailsLogic.json");
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		StringBuilder sbld = new StringBuilder();
		String line = null;
		while ((line = br.readLine()) != null)
			sbld.append(line);
		br.close();
		is.close();

		String jsonStr = sbld.toString();
		logger.debug("    jsonStr: " + jsonStr);

		MortgageDetails mortgageDetails = JsonUtil.jsonToObject(jsonStr, MortgageDetails.class);
		logger.debug("    mortgageFlexiDetails: " + mortgageDetails);

		when(mortgageDetailsServiceMock.getMortgageDetails(customerId, accountNo)).thenReturn(mortgageDetails);

		mockMvc.perform(MockMvcRequestBuilders.get("/bo/cs/customer/loan/mortgage/" + accountNo + "/details").header("customerId", customerId))
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
				.andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
				.andExpect(MockMvcResultMatchers.jsonPath("$.isRedrawalAvailable", is(true)))
				.andExpect(MockMvcResultMatchers.jsonPath("$.redrawalAmount", is("500.0")))
				.andExpect(MockMvcResultMatchers.jsonPath("$.remainingAmount", is("4500.0")))
				.andExpect(MockMvcResultMatchers.jsonPath("$.overdueAmount", is("255.0")))
				.andExpect(MockMvcResultMatchers.jsonPath("$.paymentDueDate", is("20180927")))
				.andExpect(MockMvcResultMatchers.jsonPath("$.monthlyPayment", is("200.0")))
				.andExpect(MockMvcResultMatchers.jsonPath("$.loanAmount", is("5000.0")))
				.andExpect(MockMvcResultMatchers.jsonPath("$.typeOfTerm", is("M")))
				.andExpect(MockMvcResultMatchers.jsonPath("$.originalTenure", is("124")))
				.andExpect(MockMvcResultMatchers.jsonPath("$.remainingTenure", is("100")))
				.andExpect(MockMvcResultMatchers.jsonPath("$.interestRate", is("5.12")))
				.andExpect(MockMvcResultMatchers.jsonPath("$.accountOwnership", is("Individual")))
				.andExpect(MockMvcResultMatchers.jsonPath("$.accountHolder[0].name", is("John")))
				.andExpect(MockMvcResultMatchers.jsonPath("$.accountHolder[1].name", is("Paul")));
	}
	
}
