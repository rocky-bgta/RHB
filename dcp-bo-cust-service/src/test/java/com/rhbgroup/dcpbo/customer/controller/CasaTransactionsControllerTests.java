package com.rhbgroup.dcpbo.customer.controller;

import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.hamcrest.Matchers;
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

import com.rhbgroup.dcp.deposits.casa.model.DcpDepositTransactionPagination;
import com.rhbgroup.dcp.eai.adaptors.transactionhistory.model.DcpTransaction;
import com.rhbgroup.dcpbo.customer.client.ConfigErrorInterface;
import com.rhbgroup.dcpbo.customer.dto.CasaTransactions;
import com.rhbgroup.dcpbo.customer.exception.CommonException;
import com.rhbgroup.dcpbo.customer.exception.CommonExceptionAdvice;
import com.rhbgroup.dcpbo.customer.model.response.BoExceptionResponse;
import com.rhbgroup.dcpbo.customer.service.CasaDetailsService;
import com.rhbgroup.dcpbo.customer.service.CasaTransactionsService;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest(classes = { CasaTransactionsControllerTests.class, CasaTransactionsControllerTests.Config.class, CasaController.class })
@EnableWebMvc
public class CasaTransactionsControllerTests {
	@Autowired
	MockMvc mockMvc;
	
	@MockBean
	CasaTransactionsService casaTransactionsServiceMock;
	
	@MockBean
	CasaDetailsService casaDetailsServiceMock;
	
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

	int customerId = 1;

	String accountNo = "1";
	String firstKey = "361";
	String lastKey = "359";
	int pageCounter = 2;

	private static Logger logger = LogManager.getLogger(CasaTransactionsControllerTests.class);

	@Test
	public void getCasaTransactionsDetailsTest() throws Exception {
		logger.debug("getCasaTransactionsDetailsTest()");
		logger.debug("    casaTransactionsServiceMock: " + casaTransactionsServiceMock);
		
		DcpDepositTransactionPagination pagination = new DcpDepositTransactionPagination();
		pagination.setFirstKey(firstKey);
		pagination.setLastKey(lastKey);
		pagination.setPageCounter(pageCounter);
		
		DcpTransaction dcpTransaction = new DcpTransaction();
		dcpTransaction.setDescription("Cash Withdraw WB");
		dcpTransaction.setSenderName("John Doe");
		dcpTransaction.setRecipientRef("Payment for savings");
		dcpTransaction.setOtherPaymentDetails("Meals");
		dcpTransaction.setRef1("Debit card ref here");
		dcpTransaction.setTxnDate("2018-02-02T00:00:00.000+08:00");
		dcpTransaction.setAmount(new BigDecimal(34.05));

		List<DcpTransaction> transactionHistory = new LinkedList<DcpTransaction>();
		transactionHistory.add(dcpTransaction);
		
		CasaTransactions casaTransactions = new CasaTransactions();
		casaTransactions.setPagination(pagination);
		casaTransactions.setTransactionHistory(transactionHistory);
		
		when(casaTransactionsServiceMock.getCasaTransactions(customerId, accountNo, firstKey, lastKey, pageCounter)).thenReturn(casaTransactions);

		String url = "/bo/cs/customer/casa/" + accountNo + "/transactions/?pageCounter=" + pageCounter + "&firstKey=" + firstKey + "&lastKey=" + lastKey;
		logger.debug("    url: " + url);

		mockMvc.perform(MockMvcRequestBuilders.get(url).header("customerId", customerId))
				.andDo(MockMvcResultHandlers.print())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
				.andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
				.andExpect(MockMvcResultMatchers.jsonPath("pagination.firstKey", Matchers.is(pagination.getFirstKey())))
				.andExpect(MockMvcResultMatchers.jsonPath("pagination.lastKey", Matchers.is(pagination.getLastKey())))
				.andExpect(MockMvcResultMatchers.jsonPath("pagination.pageCounter", Matchers.is(pagination.getPageCounter())))
				.andExpect(MockMvcResultMatchers.jsonPath("transactionHistory[0].description", Matchers.is(dcpTransaction.getDescription())))
				.andExpect(MockMvcResultMatchers.jsonPath("transactionHistory[0].senderName", Matchers.is(dcpTransaction.getSenderName())))
				.andExpect(MockMvcResultMatchers.jsonPath("transactionHistory[0].recipientRef", Matchers.is(dcpTransaction.getRecipientRef())))
				.andExpect(MockMvcResultMatchers.jsonPath("transactionHistory[0].otherPaymentDetails", Matchers.is(dcpTransaction.getOtherPaymentDetails())))
				.andExpect(MockMvcResultMatchers.jsonPath("transactionHistory[0].ref1", Matchers.is(dcpTransaction.getRef1())))
				.andExpect(MockMvcResultMatchers.jsonPath("transactionHistory[0].txnDate", Matchers.is(dcpTransaction.getTxnDate())))
				.andExpect(MockMvcResultMatchers.jsonPath("transactionHistory[0].amount", Matchers.comparesEqualTo(dcpTransaction.getAmount())));
	}

	@Test
	public void getCasaTransactionsDetailsTest_notFound() throws Exception {
		logger.debug("getCasaTransactionsDetailsTest_notFound()");
		logger.debug("    casaTransactionsServiceMock: " + casaTransactionsServiceMock);
		
		int customerId = 1;
		
		when(casaTransactionsServiceMock.getCasaTransactions(Mockito.anyInt(), Mockito.anyString(), Mockito.any(),
				Mockito.any(), Mockito.anyInt())).thenThrow(new CommonException(CommonException.GENERIC_ERROR_CODE));

		String url = "/bo/cs/customer/casa/" + accountNo + "/transactions/?pageCounter=" + pageCounter + "&firstKey=" + firstKey + "&lastKey=" + lastKey;
		logger.debug("    url: " + url);

		mockMvc.perform(MockMvcRequestBuilders.get(url).header("customerId", customerId))
				.andDo(MockMvcResultHandlers.print())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
				.andExpect(MockMvcResultMatchers.status().is5xxServerError());
	}
}
