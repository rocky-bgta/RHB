package com.rhbgroup.dcpbo.customer.controller;

import com.rhbgroup.dcp.model.Capsule;
import com.rhbgroup.dcpbo.customer.contract.ViewTransaction;
import com.rhbgroup.dcpbo.customer.dto.MortgageTransactions;
import com.rhbgroup.dcpbo.customer.service.GetMortgageTransactionService;

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
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest(classes = { MortgageTransactionsControllerTest.class, 
		LoanController.class })
@EnableWebMvc
public class MortgageTransactionsControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean(name = "viewTransactionAsb")
    private ViewTransaction viewTransaction;

    @MockBean(name = "viewTransactionPersonalLoan")
    private ViewTransaction viewTransactionPersonalLoan;

    @MockBean(name = "viewHirePurchaseTransactionLoan")
    private ViewTransaction hirePurchaseTransactionLoan;
    
    @MockBean
    GetMortgageTransactionService getMortgageTransactionServiceMock;

    
    @Test
    public void GetMortgageTransactionsTest() throws Exception{

        MortgageTransactions testMortgageTransactions = new MortgageTransactions();

        String jsonString = "{" +
                "  \"pagination\": {" +
                "    \"firstKey\": \"1\"," +
                "    \"lastKey\": \"2\"," +
                "    \"isLastPage\": false," +
                "    \"pageCounter\": 1" +
                "  }," +
                "  \"transactionHistory\": [" +
                "    {" +
                "      \"txnDate\": \"2018-06-19T00:00:00.000+08:00\"," +
                "      \"description\": \"Ref1 Ref2\"," +
                "      \"amount\": 12345.67" +
                "    }," +
                "    {" +
                "      \"txnDate\": \"2018-06-19T00:00:00.000+08:00\"," +
                "      \"description\": \"Ref3 Ref4\"," +
                "      \"amount\": 12345.68" +
                "    }" +
                "  ]" +
                "}";
        Capsule requestCapsule = new Capsule();
        requestCapsule.updateCurrentMessage(jsonString);
        testMortgageTransactions.convert(requestCapsule);

        when(getMortgageTransactionServiceMock.getMortgageTransactions(1, "123", 1, "1", "2")).thenReturn(testMortgageTransactions);

        mockMvc.perform(MockMvcRequestBuilders.get("/bo/cs/customer/loan/mortgage/123/transactions?pageCounter=1&firstKey=1&lastKey=2")
                .header("customerId", 1))
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("pagination.firstKey", Matchers.is("1")))
                .andExpect(MockMvcResultMatchers.jsonPath("pagination.lastKey", Matchers.is("2")))
                .andExpect(MockMvcResultMatchers.jsonPath("pagination.pageCounter", Matchers.is(1)))
                .andExpect(MockMvcResultMatchers.jsonPath("pagination.isLastPage", Matchers.is(false)))
                .andExpect(MockMvcResultMatchers.jsonPath("transactionHistory[0].txnDate", Matchers.is("2018-06-19T00:00:00.000+08:00")))
                .andExpect(MockMvcResultMatchers.jsonPath("transactionHistory[0].description", Matchers.is("Ref1 Ref2")))
                .andExpect(MockMvcResultMatchers.jsonPath("transactionHistory[0].amount", Matchers.is(12345.67)))
                .andExpect(MockMvcResultMatchers.jsonPath("transactionHistory[1].txnDate", Matchers.is("2018-06-19T00:00:00.000+08:00")))
                .andExpect(MockMvcResultMatchers.jsonPath("transactionHistory[1].description", Matchers.is("Ref3 Ref4")))
                .andExpect(MockMvcResultMatchers.jsonPath("transactionHistory[1].amount", Matchers.is(12345.68)));

    }
}
