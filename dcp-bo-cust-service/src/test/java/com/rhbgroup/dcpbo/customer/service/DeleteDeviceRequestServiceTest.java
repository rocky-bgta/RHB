package com.rhbgroup.dcpbo.customer.service;

import com.rhbgroup.dcpbo.customer.dcpbo.BoApproval;
import com.rhbgroup.dcpbo.customer.dcpbo.BoApprovalDevice;
import com.rhbgroup.dcpbo.customer.dcpbo.BoConfigFunction;
import com.rhbgroup.dcpbo.customer.dcpbo.BoUser;
import com.rhbgroup.dcpbo.customer.exception.CommonException;
import com.rhbgroup.dcpbo.customer.model.DelApprovalRequest;
import com.rhbgroup.dcpbo.customer.model.DelApprovalResponse;
import com.rhbgroup.dcpbo.customer.model.DeviceProfile;
import com.rhbgroup.dcpbo.customer.model.UserProfile;
import com.rhbgroup.dcpbo.customer.repository.*;

import org.junit.Before;
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

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.when;
import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest(classes = { DeleteDeviceRequestService.class, DeleteDeviceRequestServiceTest.Config.class })
public class DeleteDeviceRequestServiceTest {

    @Autowired
    DeleteDeviceRequestService deleteDeviceRequestService;

    @MockBean
    BoConfigFunctionRepository boConfigFunctionRepository;

    @MockBean
    BoApprovalRepository boApprovalRepository;

    @MockBean
    BoApprovalDeviceRepository boApprovalDeviceRepository;

    @MockBean
    BoUserRepository boUserRepository;

    @MockBean
    BoUsergroupAccessRepository boUsergroupAccessRepository;

    @MockBean
    BoUserUsergroupRepository boUserUsergroupRepository;

    @MockBean
    DeviceProfileRepository deviceProfileRepository;

    @MockBean
    UserProfileRepository userProfileRepository;

    @MockBean
    KonySubscriptionService konySubscriptionService;

    private DelApprovalRequest delApprovalRequest;

    @TestConfiguration
	static class Config {

		@Bean
		@Primary
		public DeleteDeviceRequestService getDeleteDeviceRequestService() {
			return new DeleteDeviceRequestService();
		}

	}
    
    @Before
    public void setup(){

        delApprovalRequest = new DelApprovalRequest();

        delApprovalRequest.setCustomerId("123");
        delApprovalRequest.setUsername("Username");
        delApprovalRequest.setDeviceId(123);
        delApprovalRequest.setName("Name");
        delApprovalRequest.setOs("OS");
        delApprovalRequest.setFunctionId(678);
    }

    @Test
    public void deleteDeviceRequestApproval() throws Exception{

        Integer functionId = delApprovalRequest.getFunctionId();
        Integer deviceId = delApprovalRequest.getDeviceId();

        BoConfigFunction boConfigFunction = new BoConfigFunction();
        boConfigFunction.setApprovalRequired(true);
        when(boConfigFunctionRepository.findByFunctionId(functionId)).thenReturn(boConfigFunction);

        List<Integer> approvalIdList = new ArrayList<>();
        approvalIdList.add(0);
        approvalIdList.add(1);
        approvalIdList.add(2);
        when(boApprovalRepository.findPendingRequest(functionId)).thenReturn(approvalIdList);

        when(boApprovalDeviceRepository.findApprovalIdByApprovalList(anyListOf(Integer.TYPE), eq(deviceId.toString()))).thenReturn(null);

        BoUser boUser = new BoUser();
        boUser.setUsername("Username");
        when(boUserRepository.findOne(123)).thenReturn(boUser);

        BoApproval boApproval = new BoApproval();
        boApproval.setId(123);
        when(boApprovalRepository.save(any(BoApproval.class))).thenReturn(boApproval);

        UserProfile userProfile = new UserProfile();
        userProfile.setCisNo("123");
        userProfile.setIdType("NRIC");
        userProfile.setIdNo("123");
        when(userProfileRepository.findByCustomerId(any())).thenReturn(userProfile);

        DelApprovalResponse delApprovalResponse = deleteDeviceRequestService.deleteDevice(deviceId, 123, delApprovalRequest);
        assertEquals(delApprovalResponse.getApprovalId(), new Integer(123));
        assertEquals(delApprovalResponse.getIsWritten(), "N");
    }

    @Test
    public void deleteDeviceRequestNoApproval() throws Exception{
        Integer functionId = delApprovalRequest.getFunctionId();
        Integer deviceId = delApprovalRequest.getDeviceId();

        BoConfigFunction boConfigFunction = new BoConfigFunction();
        boConfigFunction.setApprovalRequired(false);
        when(boConfigFunctionRepository.findByFunctionId(functionId)).thenReturn(boConfigFunction);

        DeviceProfile deviceProfile = new DeviceProfile();
        deviceProfile.setSubscriberId("123");
        deviceProfile.setUserId(123);

        UserProfile userProfile = new UserProfile();
        userProfile.setCisNo("123");
        userProfile.setIdType("NRIC");
        userProfile.setIdNo("123");

        when(deviceProfileRepository.findOne(anyInt())).thenReturn(deviceProfile);
        when(userProfileRepository.findByCustomerId(any())).thenReturn(userProfile);

        DelApprovalResponse delApprovalResponse = deleteDeviceRequestService.deleteDevice(deviceId, 123, delApprovalRequest);
        assertEquals(delApprovalResponse.getApprovalId(), new Integer(0));
        assertEquals(delApprovalResponse.getIsWritten(), "Y");
    }

    @Test(expected = CommonException.class)
    public void deleteDeviceRequestFail() throws Exception{
        Integer functionId = delApprovalRequest.getFunctionId();
        Integer deviceId = delApprovalRequest.getDeviceId();

        BoConfigFunction boConfigFunction = new BoConfigFunction();
        boConfigFunction.setApprovalRequired(true);
        when(boConfigFunctionRepository.findByFunctionId(functionId)).thenReturn(boConfigFunction);

        List<Integer> approvalIdList = new ArrayList<>();
        approvalIdList.add(0);
        approvalIdList.add(1);
        approvalIdList.add(2);
        when(boApprovalRepository.findPendingRequest(functionId)).thenReturn(approvalIdList);

        BoApprovalDevice boApprovalDevice = new BoApprovalDevice();
        boApprovalDevice.setId(123);
        when(boApprovalDeviceRepository.findApprovalIdByApprovalList(anyListOf(Integer.TYPE), eq(deviceId.toString()))).thenReturn(boApprovalDevice);

        deleteDeviceRequestService.deleteDevice(deviceId, 123, delApprovalRequest);
    }
}

