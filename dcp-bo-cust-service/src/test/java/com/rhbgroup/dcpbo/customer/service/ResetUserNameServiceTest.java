package com.rhbgroup.dcpbo.customer.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.junit4.SpringRunner;

import com.rhbgroup.dcpbo.customer.ServiceBeanConfiguration;
import com.rhbgroup.dcpbo.customer.audit.collector.AdditionalDataHolder;
import com.rhbgroup.dcpbo.customer.audit.collector.PutUnblockFacilityAdditionalDataRetriever;
import com.rhbgroup.dcpbo.customer.contract.BoData;
import com.rhbgroup.dcpbo.customer.dto.ResetUserNameResponse;
import com.rhbgroup.dcpbo.customer.model.UserProfile;
import com.rhbgroup.dcpbo.customer.repository.CardProfileRepository;
import com.rhbgroup.dcpbo.customer.repository.UserProfileRepository;
import com.rhbgroup.dcpbo.customer.service.impl.ResetUserNameServiceImpl;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { ResetUserNameService.class, ResetUserNameServiceTest.Config.class })
public class ResetUserNameServiceTest {
	
	@Autowired
	ResetUserNameService resetUserNameService;

	@MockBean
	private UserProfileRepository userProfileRepositoryMock;

	@MockBean
	private CardProfileRepository cardProfileRepositoryMock;
	
	@MockBean
	private AdditionalDataHolder additionalDataHolder;
	
	@TestConfiguration
	static class Config {
		@Bean
		@Primary
		public ResetUserNameService getResetUserNameService() {
			return new ResetUserNameServiceImpl();
		}
		
		@Bean
		public AdditionalDataHolder getAdditionalDataHolder()
		{
			return new AdditionalDataHolder();
		}
		
		@Bean
		public PutUnblockFacilityAdditionalDataRetriever getPutUnblockFacilityAdditionalDataRetriever()
		{
			return new PutUnblockFacilityAdditionalDataRetriever(getAdditionalDataHolder());
		}
	}
	
	@Test
	public void resetUserNameForCodeL() {
		UserProfile userProfile = new UserProfile();
		
		userProfile.setUsername("sit001");
		userProfile.setName("SIT DCP01");
		userProfile.setIdNo("42");
        when(userProfileRepositoryMock.getUserProfile(Mockito.anyInt())).thenReturn(userProfile);
        when(userProfileRepositoryMock.updateFailedLoginCount(Mockito.anyInt())).thenReturn(1);
		        
        BoData response = (BoData) resetUserNameService.resetUserName("2", "L");
        ResetUserNameResponse res = (ResetUserNameResponse) response;
		assertEquals("Username Successfully Reset", res.getStatusTitle());
		assertEquals("User can login to DCP now", res.getStatusDesc());
		
	}
	
	@Test
	public void resetUserNameForCodeC() {
		UserProfile userProfile = new UserProfile();
		
		userProfile.setUsername("sit001");
		userProfile.setName("SIT DCP01");
		userProfile.setIdNo("42");
        when(userProfileRepositoryMock.getUserProfile(Mockito.anyInt())).thenReturn(userProfile);
        when(userProfileRepositoryMock.updateFailedChallengeCount(Mockito.anyInt())).thenReturn(1);
		
        BoData response = (BoData) resetUserNameService.resetUserName("2", "C");
        ResetUserNameResponse res = (ResetUserNameResponse) response;
		assertEquals("Username Successfully Reset", res.getStatusTitle());
		assertEquals("User can login to DCP now", res.getStatusDesc());
		
	}
	
	@Test
	public void resetUserNameForCodeTPIN() {
		UserProfile userProfile = new UserProfile();
		
		userProfile.setUsername("sit001");
		userProfile.setName("SIT DCP01");
		userProfile.setIdNo("42");
        when(userProfileRepositoryMock.getUserProfile(Mockito.anyInt())).thenReturn(userProfile);
        when(cardProfileRepositoryMock.updateFailedCardTpinCount(Mockito.anyInt())).thenReturn(1);
		
        BoData response = (BoData) resetUserNameService.resetUserName("2", "TPIN");
        ResetUserNameResponse res = (ResetUserNameResponse) response;
		assertEquals("Card T-PIN Counter Successfully Reset", res.getStatusTitle());
		assertEquals("User can continue to set their card PIN now", res.getStatusDesc());
		
	}

}
