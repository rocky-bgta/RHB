package com.rhbgroup.dcpbo.customer.service;

import com.rhbgroup.dcpbo.customer.dto.CustomerFavourites;
import com.rhbgroup.dcpbo.customer.exception.CommonException;
import com.rhbgroup.dcpbo.customer.model.*;
import com.rhbgroup.dcpbo.customer.repository.ProfileFavouriteRepo;
import com.rhbgroup.dcpbo.customer.repository.TopupBillerRepo;
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
@SpringBootTest(classes = { TopupFavouriteService.class, TopupFavouriteServiceTest.Config.class })
public class TopupFavouriteServiceTest {

    @Autowired
    TopupFavouriteService topupFavouriteService;

    @MockBean
    ProfileFavouriteRepo profileFavouriteRepoMock;

    @MockBean
    TopupBillerRepo topupBillerRepoMock;

    @TestConfiguration
   	static class Config {

   		@Bean
   		@Primary
   		public TopupFavouriteService getTopupFavouriteService() {
   			return new TopupFavouriteService();
   		}

   	}
    
    @Test
    public void retrieveFavouriteTopupDetailsSuccess() throws Exception{
        ProfileFavourite profileFavourite = new ProfileFavourite();
        TopupBiller topupBiller = new TopupBiller();

        profileFavourite.setId(123);
        profileFavourite.setTxnType("TOPUP");
        profileFavourite.setMainFunction("TOPUP");
        profileFavourite.setNickname("Test Nickname");
        profileFavourite.setPayeeId(567);
        profileFavourite.setAmount(new BigDecimal(123.45));
        profileFavourite.setRef1("test Ref1");
        profileFavourite.setIsQuickLink(false);
        profileFavourite.setIsQuickPay(false);

        topupBiller.setBillerName("Maxis");

        when(profileFavouriteRepoMock.findById(123)).thenReturn(profileFavourite);
        when(topupBillerRepoMock.findById(567)).thenReturn(topupBiller);

        CustomerFavourites result = topupFavouriteService.retrieveFavouriteTopupDetails(123);

        assertEquals(new Integer(123), result.getId());
        assertEquals("TOPUP", result.getTxnType());
        assertEquals("TOPUP", result.getMainFunction());
        assertEquals("Maxis", result.getPayeeName());
        assertEquals("Test Nickname", result.getNickname());
        assertEquals("test Ref1", result.getRef1());
        assertEquals(new BigDecimal(123.45), result.getAmount());
        assertEquals(false, result.getIsQuickLink());
        assertEquals(false, result.getIsQuickPay());
    }

    @Test
    public void retrieveFavouriteTopupDetailsFail() throws Exception{
        ProfileFavourite profileFavourite = null;

        when(profileFavouriteRepoMock.findById(123)).thenReturn(profileFavourite);

        try{
            topupFavouriteService.retrieveFavouriteTopupDetails(123);
        } catch (CommonException ex){
            assertEquals(CommonException.CUSTOMER_NOT_FOUND, ex.getErrorCode());
        }
    }
}
