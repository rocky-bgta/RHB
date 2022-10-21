package com.rhbgroup.dcpbo.user.workflow.function.device;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Timestamp;
import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.rhbgroup.dcpbo.common.exception.CommonException;
import com.rhbgroup.dcpbo.user.common.ApprovalDeviceRepository;
import com.rhbgroup.dcpbo.user.common.ApprovalRepository;
import com.rhbgroup.dcpbo.user.common.DeviceProfileRepository;
import com.rhbgroup.dcpbo.user.common.UserProfileRepository;
import com.rhbgroup.dcpbo.user.common.UserRepository;
import com.rhbgroup.dcpbo.user.common.model.bo.Approval;
import com.rhbgroup.dcpbo.user.common.model.bo.ApprovalDevice;
import com.rhbgroup.dcpbo.user.common.model.dcp.DeviceProfile;
import com.rhbgroup.dcpbo.user.common.model.dcp.UserProfile;
import com.rhbgroup.dcpbo.user.info.model.bo.User;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {WorkflowFunctionDeviceApprovalServiceImpl.class, WorkflowFunctionDeviceApprovalServiceImplTests.class})
public class WorkflowFunctionDeviceApprovalServiceImplTests {

	@Autowired
	WorkflowFunctionDeviceApprovalService workflowFunctionDeviceApprovalService;

	@MockBean
	ApprovalRepository approvalRepositoryMock;

	@MockBean
	ApprovalDeviceRepository approvalDeviceRepositoryMock;

	@MockBean
	UserRepository userRepositoryMock;

	@MockBean
	DeviceProfileRepository deviceProfileRepositoryMock;

	@MockBean
	UserProfileRepository userProfileRepositoryMock;
	
	private static Logger logger = LogManager.getLogger(WorkflowFunctionDeviceApprovalServiceImplTests.class);

	int approvalId = 1;
	int creatorId = 123;
	int customerId = 456;
	int deviceProfileId = 1;
	int requestUserId = 123;
	
	@Test
	public void getDeviceApprovalTest() throws Throwable {
		logger.debug("getDeviceApprovalTest()");
		logger.debug("    approvalRepositoryMock: " + approvalRepositoryMock);
		logger.debug("    approvalDeviceRepositoryMock: " + approvalDeviceRepositoryMock);
		logger.debug("    userRepositoryMock: " + userRepositoryMock);
		logger.debug("    deviceProfileRepositoryMock: " + deviceProfileRepositoryMock);
		logger.debug("    userProfileRepositoryMock: " + userProfileRepositoryMock);

		Approval approval = createApproval();
		when(approvalRepositoryMock.getOne(Mockito.anyInt())).thenReturn(approval);
		
		User creator = createCreator();
		when(userRepositoryMock.getOne(Mockito.anyInt())).thenReturn(creator);

		ApprovalDevice approvalDevice = createApprovalDevice();
		when(approvalDeviceRepositoryMock.findByApprovalId(Mockito.anyInt())).thenReturn(approvalDevice);
		
		DeviceProfile deviceProfile = createDeviceProfile();
		when(deviceProfileRepositoryMock.findById(Mockito.anyInt())).thenReturn(deviceProfile);
		
		UserProfile userProfile = createUserProfile();
		when(userProfileRepositoryMock.findOne(Mockito.anyInt())).thenReturn(userProfile);

		WorkflowFunctionDeviceApproval workflowFunctionDeviceApproval = (WorkflowFunctionDeviceApproval) workflowFunctionDeviceApprovalService
				.getDeviceApproval(approvalId, requestUserId);
		logger.debug("    workflowFunctionDeviceApproval: " + workflowFunctionDeviceApproval);
		assertNotNull(workflowFunctionDeviceApproval);
		
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
		logger.debug("                                  : " + objectMapper.writeValueAsString(workflowFunctionDeviceApproval));
	}

	@Test(expected = CommonException.class)
	public void getDeviceApprovalTest_approvalNotFound() throws Throwable {
		logger.debug("getDeviceApprovalTest_approvalNotFound()");
		logger.debug("    approvalRepositoryMock: " + approvalRepositoryMock);

		when(approvalRepositoryMock.getOne(Mockito.anyInt())).thenReturn(null);

		workflowFunctionDeviceApprovalService.getDeviceApproval(approvalId, requestUserId);
	}
	
	@Test(expected = CommonException.class)
	public void getDeviceApprovalTest_creatorNotFound() throws Throwable {
		logger.debug("getDeviceApprovalTest_creatorNotFound()");
		logger.debug("    approvalRepositoryMock: " + approvalRepositoryMock);
		logger.debug("    userRepositoryMock: " + userRepositoryMock);

		when(approvalRepositoryMock.getOne(Mockito.anyInt())).thenReturn(createApproval());
		when(userRepositoryMock.getOne(Mockito.anyInt())).thenReturn(null);

		workflowFunctionDeviceApprovalService.getDeviceApproval(approvalId, requestUserId);
	}
	
