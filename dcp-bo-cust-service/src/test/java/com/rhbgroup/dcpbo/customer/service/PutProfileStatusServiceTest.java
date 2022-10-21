package com.rhbgroup.dcpbo.customer.service;

import com.rhbgroup.dcpbo.customer.audit.collector.AdditionalDataHolder;
import com.rhbgroup.dcpbo.customer.audit.collector.BoException;
import com.rhbgroup.dcpbo.customer.model.UserProfile;
import com.rhbgroup.dcpbo.customer.repository.UserProfileRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest(classes = { PutProfileStatusService.class, PutProfileStatusServiceTest.Config.class })
public class PutProfileStatusServiceTest {

    @Autowired
    PutProfileStatusService putProfileStatusService;

    @MockBean
    UserProfileRepository userProfileRepository;
    
    @TestConfiguration
   	static class Config {

   		@Bean
   		@Primary
   		public PutProfileStatusService getPutProfileStatusService() {
   			return new PutProfileStatusService();
   		}

   		@Bean
   		public AdditionalDataHolder getAdditionalDataHolder()
   		{
   			return new AdditionalDataHolder();
   		}
   	}
    
    @Test
    public void putProfileStatusServiceSuccess(){
        UserProfile userProfile = new UserProfile();

        when(userProfileRepository.updateProfileStatus(1,"A")).thenReturn(1);
        when(userProfileRepository.findByCustomerId(1)).thenReturn(userProfile);

        assertEquals("A",putProfileStatusService.writeProfileStatus("1").getUserStatus());
    }

    @Test(expected = BoException.class)
    public void putProfileStatusServiceFail(){
        UserProfile userProfile = new UserProfile();

        when(userProfileRepository.updateProfileStatus(2,"A")).thenReturn(1);
        when(userProfileRepository.findByCustomerId(1)).thenReturn(userProfile);

        assertEquals("A",putProfileStatusService.writeProfileStatus("1").getUserStatus());
    }

}