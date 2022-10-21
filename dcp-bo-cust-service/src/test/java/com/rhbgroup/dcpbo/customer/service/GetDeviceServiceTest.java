package com.rhbgroup.dcpbo.customer.service;

import com.rhbgroup.dcpbo.customer.audit.collector.AdditionalDataHolder;
import com.rhbgroup.dcpbo.customer.model.DeviceProfile;
import com.rhbgroup.dcpbo.customer.model.UserProfile;
import com.rhbgroup.dcpbo.customer.repository.DeviceProfileRepository;
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

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest(classes = { GetDevicesService.class, GetDeviceServiceTest.Config.class })
public class GetDeviceServiceTest {

    @Autowired
    GetDevicesService getDevicesService;

    @MockBean
    UserProfileRepository userProfileRepository;

    @MockBean
    DeviceProfileRepository deviceProfileRepositoryMock;
    
    @TestConfiguration
	static class Config {

		@Bean
		@Primary
		public GetDevicesService getGetDevicesService() {
			return new GetDevicesService();
		}
		
		@Bean
		public AdditionalDataHolder getAdditionalDataHolder()
		{
			return new AdditionalDataHolder();
		}
	}
	
    @Test
    public void getDeviceServiceTestSuccess(){
        DeviceProfile deviceProfile = new DeviceProfile();
        deviceProfile.setDeviceId("aeiou");
        deviceProfile.setId(1);
        List<DeviceProfile> deviceProfileList = new ArrayList<>();
        deviceProfileList.add(deviceProfile);
        UserProfile userProfile = new UserProfile();
        userProfile.setTxnSigningDevice(1);

        when(deviceProfileRepositoryMock.findDeviceByUserIdAndActiveDevice(7)).thenReturn(deviceProfileList);
        when(userProfileRepository.findByCustomerId(7)).thenReturn(userProfile);

        assertEquals("true",getDevicesService.retrieveDevice("7").get(0).getPrimaryDevice());
    }
    
    @Test
    public void getDeviceServiceTestIsPrimary(){
        DeviceProfile deviceProfile = new DeviceProfile();
        deviceProfile.setDeviceId("A507CE1F-37D1-4ED3-9A12-E417787EE3F4");
        deviceProfile.setId(70072);
        
        UserProfile userProfile = new UserProfile();
        userProfile.setTxnSigningDevice(70072);
        
        Integer mainDeviceId = userProfile.getTxnSigningDevice();
        Boolean primaryDevice = false;
        if (deviceProfile.getId().equals(mainDeviceId))
            primaryDevice = true;
        
        List<DeviceProfile> deviceProfileList = new ArrayList<>();
        deviceProfileList.add(deviceProfile);

        assertEquals("true", primaryDevice.toString());
    }

}