	@Test(expected = CommonException.class)
	public void getDeviceApprovalTest_approvalDeviceNotFound() throws Throwable {
		logger.debug("getDeviceApprovalTest_approvalDeviceNotFound()");
		logger.debug("    approvalRepositoryMock: " + approvalRepositoryMock);
		logger.debug("    userRepositoryMock: " + userRepositoryMock);
		logger.debug("    approvalDeviceRepositoryMock: " + approvalDeviceRepositoryMock);

		when(approvalRepositoryMock.getOne(Mockito.anyInt())).thenReturn(createApproval());
		when(userRepositoryMock.getOne(Mockito.anyInt())).thenReturn(createCreator());
		when(approvalDeviceRepositoryMock.findByApprovalId(Mockito.anyInt())).thenReturn(null);

		workflowFunctionDeviceApprovalService.getDeviceApproval(approvalId, requestUserId);
	}
	
	@Test(expected = CommonException.class)
	public void getDeviceApprovalTest_invalidPayload() throws Throwable {
		logger.debug("getDeviceApprovalTest_invalidPayload()");
		logger.debug("    approvalRepositoryMock: " + approvalRepositoryMock);
		logger.debug("    userRepositoryMock: " + userRepositoryMock);
		logger.debug("    approvalDeviceRepositoryMock: " + approvalDeviceRepositoryMock);

		when(approvalRepositoryMock.getOne(Mockito.anyInt())).thenReturn(createApproval());
		when(userRepositoryMock.getOne(Mockito.anyInt())).thenReturn(createCreator());
		
		ApprovalDevice approvalDevice = createApprovalDevice();
		approvalDevice.setPayload("abc123");
		when(approvalDeviceRepositoryMock.findByApprovalId(Mockito.anyInt())).thenReturn(approvalDevice);

		workflowFunctionDeviceApprovalService.getDeviceApproval(approvalId, requestUserId);
	}
	
	@Test(expected = CommonException.class)
	public void getDeviceApprovalTest_deviceProfileNotFound() throws Throwable {
		logger.debug("getDeviceApprovalTest_deviceProfileNotFound()");
		logger.debug("    approvalRepositoryMock: " + approvalRepositoryMock);
		logger.debug("    userRepositoryMock: " + userRepositoryMock);
		logger.debug("    approvalDeviceRepositoryMock: " + approvalDeviceRepositoryMock);
		logger.debug("    deviceProfileRepositoryMock: " + deviceProfileRepositoryMock);

		when(approvalRepositoryMock.getOne(Mockito.anyInt())).thenReturn(createApproval());
		when(userRepositoryMock.getOne(Mockito.anyInt())).thenReturn(createCreator());
		when(approvalDeviceRepositoryMock.findByApprovalId(Mockito.anyInt())).thenReturn(createApprovalDevice());
		when(deviceProfileRepositoryMock.findByDeviceId(Mockito.anyString())).thenReturn(null);

		workflowFunctionDeviceApprovalService.getDeviceApproval(approvalId, requestUserId);
	}
	
	@Test(expected = CommonException.class)
	public void getDeviceApprovalTest_userProfileNotFound() throws Throwable {
		logger.debug("getDeviceApprovalTest_userProfileNotFound()");
		logger.debug("    approvalRepositoryMock: " + approvalRepositoryMock);
		logger.debug("    userRepositoryMock: " + userRepositoryMock);
		logger.debug("    approvalDeviceRepositoryMock: " + approvalDeviceRepositoryMock);
		logger.debug("    deviceProfileRepositoryMock: " + deviceProfileRepositoryMock);
		logger.debug("    userProfileRepositoryMock: " + userProfileRepositoryMock);

		when(approvalRepositoryMock.getOne(Mockito.anyInt())).thenReturn(createApproval());
		when(userRepositoryMock.getOne(Mockito.anyInt())).thenReturn(createCreator());
		when(approvalDeviceRepositoryMock.findByApprovalId(Mockito.anyInt())).thenReturn(createApprovalDevice());
		when(deviceProfileRepositoryMock.findByDeviceId(Mockito.anyString())).thenReturn(createDeviceProfile());
		when(userProfileRepositoryMock.findOne(Mockito.anyInt())).thenReturn(null);

		workflowFunctionDeviceApprovalService.getDeviceApproval(approvalId, requestUserId);
	}
	
