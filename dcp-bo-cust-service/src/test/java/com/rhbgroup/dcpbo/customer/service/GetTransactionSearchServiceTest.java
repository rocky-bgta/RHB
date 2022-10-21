package com.rhbgroup.dcpbo.customer.service;

import com.rhbgroup.dcpbo.customer.exception.CommonException;
import com.rhbgroup.dcpbo.customer.model.*;
import com.rhbgroup.dcpbo.customer.repository.*;
import com.rhbgroup.dcpbo.customer.service.impl.GetTransactionSearchServiceImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { GetTransactionSearchServiceImpl.class, GetTransactionSearchServiceTest.Config.class })
public class GetTransactionSearchServiceTest {

    @Autowired
    private GetTransactionSearchServiceImpl getTransactionSearchService;
    @MockBean
    private PaymentTxnRepository paymentTxnRepository;
    @MockBean
    private TransferTxnRepository transferTxnRepository;
    @MockBean
    private TopupTxnRepository topupTxnRepository;
    @MockBean
    private UserProfileRepository userProfileRepository;

    @TestConfiguration
   	static class Config {

   		@Bean
   		@Primary
   		public GetTransactionSearchServiceImpl getGetTransactionSearchServiceImpl() {
   			return new GetTransactionSearchServiceImpl();
   		}
   		
   	}
    
    @Test
    public void testRetrieveTransactionSearchPaymentTypeByRefId() {
        PaymentTxn paymentTxn = new PaymentTxn();
        paymentTxn.setUserId(2210122);
        TransferTxn transferTxn = new TransferTxn();
        transferTxn.setUserId(2210122);
        TopupTxn topupTxn = new TopupTxn();
        topupTxn.setUserId(2210122);
        UserProfile userProfile = new UserProfile();
        userProfile.setUserStatus("A");

        when(paymentTxnRepository.findByRefId("79731234")).thenReturn(paymentTxn);
        when(transferTxnRepository.findByRefId("79731234")).thenReturn(transferTxn);
        when(topupTxnRepository.findByRefId("79731234")).thenReturn(topupTxn);
        when(userProfileRepository.findOne(2210122)).thenReturn(userProfile);

        assertEquals( "PAYMENT" , getTransactionSearchService.retrieveTransactionSearch("79731234").get(0).getTransaction().getTxnType());
    }

    @Test
    public void testRetrieveTransactionSearchTransferTypeByRefId() {
        PaymentTxn paymentTxn = new PaymentTxn();
        paymentTxn.setUserId(2210122);
        TransferTxn transferTxn = new TransferTxn();
        transferTxn.setUserId(2210122);
        TopupTxn topupTxn = new TopupTxn();
        topupTxn.setUserId(2210122);
        UserProfile userProfile = new UserProfile();
        userProfile.setUserStatus("A");

        when(paymentTxnRepository.findByRefId("88888")).thenReturn(paymentTxn);
        when(transferTxnRepository.findByRefId("79731234")).thenReturn(transferTxn);
        when(topupTxnRepository.findByRefId("79731234")).thenReturn(topupTxn);
        when(userProfileRepository.findOne(2210122)).thenReturn(userProfile);

        assertEquals( "TRANSFER" , getTransactionSearchService.retrieveTransactionSearch("79731234").get(0).getTransaction().getTxnType());
    }

    @Test
    public void testRetrieveTransactionSearchTopupTypeByRefId() {
        PaymentTxn paymentTxn = new PaymentTxn();
        paymentTxn.setUserId(2210122);
        TransferTxn transferTxn = new TransferTxn();
        transferTxn.setUserId(2210122);
        TopupTxn topupTxn = new TopupTxn();
        topupTxn.setUserId(2210122);
        UserProfile userProfile = new UserProfile();
        userProfile.setUserStatus("A");

        when(paymentTxnRepository.findByRefId("88888")).thenReturn(paymentTxn);
        when(transferTxnRepository.findByRefId("88888")).thenReturn(transferTxn);
        when(topupTxnRepository.findByRefId("79731234")).thenReturn(topupTxn);
        when(userProfileRepository.findOne(2210122)).thenReturn(userProfile);

        assertEquals( "TOPUP" , getTransactionSearchService.retrieveTransactionSearch("79731234").get(0).getTransaction().getTxnType());
    }

    @Test(expected = CommonException.class)
    public void testRetrieveTransactionSearchInvalidRefIdForTransaction() {
        PaymentTxn paymentTxn = new PaymentTxn();
        paymentTxn.setUserId(2210122);
        TransferTxn transferTxn = new TransferTxn();
        transferTxn.setUserId(2210122);
        TopupTxn topupTxn = new TopupTxn();
        topupTxn.setUserId(2210122);
        UserProfile userProfile = new UserProfile();
        userProfile.setUserStatus("A");

        when(paymentTxnRepository.findByRefId("888888")).thenReturn(paymentTxn);
        when(transferTxnRepository.findByRefId("88888")).thenReturn(transferTxn);
        when(topupTxnRepository.findByRefId("88888")).thenReturn(topupTxn);
        when(userProfileRepository.findOne(2210122)).thenReturn(userProfile);

        assertEquals( "NOT_FOUND" , getTransactionSearchService.retrieveTransactionSearch("79731234").get(0).getTransaction().getTxnType());
    }

    @Test(expected = CommonException.class  )
    public void testRetrieveTransactionSearchInvalidUserIdForUserProfile() {
        PaymentTxn paymentTxn = new PaymentTxn();
        paymentTxn.setUserId(2210122);
        TransferTxn transferTxn = new TransferTxn();
        transferTxn.setUserId(2210122);
        TopupTxn topupTxn = new TopupTxn();
        topupTxn.setUserId(2210122);
        UserProfile userProfile = new UserProfile();
        userProfile.setUserStatus("A");

        when(paymentTxnRepository.findByRefId("79731234")).thenReturn(paymentTxn);
        when(transferTxnRepository.findByRefId("79731234")).thenReturn(transferTxn);
        when(topupTxnRepository.findByRefId("79731234")).thenReturn(topupTxn);
        when(userProfileRepository.findOne(8888)).thenReturn(userProfile);

        assertEquals( "PAYMENT" , getTransactionSearchService.retrieveTransactionSearch("79731234").get(0).getTransaction().getTxnType());
    }
}
