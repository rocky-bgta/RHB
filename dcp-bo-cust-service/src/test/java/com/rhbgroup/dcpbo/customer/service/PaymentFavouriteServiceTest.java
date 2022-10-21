package com.rhbgroup.dcpbo.customer.service;

import com.rhbgroup.dcpbo.customer.dto.CustomerFavourites;
import com.rhbgroup.dcpbo.customer.exception.CommonException;
import com.rhbgroup.dcpbo.customer.model.Biller;
import com.rhbgroup.dcpbo.customer.model.ProfileFavourite;
import com.rhbgroup.dcpbo.customer.repository.BillerRepo;
import com.rhbgroup.dcpbo.customer.repository.ProfileFavouriteRepo;
import com.rhbgroup.dcpbo.customer.service.impl.GetTransactionTopupServiceImpl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { PaymentFavouriteService.class, PaymentFavouriteServiceTest.Config.class })
public class PaymentFavouriteServiceTest {

    @Autowired
    PaymentFavouriteService paymentFavouriteService;

    @MockBean
    ProfileFavouriteRepo profileFavouriteRepoMock;

    @MockBean
    BillerRepo billerRepoMock;
    
    @TestConfiguration
   	static class Config {

   		@Bean
   		@Primary
   		public PaymentFavouriteService getPaymentFavouriteService() {
   			return new PaymentFavouriteService();
   		}
   		
   	}
    
    @Test
    public void retrieveFavouritePaymentDetailsSuccess() throws Exception{
        ProfileFavourite profileFavourite = new ProfileFavourite();
        Biller biller = new Biller();

        profileFavourite.setId(123);
        profileFavourite.setTxnType("PAYMENT");
        profileFavourite.setMainFunction("OTHER_BILLER");
        profileFavourite.setNickname("Test Nickname");
        profileFavourite.setAmount(new BigDecimal(123.45));
        profileFavourite.setRef1("test Ref1");
        profileFavourite.setIsQuickLink(false);
        profileFavourite.setIsQuickPay(false);
        profileFavourite.setPayeeId(456);

        biller.setBillerName("Tenaga Nasional Berhad");

        when(profileFavouriteRepoMock.findById(123)).thenReturn(profileFavourite);
        when(billerRepoMock.findById(456)).thenReturn(biller);

        CustomerFavourites result = paymentFavouriteService.retrieveFavouritePaymentDetails(123);

        assertEquals(new Integer(123) ,profileFavourite.getId());
        assertEquals("PAYMENT" ,profileFavourite.getTxnType());
        assertEquals("OTHER_BILLER" ,profileFavourite.getMainFunction());
        assertEquals(new Integer(456) ,profileFavourite.getPayeeId());
        assertEquals("Tenaga Nasional Berhad" ,biller.getBillerName());
        assertEquals("Test Nickname" ,profileFavourite.getNickname());
        assertEquals(new BigDecimal(123.45) ,profileFavourite.getAmount());
        assertEquals("test Ref1" ,profileFavourite.getRef1());
        assertEquals(false ,profileFavourite.getIsQuickLink());
        assertEquals(false ,profileFavourite.getIsQuickPay());
    }

    @Test
    public void retrieveFavouritePaymentDetailsFail() throws Exception{
        ProfileFavourite profileFavourite = null;

        when(profileFavouriteRepoMock.findById(123)).thenReturn(profileFavourite);

        try{
            paymentFavouriteService.retrieveFavouritePaymentDetails(123);
        } catch (CommonException ex){
            assertEquals(CommonException.CUSTOMER_NOT_FOUND, ex.getErrorCode());
        }
    }

}