	@Test
	public void getDeviceApprovalTest_notPrimaryDevice() throws Throwable {
		logger.debug("getDeviceApprovalTest_notPrimaryDevice()");
		logger.debug("    approvalRepositoryMock: " + approvalRepositoryMock);
		logger.debug("    userRepositoryMock: " + userRepositoryMock);
		logger.debug("    approvalDeviceRepositoryMock: " + approvalDeviceRepositoryMock);
		logger.debug("    deviceProfileRepositoryMock: " + deviceProfileRepositoryMock);
		logger.debug("    userProfileRepositoryMock: " + userProfileRepositoryMock);

		when(approvalRepositoryMock.getOne(Mockito.anyInt())).thenReturn(createApproval());
		when(userRepositoryMock.getOne(Mockito.anyInt())).thenReturn(createCreator());
		when(approvalDeviceRepositoryMock.findByApprovalId(Mockito.anyInt())).thenReturn(createApprovalDevice());
		when(deviceProfileRepositoryMock.findById(Mockito.anyInt())).thenReturn(createDeviceProfile());
		
		UserProfile userProfile = createUserProfile();
		userProfile.setTxnSigningDevice(12345);
		when(userProfileRepositoryMock.findOne(Mockito.anyInt())).thenReturn(userProfile);

		WorkflowFunctionDeviceApproval workflowFunctionDeviceApproval = (WorkflowFunctionDeviceApproval) workflowFunctionDeviceApprovalService
				.getDeviceApproval(approvalId, requestUserId);
		logger.debug("    workflowFunctionDeviceApproval: " + workflowFunctionDeviceApproval);
		assertNotNull(workflowFunctionDeviceApproval);
		
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
		logger.debug("                                  : " + objectMapper.writeValueAsString(workflowFunctionDeviceApproval));
	}
	
	@Test
	public void getDeviceApprovalTest_notDeleteActionType() throws Throwable {
		logger.debug("getDeviceApprovalTest_notDeleteActionType()");
		logger.debug("    approvalRepositoryMock: " + approvalRepositoryMock);
		logger.debug("    userRepositoryMock: " + userRepositoryMock);
		logger.debug("    approvalDeviceRepositoryMock: " + approvalDeviceRepositoryMock);
		logger.debug("    deviceProfileRepositoryMock: " + deviceProfileRepositoryMock);
		logger.debug("    userProfileRepositoryMock: " + userProfileRepositoryMock);

		Approval approval = createApproval();
		approval.setActionType("CREATE");
		when(approvalRepositoryMock.getOne(Mockito.anyInt())).thenReturn(approval);

		when(userRepositoryMock.getOne(Mockito.anyInt())).thenReturn(createCreator());
		when(approvalDeviceRepositoryMock.findByApprovalId(Mockito.anyInt())).thenReturn(createApprovalDevice());

		WorkflowFunctionDeviceApproval workflowFunctionDeviceApproval = (WorkflowFunctionDeviceApproval) workflowFunctionDeviceApprovalService
				.getDeviceApproval(approvalId, requestUserId);
		logger.debug("    workflowFunctionDeviceApproval: " + workflowFunctionDeviceApproval);
		assertNotNull(workflowFunctionDeviceApproval);
		
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
		logger.debug("                                  : " + objectMapper.writeValueAsString(workflowFunctionDeviceApproval));
	}
	
	private Approval createApproval() {
		Approval approval = new Approval();
		approval.setId(approvalId);
		approval.setActionType("Delete");
		approval.setCreatedTime(new Timestamp(System.currentTimeMillis()));
		approval.setReason("Not sure of the reason, but that's just how it is.");
		approval.setCreatorId(creatorId);
		
		return approval;
	}
	
	private User createCreator() {
		User creator = new User();
		creator.setId(creatorId);
		creator.setName("Mohd Ikhwan Haris");
		
		return creator;
	}
	
	private ApprovalDevice createApprovalDevice() throws IOException {
		ApprovalDevice approvalDevice = new ApprovalDevice();
		approvalDevice.setId(deviceProfileId);
		approvalDevice.setApprovalId(approvalId);
		approvalDevice.setState(WorkflowFunctionDeviceApprovalServiceImpl.STATE_BEFORE);
		approvalDevice.setPayload(loadFileAsString("WorkflowFunctionDeviceApprovalPayload.json"));
		
		return approvalDevice;
		
	}
	
	private DeviceProfile createDeviceProfile() {
		DeviceProfile deviceProfile = new DeviceProfile();
		deviceProfile.setId(deviceProfileId);
		deviceProfile.setUserId(customerId);
		deviceProfile.setDeviceName("Samsung Galaxy S7");
		deviceProfile.setOs("Android 8.0");
		deviceProfile.setLastLogin(new Date(System.currentTimeMillis()));
		deviceProfile.setCreatedTime(new Date(System.currentTimeMillis() - 86400000));
		
		return deviceProfile;
	}
	
	private UserProfile createUserProfile() {
		UserProfile userProfile = new UserProfile();
		userProfile.setId(customerId);
		userProfile.setTxnSigningDevice(deviceProfileId);
		
		return userProfile;
	}

	private String loadFileAsString(String filename) throws IOException {
		logger.debug("loadFileAsString()");

		InputStream is = getClass().getClassLoader().getResourceAsStream(filename);
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		StringBuilder sbld = new StringBuilder();
		String line = null;
		while ((line = br.readLine()) != null)
			sbld.append(line);
		br.close();
		is.close();
		
		String contents = sbld.toString();
		logger.debug("    loaded file: " + filename + ": " + contents);

		return contents;
	}

}
