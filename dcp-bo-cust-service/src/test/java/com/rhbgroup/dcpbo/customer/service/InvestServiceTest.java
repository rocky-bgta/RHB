package com.rhbgroup.dcpbo.customer.service;

import com.rhbgroup.dcp.data.entity.profiles.UserProfile;
import com.rhbgroup.dcp.data.repository.ProfileRepository;
import com.rhbgroup.dcpbo.customer.dto.CustomerFavourites;
import com.rhbgroup.dcpbo.customer.model.FundDetails;
import com.rhbgroup.dcpbo.customer.model.ProfileFavourite;
import com.rhbgroup.dcpbo.customer.repository.FundRepository;
import com.rhbgroup.dcpbo.customer.repository.ProfileFavouriteRepo;
import com.rhbgroup.dcpbo.customer.service.impl.InvestServiceImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@AutoConfigureMockMvc
@WebAppConfiguration
@EnableAspectJAutoProxy(proxyTargetClass = false)
@SpringBootTest(classes = {InvestServiceTest.class,
        InvestServiceTest.Config.class})
public class InvestServiceTest {

    @Autowired
    InvestServiceImpl getInvestService;

    @MockBean
    private static ProfileFavouriteRepo profileFavRepoMock;

    @MockBean (name = "profileRepository")
    ProfileRepository profileRepositoryMock;

    @MockBean (name = "fundRepository")
    FundRepository fundRepositoryMock;

    @TestConfiguration
    static class Config {
        @Bean
        @Primary
        public InvestService getInvestService() {
            return new InvestServiceImpl();
        }
    }

    @Test
    public void retrieveFavouriteInvestDetailsTest() {

        int customerId = 1;
        int userId = customerId;
        int profileFavouriteId = 111;
        String cisNo = "1234567890";
        String username = "Ikhwan";
        String nickname = "Ikhwan";
        int favId = 1;

        UserProfile userProfile = new UserProfile();
        userProfile.setCisNo(cisNo);
        userProfile.setUsername(username);//ProfileFavourite

        ProfileFavourite favProfile = new ProfileFavourite();
        favProfile.setId(profileFavouriteId);
        favProfile.setUserId(userId);
        favProfile.setTxnType("INVEST");
        favProfile.setRef1("1234");
        favProfile.setNickname(nickname);
        favProfile.setIsQuickLink(true);
        favProfile.setIsQuickPay(true);

        FundDetails fund = new FundDetails();
        fund.setId(1);
        fund.setFundLongName("test");
        fund.setFundShortName("fundShortName");
        fund.setFundType("test3");
        fund.setFundId("1234");

        CustomerFavourites customerFavourites = new CustomerFavourites();
        customerFavourites.setFundName("test");
        customerFavourites.setFundShortName("fundShortName");
        customerFavourites.setId(profileFavouriteId);
        customerFavourites.setIsQuickLink(true);
        customerFavourites.setIsQuickPay(true);
        customerFavourites.setMainFunction("mainFunction");
        customerFavourites.setMembershipNumber("123");
        customerFavourites.setMembersName("test");
        customerFavourites.setNickname(nickname);
        customerFavourites.setPayeeId(1);
        customerFavourites.setRef1("test");
        customerFavourites.setRef2("test");
        customerFavourites.setRef3("test");
        customerFavourites.setRef4("test");

        ProfileFavourite favourite = new ProfileFavourite();
        favourite.setEmail("example");
        favourite.setId(profileFavouriteId);
        favourite.setIsFirstTrx(true);
        favourite.setIsQuickLink(true);
        favourite.setIsQuickLink(true);
        favourite.setIsQuickPay(true);
        favourite.setMainFunction("test");
        favourite.setMobileNo("123");
        favourite.setTxnType("INVEST");

        when(profileFavRepoMock.findById(userId)).thenReturn(favProfile);
        assertEquals(userProfile.getUsername(), username);
        assertEquals(userProfile.getCisNo(), cisNo);

        when(fundRepositoryMock.findByfundId(favProfile.getRef1())).thenReturn(fund);

        CustomerFavourites customerFavourites2 = getInvestService.retrieveFavouriteInvestDetails(favId);
        if (favProfile.getTxnType().equals("INVEST")) {

            assertEquals(customerFavourites2.getId(), customerFavourites.getId());
            assertEquals(customerFavourites2.getNickname(), customerFavourites.getNickname());
            assertEquals(customerFavourites2.getFundName(), customerFavourites.getFundName());
            assertEquals(customerFavourites2.getFundShortName(), customerFavourites.getFundShortName());
            assertEquals(customerFavourites2.getAmount(), customerFavourites.getAmount());

            assertEquals(customerFavourites2.getIsQuickLink(), customerFavourites.getIsQuickLink());
            assertEquals(customerFavourites2.getIsQuickPay(), customerFavourites.getIsQuickPay());
        }

    }
}
