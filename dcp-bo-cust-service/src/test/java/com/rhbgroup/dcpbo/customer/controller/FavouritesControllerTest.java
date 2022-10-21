package com.rhbgroup.dcpbo.customer.controller;

import com.rhbgroup.dcpbo.customer.aspect.ApiContext;
import com.rhbgroup.dcpbo.customer.client.ConfigErrorInterface;
import com.rhbgroup.dcpbo.customer.dto.CustomerFavourites;
import com.rhbgroup.dcpbo.customer.exception.CommonException;
import com.rhbgroup.dcpbo.customer.exception.CommonExceptionAdvice;
import com.rhbgroup.dcpbo.customer.model.response.BoExceptionResponse;
import com.rhbgroup.dcpbo.customer.service.InvestService;
import com.rhbgroup.dcpbo.customer.service.PaymentFavouriteService;
import com.rhbgroup.dcpbo.customer.service.TopupFavouriteService;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.math.BigDecimal;

import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest(classes = {FavouritesControllerTest.class, FavouritesControllerTest.Config.class,
        FavouritesController.class})
@EnableWebMvc
public class FavouritesControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    InvestService investServiceMock;

    @MockBean
    TopupFavouriteService topupFavouriteServiceMock;

    @MockBean
    PaymentFavouriteService paymentFavouriteServiceMock;

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
        ApiContext apiState() {
            return new ApiContext();
        }
    }

    @Test
    public void getFavouriteTopup() throws Exception {
        CustomerFavourites favouritesTopup = new CustomerFavourites();

        favouritesTopup.setId(123);
        favouritesTopup.setTxnType("TRANSFER");
        favouritesTopup.setMainFunction("JOMPAY_BILLER");
        favouritesTopup.setPayeeName("Tune Talk");
        favouritesTopup.setNickname("SOMEWHERE");
        favouritesTopup.setAmount(new BigDecimal("123.45"));
        favouritesTopup.setRef1("CAPRICORN");
        favouritesTopup.setIsQuickLink(false);
        favouritesTopup.setIsQuickPay(false);

        when(topupFavouriteServiceMock.retrieveFavouriteTopupDetails(123)).thenReturn(favouritesTopup);

        mockMvc.perform(MockMvcRequestBuilders.get("/bo/cs/favourites/123/topup"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andExpect(MockMvcResultMatchers.jsonPath("id", Matchers.is(123)))
                .andExpect(MockMvcResultMatchers.jsonPath("txnType", Matchers.is("TRANSFER")))
                .andExpect(MockMvcResultMatchers.jsonPath("mainFunction", Matchers.is("JOMPAY_BILLER")))
                .andExpect(MockMvcResultMatchers.jsonPath("payeeName", Matchers.is("Tune Talk")))
                .andExpect(MockMvcResultMatchers.jsonPath("nickname", Matchers.is("SOMEWHERE")))
                .andExpect(MockMvcResultMatchers.jsonPath("ref1", Matchers.is("CAPRICORN")))
                .andExpect(MockMvcResultMatchers.jsonPath("amount", Matchers.is(123.45)))
                .andExpect(MockMvcResultMatchers.jsonPath("isQuickLink", Matchers.is(false)))
                .andExpect(MockMvcResultMatchers.jsonPath("isQuickPay", Matchers.is(false)));
    }

    @Test
    public void retrievePaymentDetails() throws Exception {
        CustomerFavourites favouritesPayment = new CustomerFavourites();

        favouritesPayment.setId(83);
        favouritesPayment.setTxnType("PAYMENT");
        favouritesPayment.setMainFunction("OTHER_BILLER");
        favouritesPayment.setPayeeId(12345);
        favouritesPayment.setPayeeName("Maxis");
        favouritesPayment.setNickname("Dad's ransom money");
        favouritesPayment.setAmount(new BigDecimal("123.45"));
        favouritesPayment.setRef1("test ref1");
        favouritesPayment.setRef2("test ref2");
        favouritesPayment.setRef3("test ref3");
        favouritesPayment.setRef4("test ref4");
        favouritesPayment.setIsQuickLink(false);
        favouritesPayment.setIsQuickPay(false);

        when(paymentFavouriteServiceMock.retrieveFavouritePaymentDetails(83)).thenReturn(favouritesPayment);

        mockMvc.perform(MockMvcRequestBuilders.get("/bo/cs/favourites/83/payment"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andExpect(MockMvcResultMatchers.jsonPath("id", Matchers.is(83)))
                .andExpect(MockMvcResultMatchers.jsonPath("txnType", Matchers.is("PAYMENT")))
                .andExpect(MockMvcResultMatchers.jsonPath("mainFunction", Matchers.is("OTHER_BILLER")))
                .andExpect(MockMvcResultMatchers.jsonPath("payeeId", Matchers.is(12345)))
                .andExpect(MockMvcResultMatchers.jsonPath("payeeName", Matchers.is("Maxis")))
                .andExpect(MockMvcResultMatchers.jsonPath("nickname", Matchers.is("Dad's ransom money")))
                .andExpect(MockMvcResultMatchers.jsonPath("amount", Matchers.is(123.45)))
                .andExpect(MockMvcResultMatchers.jsonPath("ref1", Matchers.is("test ref1")))
                .andExpect(MockMvcResultMatchers.jsonPath("ref2", Matchers.is("test ref2")))
                .andExpect(MockMvcResultMatchers.jsonPath("ref3", Matchers.is("test ref3")))
                .andExpect(MockMvcResultMatchers.jsonPath("ref4", Matchers.is("test ref4")))
                .andExpect(MockMvcResultMatchers.jsonPath("isQuickLink", Matchers.is(false)))
                .andExpect(MockMvcResultMatchers.jsonPath("isQuickPay", Matchers.is(false)));
    }
}
