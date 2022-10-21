package com.rhbgroup.dcpbo.customer.service;

import com.rhbgroup.dcpbo.customer.audit.collector.AdditionalDataHolder;
import com.rhbgroup.dcpbo.customer.exception.CommonException;
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

import java.util.Date;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest(classes = { DeleteDeviceService.class, DeleteDeviceServiceTest.Config.class })
public class DeleteDeviceServiceTest {

    @Autowired
    DeleteDeviceService deleteDeviceService;

    @MockBean
    DeviceProfileRepository deviceProfileRepositoryMock;

    @MockBean
    UserProfileRepository userProfileRepositoryMock;

    @MockBean
    KonySubscriptionService konySubscriptionService;

    @TestConfiguration
	static class Config {

		@Bean
		@Primary
		public DeleteDeviceService getDeleteDeviceService() {
			return new DeleteDeviceService();
		}
		
		@Bean
		public AdditionalDataHolder getAdditionalDataHolder()
		{
			return new AdditionalDataHolder();
		}
	}
	
    @Test
    public void deleteDeviceSuccess() throws Exception{
        DeviceProfile testDeviceProfile = new DeviceProfile();
        Date now = new Date();

        testDeviceProfile.setId(1);
        testDeviceProfile.setUserId(123001);
        testDeviceProfile.setDeviceId("testId");
        testDeviceProfile.setDeviceName("Samsung S8");
        testDeviceProfile.setOs("Android 8.0");
        testDeviceProfile.setQuickLoginRefreshToken("eyJhbGciOiJIUzI1NiIsInR5cGUiOiJKV1QifQ");
        testDeviceProfile.setPushNotificationSubscriptionToken("eyJhbGciOiJIUzI1NiIsInR5cGUiOiJKV1QifQ");
        testDeviceProfile.setPushNotificationPlatform("ANDROID");
        testDeviceProfile.setLastLogin(now);
        testDeviceProfile.setCreatedTime(now);
        testDeviceProfile.setIsQuickLoginBioEnabled(false);
        testDeviceProfile.setSubscriberId("1408953838000");
        testDeviceProfile.setSecurePlusSequenceNo(123123);
        testDeviceProfile.setSecurePlusSetup(false);

        UserProfile testUserProfile = new UserProfile();

        testUserProfile.setId(123001);
        testUserProfile.setUsername("TEST USERNAME");
        testUserProfile.setTxnSigningDevice(1);

        when(deviceProfileRepositoryMock.findOne(1)).thenReturn(testDeviceProfile);
        when(deviceProfileRepositoryMock.updateDeviceStatus("INACTIVE", 1)).thenReturn(1);
        when(userProfileRepositoryMock.nullifyTxnSigningDevice(123001, 1)).thenReturn(1);
        when(userProfileRepositoryMock.findByCustomerId(123001)).thenReturn(testUserProfile);

        assertTrue(deleteDeviceService.deleteDevice(1));
    }

    @Test(expected = CommonException.class)
    public void deleteDeviceFail() throws Exception{

        DeviceProfile deviceProfile = new DeviceProfile();
        deviceProfile.setSubscriberId("123");
        deviceProfile.setUserId(123);

        UserProfile userProfile = new UserProfile();
        userProfile.setCisNo("123");

        when(deviceProfileRepositoryMock.findOne(anyInt())).thenReturn(deviceProfile);
        when(userProfileRepositoryMock.findByCustomerId(any())).thenReturn(userProfile);
        when(deviceProfileRepositoryMock.updateDeviceStatus("INACTIVE", 321)).thenReturn(0);

        deleteDeviceService.deleteDevice(321);
    }
}