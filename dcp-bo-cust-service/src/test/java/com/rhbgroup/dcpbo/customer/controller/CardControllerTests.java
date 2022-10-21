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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import com.rhbgroup.dcp.creditcards.model.DcpSuppCard;
import com.rhbgroup.dcpbo.customer.model.CreditCardDetails;
import com.rhbgroup.dcpbo.customer.service.CardDetailsService;
import com.rhbgroup.dcpbo.customer.service.CardTransactionsService;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest(classes = {CardControllerTests.class, CardController.class})
@EnableWebMvc
public class CardControllerTests {
	@Autowired
	MockMvc mockMvc;

	@MockBean
	CardDetailsService cardDetailsServiceMock;
	
	@MockBean
	CardTransactionsService cardTransactionsServiceMock;
	
	private static Logger logger = LogManager.getLogger(CardControllerTests.class);

	@Test
	public void getCardDetailsTest() throws Exception {
		logger.debug("getCardDetailsTest()");
		logger.debug("    cardDetailsServiceMock: " + cardDetailsServiceMock);
		
		int customerId = 1;
		String cardNo = "2";
		String channelFlag="D";
		String blockCode = "A";
		String connectorCode = "PRIIND";
		String accountBlockCode  = "Q";
		
		CreditCardDetails cardDetails = new CreditCardDetails();
		cardDetails.setOutstandingBalance(new BigDecimal(1234.56));
		cardDetails.setStatementBalance(new BigDecimal(2345.67));
		cardDetails.setAvailableLimit(new BigDecimal(10000));
		cardDetails.setAvailableCredit(new BigDecimal(15000));
		cardDetails.setCreditLimit(new BigDecimal(9000));
		cardDetails.setPaymentDueDate("2018-02-01T17:00:00.000+08:00");
		cardDetails.setMinPaymentDue(new BigDecimal(123.45));
		cardDetails.setRewardPointBalance(new BigDecimal(123));
		addSupplementaryCard(cardDetails, "1234567890123456", 3000, 1234.56, "SITI BINTI MAZLAN", "MASTER");
		addSupplementaryCard(cardDetails, "2345678901234567", 4000, 2345.67, "ALI BIN MAHMUD", "VISA");
		
		when(cardDetailsServiceMock.getCardDetails(customerId, cardNo, channelFlag, connectorCode, blockCode, accountBlockCode)).thenReturn(cardDetails);

		mockMvc.perform(MockMvcRequestBuilders.get("/bo/cs/customer/card/" + cardNo + "/" + channelFlag + "/" + connectorCode + "/" + blockCode + "/" + accountBlockCode + "/details").header("customerId", customerId))
				.andDo(MockMvcResultHandlers.print())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
				.andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
				.andExpect(MockMvcResultMatchers.jsonPath("outstandingBalance", Matchers.comparesEqualTo(new BigDecimal(1234.56))))
				.andExpect(MockMvcResultMatchers.jsonPath("statementBalance", Matchers.comparesEqualTo(new BigDecimal(2345.67))))
				.andExpect(MockMvcResultMatchers.jsonPath("availableLimit", Matchers.is(10000)))
				.andExpect(MockMvcResultMatchers.jsonPath("availableCredit", Matchers.is(15000)))
				.andExpect(MockMvcResultMatchers.jsonPath("creditLimit", Matchers.is(9000)))
				.andExpect(MockMvcResultMatchers.jsonPath("paymentDueDate", Matchers.is("2018-02-01T17:00:00.000+08:00")))
				.andExpect(MockMvcResultMatchers.jsonPath("minPaymentDue", Matchers.comparesEqualTo(new BigDecimal(123.45))))
				.andExpect(MockMvcResultMatchers.jsonPath("rewardPointBalance", Matchers.is(123)))
				.andExpect(MockMvcResultMatchers.jsonPath("suppCards[0].cardNo", Matchers.is("1234567890123456")))
				.andExpect(MockMvcResultMatchers.jsonPath("suppCards[0].suppCreditLimit", Matchers.is(3000)))
				.andExpect(MockMvcResultMatchers.jsonPath("suppCards[0].outstandingBalance", Matchers.comparesEqualTo(new BigDecimal(1234.56))))
				.andExpect(MockMvcResultMatchers.jsonPath("suppCards[0].embossName", Matchers.is("SITI BINTI MAZLAN")))
				.andExpect(MockMvcResultMatchers.jsonPath("suppCards[0].paymentNetwork", Matchers.is("MASTER")))
				.andExpect(MockMvcResultMatchers.jsonPath("suppCards[1].cardNo", Matchers.is("2345678901234567")))
				.andExpect(MockMvcResultMatchers.jsonPath("suppCards[1].suppCreditLimit", Matchers.is(4000)))
				.andExpect(MockMvcResultMatchers.jsonPath("suppCards[1].outstandingBalance", Matchers.comparesEqualTo(new BigDecimal(2345.67))))
				.andExpect(MockMvcResultMatchers.jsonPath("suppCards[1].embossName", Matchers.is("ALI BIN MAHMUD")))
				.andExpect(MockMvcResultMatchers.jsonPath("suppCards[1].paymentNetwork", Matchers.is("VISA")));
	}

	private void addSupplementaryCard(CreditCardDetails cardDetails, String cardNo, int creditLimit, double outstandingBalance,
			String embossName, String paymentNetwork) {
		List<DcpSuppCard> suppCardList = cardDetails.getSuppCards();
		if (suppCardList == null) {
			suppCardList = new LinkedList<DcpSuppCard>();
			cardDetails.setSuppCards(suppCardList);
		}
		
		DcpSuppCard suppCard = new DcpSuppCard();
		suppCard.setCardNo(cardNo);
		suppCard.setSuppCreditLimit(new BigDecimal(creditLimit));
		suppCard.setOutstandingBalance(new BigDecimal(outstandingBalance));
		suppCard.setEmbossName(embossName);
		suppCard.setPaymentNetwork(paymentNetwork);
		suppCardList.add(suppCard);
	}
}
