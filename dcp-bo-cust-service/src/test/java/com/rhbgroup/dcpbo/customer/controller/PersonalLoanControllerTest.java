package com.rhbgroup.dcpbo.customer.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rhbgroup.dcpbo.customer.contract.BoData;
import com.rhbgroup.dcpbo.customer.model.PersonalLoan;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import com.rhbgroup.dcpbo.customer.service.PersonalLoanService;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest(classes = { PersonalLoanControllerTest.class, PersonalLoanController.class })
@EnableWebMvc
public class PersonalLoanControllerTest {

	@Autowired
	MockMvc mockMvc;

	@MockBean
	PersonalLoanService mockPersonalLoanService;

	private static Logger logger = LogManager.getLogger(PersonalLoanControllerTest.class);

	@Test
	public void getPersonalLoanDetailsTest() throws Exception {
		logger.debug("mockPersonalLoanService: " + mockPersonalLoanService);

		int customerId = 1;
		String accountNo = "1";

		InputStream is = getClass().getClassLoader().getResourceAsStream("GetPersonalFinanceAccountDetailsLogic.json");
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		StringBuilder sbld = new StringBuilder();
		String line = null;
		while ((line = br.readLine()) != null)
			sbld.append(line);
		br.close();
		is.close();

		String jsonStr = sbld.toString();
		logger.debug("jsonStr: " + jsonStr);

        PersonalLoan testResult = new PersonalLoan();
        testResult.setRemainingAmount("4500.00");
        testResult.setOverdueAmount("255.00");
        testResult.setPaymentDueDate("2018-02-02T00:00:00.000+08:00");
        testResult.setMonthlyPayment("200.00");
        testResult.setLoanAmount("5000.00");
        testResult.setTypeOfTerm("M");
        testResult.setOriginalTenure("124");
        testResult.setRemainingTenure("100");
        testResult.setInterestRate("5.12");

		when(mockPersonalLoanService.getPersonalLoanDetails(customerId, accountNo)).thenReturn(testResult);

		mockMvc.perform(MockMvcRequestBuilders.get("/bo/cs/customer/loan/personal/" + accountNo + "/details")
				.header("customerId", customerId))
				.andDo(MockMvcResultHandlers.print())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
				.andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
				.andExpect(MockMvcResultMatchers.jsonPath("remainingAmount", Matchers.is("4500.00")))
				.andExpect(MockMvcResultMatchers.jsonPath("overdueAmount", Matchers.is("255.00")))
                .andExpect(MockMvcResultMatchers.jsonPath("paymentDueDate", Matchers.is("2018-02-02T00:00:00.000+08:00")))
                .andExpect(MockMvcResultMatchers.jsonPath("monthlyPayment", Matchers.is("200.00")))
                .andExpect(MockMvcResultMatchers.jsonPath("loanAmount", Matchers.is("5000.00")))
                .andExpect(MockMvcResultMatchers.jsonPath("typeOfTerm", Matchers.is("M")))
                .andExpect(MockMvcResultMatchers.jsonPath("originalTenure", Matchers.is("124")))
                .andExpect(MockMvcResultMatchers.jsonPath("remainingTenure", Matchers.is("100")))
                .andExpect(MockMvcResultMatchers.jsonPath("interestRate", Matchers.is("5.12")));
	}

	@Test
	public void getPersonalLoanDetailsTest_notFound() throws Exception {

		int customerId = 1;
		String accountId = "1";

		when(mockPersonalLoanService.getPersonalLoanDetails(customerId, accountId)).thenReturn(null);

		mockMvc.perform(MockMvcRequestBuilders.get("/bo/cs/customer/loan/personal/" + accountId + "/details")
				.header("customerId", customerId))
				.andDo(MockMvcResultHandlers.print())
				.andExpect(MockMvcResultMatchers.status().is2xxSuccessful());
	}

}
