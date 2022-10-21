package com.rhbgroup.dcpbo.user.workflow.device;

import com.rhbgroup.dcpbo.common.audit.AdditionalDataHolder;
import com.rhbgroup.dcpbo.common.exception.CommonException;
import com.rhbgroup.dcpbo.user.common.*;
import com.rhbgroup.dcpbo.user.common.model.bo.BoApprovalDevice;
import com.rhbgroup.dcpbo.user.common.model.dcp.DeviceProfile;
import com.rhbgroup.dcpbo.user.common.model.dcp.UserProfile;
import com.rhbgroup.dcpbo.user.info.model.bo.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
        WorkflowDeviceServiceTest.class,
        WorkflowDeviceService.class
})
public class WorkflowDeviceServiceTest {

    @MockBean
    BoUserApprovalRepo boUserApprovalRepo;

    @Autowired
    WorkflowDeviceService workflowDeviceService;

    @MockBean
    BoApprovalDeviceRepository boApprovalDeviceRepository;

    @MockBean
    DeviceProfileRepository deviceProfileRepository;

    @MockBean
    AdditionalDataHolder additionalDataHolder;

    @MockBean
    UserProfileRepository userProfileRepository;

    @MockBean
    KonySubscriptionService konySubscriptionService;

    @MockBean
    UserRepository userRepository;

    @Test
    public void approveDeletionSuccess(){

        Integer approvalId = 1;
        String payload = "{\"os\":\"Android 8.0\",\"customerId\":\"1021\",\"name\":\"boogie123\",\"deviceId\":2,\"username\":\"muhammad.ali\"}";

        BoApprovalDevice boApprovalDevice = new BoApprovalDevice();
        boApprovalDevice.setPayload(payload);

        when(boApprovalDeviceRepository.findByApprovalId(approvalId)).thenReturn(boApprovalDevice);

        DeviceProfile deviceProfile = new DeviceProfile();
        deviceProfile.setSubscriberId("123");
        deviceProfile.setUserId(123);

        UserProfile userProfile = new UserProfile();
        userProfile.setCisNo("123");

        User user = new User();
        user.setId(456);
        user.setUsername("testUser");

        when(deviceProfileRepository.findOne(anyInt())).thenReturn(deviceProfile);
        when(userProfileRepository.findByCustomerId(any())).thenReturn(userProfile);
        when(userRepository.findById(456)).thenReturn(user);

        WorkflowDeviceResponse workflowDeviceResponse = workflowDeviceService.approveDeletion("Testing deletion approval", approvalId, 456);
        assertEquals(new Integer(approvalId), workflowDeviceResponse.getApprovalId());
    }

    @Test(expected = CommonException.class)
    public void approveDeletionFail(){

        Integer approvalId = 1;

        User user = new User();
        user.setId(456);
        user.setUsername("testUser");

        when(userRepository.findById(456)).thenReturn(user);

        BoApprovalDevice boApprovalDevice = new BoApprovalDevice();
        boApprovalDevice.setPayload(null);

        when(boApprovalDeviceRepository.findByApprovalId(approvalId)).thenReturn(boApprovalDevice);

        WorkflowDeviceResponse workflowDeviceResponse = workflowDeviceService.approveDeletion("Testing deletion approval", approvalId, 456);
    }
}
