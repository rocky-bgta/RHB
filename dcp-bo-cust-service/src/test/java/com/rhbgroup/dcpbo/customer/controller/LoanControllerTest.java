package com.rhbgroup.dcpbo.customer.controller;

import com.rhbgroup.dcpbo.customer.contract.ViewTransaction;
import com.rhbgroup.dcpbo.customer.dto.*;
import com.rhbgroup.dcpbo.customer.service.GetMortgageTransactionService;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.sql.Timestamp;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest(classes = { LoanControllerTest.class, 
		LoanController.class })
@EnableWebMvc
public class LoanControllerTest {

    @Autowired
    protected MockMvc mockMvc;
    
    @MockBean(name = "viewTransactionAsb")
    private ViewTransaction viewTransaction;

    @MockBean(name = "viewTransactionPersonalLoan")
    private ViewTransaction viewTransactionPersonalLoan;

    @MockBean(name = "viewHirePurchaseTransactionLoan")
    private ViewTransaction hirePurchaseTransactionLoan;

    private AsbTransactions asbTransactions;

    private LoanPersonalTransactions loanPersonalTransactions;

    private HirePurchaseTransactions hirePurchaseTransactions;
    
    @MockBean
    private GetMortgageTransactionService getMortgageTransactionService;
    
    @Before
    public void setup() {
        asbTransactions = new AsbTransactions();
        loanPersonalTransactions = new LoanPersonalTransactions();
        hirePurchaseTransactions = new HirePurchaseTransactions();
        Pagination pagination = new Pagination();
        pagination.setFirstKey("20");
        pagination.setLastKey("40");
        pagination.setIsLastPage(true);
        pagination.setPageCounter(4);

        TransactionHistory transactionHistory1 = new TransactionHistory();
        transactionHistory1.setAmount(300.40);
        transactionHistory1.setDescription("baki semalam");
        transactionHistory1.setTxnDate(new Timestamp(System.currentTimeMillis()).toString());
        asbTransactions.addTransactionHistory(transactionHistory1);
        loanPersonalTransactions.addTransactionHistory(transactionHistory1);
        hirePurchaseTransactions.addTransactionHistory(transactionHistory1);

        TransactionHistory transactionHistory2 = new TransactionHistory();
        transactionHistory2.setAmount(590990.999);
        transactionHistory2.setDescription("baki kelmarin");
        transactionHistory2.setTxnDate(new Timestamp(System.currentTimeMillis()).toString());
        asbTransactions.addTransactionHistory(transactionHistory2);
        loanPersonalTransactions.addTransactionHistory(transactionHistory2);
        hirePurchaseTransactions.addTransactionHistory(transactionHistory2);

        asbTransactions.setPagination(pagination);
        loanPersonalTransactions.setPagination(pagination);
        hirePurchaseTransactions.setPagination(pagination);
    }

    @Test
    public void transactionHistoryFound() throws Exception {
        BDDMockito.given(this.viewTransaction.listing(666, "1", 2, "361", "359"))
                .willReturn(this.asbTransactions);

        this.mockMvc.perform(MockMvcRequestBuilders.get("/bo/cs/customer/loan/asb/1/transactions/?pageCounter=2&firstKey=361&lastKey=359")
                .header("customerId", 666))
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("pagination.pageCounter", Matchers.is(4)))
                .andExpect(MockMvcResultMatchers.jsonPath("transactionHistory[0].amount", Matchers.is(300.40)));

    }

    @Test
    public void loanPersonalTransactionsFound() throws Exception {
        BDDMockito.given(this.viewTransactionPersonalLoan.listing(666, "1", 2, "361", "359"))
                .willReturn(this.loanPersonalTransactions);

        this.mockMvc.perform(MockMvcRequestBuilders.get("/bo/cs/customer/loan/personal/1/transactions/?pageCounter=2&firstKey=361&lastKey=359")
                .header("customerId", 666))
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("pagination.pageCounter", Matchers.is(4)))
                .andExpect(MockMvcResultMatchers.jsonPath("transactionHistory[0].amount", Matchers.is(300.40)));
    }

    @Test
    public void hirePurchaseTransactionsFound() throws Exception {
        BDDMockito.given(this.hirePurchaseTransactionLoan.listing(666, "1", 2, "361", "359"))
                .willReturn(this.hirePurchaseTransactions);

        this.mockMvc.perform(MockMvcRequestBuilders.get("/bo/cs/customer/loan/hp/1/transactions/?pageCounter=2&firstKey=361&lastKey=359")
                .header("customerId", 666))
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("pagination.pageCounter", Matchers.is(4)))
                .andExpect(MockMvcResultMatchers.jsonPath("transactionHistory[0].amount", Matchers.is(300.40)));
    }
}