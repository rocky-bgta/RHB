package com.rhbgroup.dcpbo.customer.service;

import com.rhbgroup.dcp.data.repository.CommonRepository;
import com.rhbgroup.dcpbo.customer.exception.CommonException;
import com.rhbgroup.dcpbo.customer.model.*;
import com.rhbgroup.dcpbo.customer.repository.*;
import com.rhbgroup.dcpbo.customer.service.impl.GetTransactionPaymentServiceImpl;
import com.rhbgroup.dcpbo.customer.service.impl.ProfileFavouriteServiceImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest(classes ={
        GetFavouritesTransferService.class,
        GetFavouritesTransferServiceTest.class
} )
public class GetFavouritesTransferServiceTest {

    @Autowired
    private GetFavouritesTransferService getFavouritesTransferService;
    @MockBean
    ProfileFavouriteRepo profileFavouriteRepo;
    @MockBean
    BankRepository bankRepository;
    @MockBean
    LookupListOfValueRepository lookupListOfValueRepository;
    @MockBean
    CountryRepository countryRepository;

    @Test
    public void testRetrieveFavouritesTransferDuitNowSuccess() {
        ProfileFavourite profileFavourite = new ProfileFavourite();
        profileFavourite.setMainFunction("DUITNOW");
        profileFavourite.setTxnType("TRANSFER");
        profileFavourite.setToIdType("PSPT");
        profileFavourite.setSubFunction("LP");
        profileFavourite.setDuitnowCountryCode("DNCC");
        profileFavourite.setPayeeId(1);
        profileFavourite.setAmount(new BigDecimal("-1"));
        Bank bank = new Bank();
        bank.setBankName("CIMB");
        String type = "pay-banks-payment-type";
        String code = "LP";
        Country country = new Country();
        country.setCountryName("Malaysia");

        when(profileFavouriteRepo.findOne(123)).thenReturn(profileFavourite);
        when(bankRepository.findOne(1)).thenReturn(bank);
        when(lookupListOfValueRepository.findDescriptionByTypeAndCode(type ,code)).thenReturn("Loan Payment");
        when(countryRepository.findCountryNameByCountryCode("DNCC")).thenReturn("Malaysia");

        assertEquals("Malaysia",getFavouritesTransferService.retrieveFavouritesTransfer(123).getDuitnowCountryName());
    }

    @Test(expected = CommonException.class )
    public void testRetrieveFavouritesTransferInvalidSubFunction() {
        ProfileFavourite profileFavourite = new ProfileFavourite();
        profileFavourite.setMainFunction("IBG");
        profileFavourite.setTxnType("TRANSFER");
        profileFavourite.setSubFunction("LP");
        profileFavourite.setAmount(new BigDecimal("-1"));
        profileFavourite.setPayeeId(1);
        Bank bank = new Bank();
        bank.setBankName("CIMB");
        String type = "pay-banks-payment-type";
        String code = "LP";

        when(profileFavouriteRepo.findOne(123)).thenReturn(profileFavourite);
        when(bankRepository.findOne(1)).thenReturn(bank);
        when(lookupListOfValueRepository.findDescriptionByTypeAndCode(type ,code)).thenThrow(CommonException.class);

        assertEquals("Loan Payment",getFavouritesTransferService.retrieveFavouritesTransfer(123).getPaymentType().getDescription());
    }

    @Test
    public void testRetrieveFavouritesTransferIBGSuccess() {
        ProfileFavourite profileFavourite = new ProfileFavourite();
        profileFavourite.setMainFunction("IBG");
        profileFavourite.setTxnType("TRANSFER");
        profileFavourite.setSubFunction("LP");
        profileFavourite.setAmount(new BigDecimal("-1"));
        profileFavourite.setPayeeId(1);
        Bank bank = new Bank();
        bank.setBankName("CIMB");
        String type = "pay-banks-payment-type";
        String code = "LP";

        when(profileFavouriteRepo.findOne(123)).thenReturn(profileFavourite);
        when(bankRepository.findOne(1)).thenReturn(bank);
        when(lookupListOfValueRepository.findDescriptionByTypeAndCode(type ,code)).thenReturn("Loan Payment");

        assertEquals("Loan Payment",getFavouritesTransferService.retrieveFavouritesTransfer(123).getPaymentType().getDescription());
    }
}
