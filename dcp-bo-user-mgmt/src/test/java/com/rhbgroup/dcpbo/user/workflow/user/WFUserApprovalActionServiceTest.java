package com.rhbgroup.dcpbo.user.workflow.user;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.junit4.SpringRunner;

import com.rhbgroup.dcp.util.JsonUtil;
import com.rhbgroup.dcpbo.common.audit.AdditionalDataHolder;
import com.rhbgroup.dcpbo.common.exception.CommonException;
import com.rhbgroup.dcpbo.user.common.model.bo.Approval;
import com.rhbgroup.dcpbo.user.common.ApprovalRepository;
import com.rhbgroup.dcpbo.user.common.BoConfigDepartmentRepo;
import com.rhbgroup.dcpbo.user.common.BoData;
import com.rhbgroup.dcpbo.user.common.model.bo.BoUmApprovalUser;
import com.rhbgroup.dcpbo.user.common.BoUmApprovalUserRepo;
import com.rhbgroup.dcpbo.user.common.UserGroupRepository;
import com.rhbgroup.dcpbo.user.common.UserRepository;
import com.rhbgroup.dcpbo.user.common.UserUsergroupRepository;
import com.rhbgroup.dcpbo.user.enums.MaintenanceActionType;
import com.rhbgroup.dcpbo.user.common.model.bo.UserUsergroup;
import com.rhbgroup.dcpbo.user.info.model.bo.User;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { WFUserApprovalService.class, WFUserApprovalActionServiceTest.Config.class })
public class WFUserApprovalActionServiceTest {

	@Autowired
	WFUserApprovalService wfUserApprovalService;

	@MockBean
	ApprovalRepository approvalRepositoryMock;

	@MockBean
	UserRepository userRepositoryMock;

	@MockBean
	BoUmApprovalUserRepo boUmApprovalUserRepoMock;

	@MockBean
	UserUsergroupRepository userUserGroupRepoMock;

	@MockBean
    UserGroupRepository userGroupRepoMock;
	
	@MockBean
	BoConfigDepartmentRepo boConfigDepartmentRepoMock;

	@TestConfiguration
	static class Config {
		@Bean
		@Primary
		public WFUserApprovalService getWFUserApprovalService() {
			return new WFUserApprovalServiceImpl();
		}
		
		@Bean
		@Primary
		public AdditionalDataHolder getAdditionalDataHolder()
		{
			return new AdditionalDataHolder();
		}
	}

	@Test
	public void approveCreateSuccess() throws Exception {
		Timestamp ts = new Timestamp(new Date().getTime());
		Approval approval = new Approval();
		approval.setCreatorId(1);
		approval.setId(1);
		approval.setActionType(MaintenanceActionType.ADD.getValue());
		approval.setReason("reason");
		approval.setUpdatedTime(ts);
		List<BoUmApprovalUser> userManagementApprovalUserList = new ArrayList<BoUmApprovalUser>();
		BoUmApprovalUser userManagementApprovalUser = new BoUmApprovalUser();
		userManagementApprovalUser.setId(1);
		userManagementApprovalUser.setApprovalId(1);
		userManagementApprovalUser.setState("P");
		userManagementApprovalUser.setUpdatedTime(ts);
		userManagementApprovalUser.setPayload("{ " + 
				"    \"functionId\": 1," + 
				"    \"username\": \"123456\", " + 
				"    \"email\": \"nesh@rhbgroup.com\"," + 
				"    \"name\": \"Sarjen\", " + 
				"    \"status\": \"A\", " + 
				"    \"department\": { " + 
				"        \"departmentId\": 1," + 
				"        \"departmentName\": \"BO Admin\" " + 
				"        }, " + 
				"    \"group\": [{" + 
				"        \"groupId\": 1, " + 
				"        \"groupName\": \"Group Numero Uno\" " + 
				"        },{" + 
				"        \"groupId\": 2, " + 
				"        \"groupName\": \"Group Numero Duo\" " + 
				"        }" + 
				"    ] " + 
				"}");
		userManagementApprovalUserList.add(userManagementApprovalUser);

		when(approvalRepositoryMock.findOne(1)).thenReturn(approval);
		when(userRepositoryMock.findByUsername("123456")).thenReturn(null);
		
		User user = new User();
		user.setUsername("user1");
		user.setId(1);
		when(userRepositoryMock.findOne(1)).thenReturn(user);
		when(boUmApprovalUserRepoMock.findAllByApprovalId(1)).thenReturn(userManagementApprovalUserList);

		WFUserApprovalActionDetail wfUserApprovalActionDetail = new WFUserApprovalActionDetail();
		wfUserApprovalActionDetail.setApprovalId(1);
		wfUserApprovalActionDetail.setReason("approveCreateSuccess");

		BoData boData = wfUserApprovalService.approveCreate(1, wfUserApprovalActionDetail);
		wfUserApprovalActionDetail = (WFUserApprovalActionDetail) boData;
		System.out.println("approveCreateSuccess result : " + JsonUtil.objectToJson(wfUserApprovalActionDetail));

		assertEquals((Integer) 1, wfUserApprovalActionDetail.getApprovalId());
	}

	@Test(expected = CommonException.class)
	public void duplicateRecordCheck() throws Exception {
		Timestamp ts = new Timestamp(new Date().getTime());
		Approval approval = new Approval();
		approval.setCreatorId(1);
		approval.setId(1);
		approval.setActionType(MaintenanceActionType.ADD.getValue());
		approval.setReason("reason");
		approval.setUpdatedTime(ts);
		List<BoUmApprovalUser> userManagementApprovalUserList = new ArrayList<BoUmApprovalUser>();
		BoUmApprovalUser userManagementApprovalUser = new BoUmApprovalUser();
		userManagementApprovalUser.setId(1);
		userManagementApprovalUser.setApprovalId(1);
		userManagementApprovalUser.setState("P");
		userManagementApprovalUser.setUpdatedTime(ts);
		userManagementApprovalUser.setPayload("{ " + 
				"    \"functionId\": 1," + 
				"    \"username\": \"123456\", " + 
				"    \"email\": \"nesh@rhbgroup.com\"," + 
				"    \"name\": \"Sarjen\", " + 
				"    \"department\": { " + 
				"        \"departmentId\": 1," + 
				"        \"departmentName\": \"BO Admin\" " + 
				"        }, " + 
				"    \"group\": [{" + 
				"        \"groupId\": 1, " + 
				"        \"groupName\": \"Group Numero Uno\" " + 
				"        },{" + 
				"        \"groupId\": 2, " + 
				"        \"groupName\": \"Group Numero Duo\" " + 
				"        }" + 
				"    ] " + 
				"}");
		userManagementApprovalUserList.add(userManagementApprovalUser);
		User user = new User();
		user.setUsername("user1");
		user.setId(1);
		when(userRepositoryMock.findOne(1)).thenReturn(user);
		when(approvalRepositoryMock.findOne(1)).thenReturn(approval);
		List<User> userList = new ArrayList<User>();
		userList.add(new User());
		when(userRepositoryMock.findByUsername("123456")).thenReturn(userList);
		when(boUmApprovalUserRepoMock.findAllByApprovalId(1)).thenReturn(userManagementApprovalUserList);

		WFUserApprovalActionDetail wfUserApprovalActionDetail = new WFUserApprovalActionDetail();
		wfUserApprovalActionDetail.setApprovalId(1);
		wfUserApprovalActionDetail.setReason("approveCreateSuccess");

		wfUserApprovalService.approveCreate(1, wfUserApprovalActionDetail);
	}

	@Test(expected = CommonException.class)
	public void wrongActiontypeTest() throws Exception {
		Timestamp ts = new Timestamp(new Date().getTime());
		Approval approval = new Approval();
		approval.setCreatorId(1);
		approval.setId(1);
		approval.setActionType(MaintenanceActionType.DELETE.getValue());
		approval.setReason("reason");
		approval.setUpdatedTime(ts);
		User user = new User();
		user.setUsername("user1");
		user.setId(1);
		when(userRepositoryMock.findOne(1)).thenReturn(user);
		when(approvalRepositoryMock.findOne(1)).thenReturn(approval);
		WFUserApprovalActionDetail wfUserApprovalActionDetail = new WFUserApprovalActionDetail();
		wfUserApprovalActionDetail.setApprovalId(1);
		wfUserApprovalActionDetail.setReason("approveCreateSuccess");

		wfUserApprovalService.approveCreate(1, wfUserApprovalActionDetail);
	}
	
	@Test(expected = CommonException.class)
	public void wrongRequestHeaderTest() throws Exception {
		when(userRepositoryMock.findOne(1)).thenReturn(null);
		WFUserApprovalActionDetail wfUserApprovalActionDetail = new WFUserApprovalActionDetail();
		wfUserApprovalService.approveCreate(1, wfUserApprovalActionDetail);
	}
	
	@Test(expected = CommonException.class)
	public void wrongRequestHeaderTest2() throws Exception {
		when(userRepositoryMock.findOne(1)).thenReturn(null);
		WFUserApprovalActionDetail wfUserApprovalActionDetail = new WFUserApprovalActionDetail();
		wfUserApprovalService.approveUpdate(1, wfUserApprovalActionDetail);
	}

	@Test(expected = CommonException.class)
	public void wrongActiontypeTest2() throws Exception {
		Timestamp ts = new Timestamp(new Date().getTime());
		Approval approval = new Approval();
		approval.setCreatorId(1);
		approval.setId(1);
		approval.setActionType(MaintenanceActionType.DELETE.getValue());
		approval.setReason("reason");
		approval.setUpdatedTime(ts);
		User user = new User();
		user.setUsername("user1");
		user.setId(1);
		when(userRepositoryMock.findOne(1)).thenReturn(user);
		when(approvalRepositoryMock.findOne(1)).thenReturn(approval);
		WFUserApprovalActionDetail wfUserApprovalActionDetail = new WFUserApprovalActionDetail();
		wfUserApprovalActionDetail.setApprovalId(1);
		wfUserApprovalActionDetail.setReason("approveCreateSuccess");

		wfUserApprovalService.approveUpdate(1, wfUserApprovalActionDetail);
	}

	@Test(expected = CommonException.class)
	public void noApprovalRecordTest() throws Exception {

		WFUserApprovalActionDetail wfUserApprovalActionDetail = new WFUserApprovalActionDetail();
		wfUserApprovalActionDetail.setApprovalId(1);
		wfUserApprovalActionDetail.setReason("approveCreateSuccess");
		User user = new User();
		user.setUsername("user1");
		user.setId(1);
		when(userRepositoryMock.findOne(1)).thenReturn(user);
		wfUserApprovalService.approveCreate(1, wfUserApprovalActionDetail);
	}	
	
	@Test(expected = CommonException.class)
	public void noApprovalRecordTest2() throws Exception {

		WFUserApprovalActionDetail wfUserApprovalActionDetail = new WFUserApprovalActionDetail();
		wfUserApprovalActionDetail.setApprovalId(1);
		wfUserApprovalActionDetail.setReason("approveUpdateSuccess");
		User user = new User();
		user.setUsername("user1");
		user.setId(1);
		when(userRepositoryMock.findOne(1)).thenReturn(user);
		wfUserApprovalService.approveUpdate(1, wfUserApprovalActionDetail);
	}

	@Test(expected = CommonException.class)
	public void missingPayloadTest() throws Exception {

		Timestamp ts = new Timestamp(new Date().getTime());
		Approval approval = new Approval();
		approval.setCreatorId(1);
		approval.setId(1);
		approval.setActionType(MaintenanceActionType.ADD.getValue());
		approval.setReason("reason");
		approval.setUpdatedTime(ts);
		User user = new User();
		user.setUsername("user1");
		user.setId(1);
		when(userRepositoryMock.findOne(1)).thenReturn(user);
		when(approvalRepositoryMock.findOne(1)).thenReturn(approval);
		when(userRepositoryMock.findByUsername("123456")).thenReturn(null);
		when(boUmApprovalUserRepoMock.findAllByApprovalId(1)).thenReturn(new ArrayList<BoUmApprovalUser>());

		WFUserApprovalActionDetail wfUserApprovalActionDetail = new WFUserApprovalActionDetail();
		wfUserApprovalActionDetail.setApprovalId(1);
		wfUserApprovalActionDetail.setReason("approveCreateSuccess");

		wfUserApprovalService.approveCreate(1, wfUserApprovalActionDetail);
	}

	@Test(expected = CommonException.class)
	public void missingPayloadTest2() throws Exception {

		Timestamp ts = new Timestamp(new Date().getTime());
		Approval approval = new Approval();
		approval.setCreatorId(1);
		approval.setId(1);
		approval.setActionType(MaintenanceActionType.EDIT.getValue());
		approval.setReason("reason");
		approval.setUpdatedTime(ts);
		User user = new User();
		user.setUsername("user1");
		user.setId(1);
		when(userRepositoryMock.findOne(1)).thenReturn(user);
		when(approvalRepositoryMock.findOne(1)).thenReturn(approval);
		when(userRepositoryMock.findByUsername("123456")).thenReturn(null);
		when(boUmApprovalUserRepoMock.findAllByApprovalId(1)).thenReturn(new ArrayList<BoUmApprovalUser>());

		WFUserApprovalActionDetail wfUserApprovalActionDetail = new WFUserApprovalActionDetail();
		wfUserApprovalActionDetail.setApprovalId(1);
		wfUserApprovalActionDetail.setReason("approveUpdateSuccess");

		wfUserApprovalService.approveUpdate(1, wfUserApprovalActionDetail);
	}

	@Test(expected = CommonException.class)
	public void wrongPayloadTest() throws Exception {

		Timestamp ts = new Timestamp(new Date().getTime());
		Approval approval = new Approval();
		approval.setCreatorId(1);
		approval.setId(1);
		approval.setActionType(MaintenanceActionType.ADD.getValue());
		approval.setReason("reason");
		approval.setUpdatedTime(ts);
		User user = new User();
		user.setUsername("user1");
		user.setId(1);
		when(userRepositoryMock.findOne(1)).thenReturn(user);
		List<BoUmApprovalUser> userManagementApprovalUserList = new ArrayList<BoUmApprovalUser>();
		BoUmApprovalUser userManagementApprovalUser = new BoUmApprovalUser();
		userManagementApprovalUserList.add(userManagementApprovalUser);
		userManagementApprovalUser = new BoUmApprovalUser();
		userManagementApprovalUserList.add(userManagementApprovalUser);
		WFUserPayload wfUserPayload = new WFUserPayload();
		WFDepartmentPayload wfDepartmentPayload = new WFDepartmentPayload();
		WFUserGroupPayload wfUserGroupPayload = new WFUserGroupPayload();
		//silly but this is for code coverage
		wfUserPayload.toString();
		wfDepartmentPayload.toString();
		wfUserGroupPayload.toString();
		
		when(approvalRepositoryMock.findOne(1)).thenReturn(approval);
		when(userRepositoryMock.findByUsername("123456")).thenReturn(null);
		when(boUmApprovalUserRepoMock.findAllByApprovalId(1)).thenReturn(userManagementApprovalUserList);

		WFUserApprovalActionDetail wfUserApprovalActionDetail = new WFUserApprovalActionDetail();
		wfUserApprovalActionDetail.setApprovalId(1);
		wfUserApprovalActionDetail.setReason("approveCreateSuccess");
		//silly but this is for code coverage
		wfUserApprovalActionDetail.toString();
		wfUserApprovalService.approveCreate(1, wfUserApprovalActionDetail);
	}
	
	@Test(expected = CommonException.class)
	public void wrongPayloadTest2() throws Exception {

		Timestamp ts = new Timestamp(new Date().getTime());
		Approval approval = new Approval();
		approval.setCreatorId(1);
		approval.setId(1);
		approval.setActionType(MaintenanceActionType.EDIT.getValue());
		approval.setReason("reason");
		approval.setUpdatedTime(ts);
		User user = new User();
		user.setUsername("user1");
		user.setId(1);
		when(userRepositoryMock.findOne(1)).thenReturn(user);
		List<BoUmApprovalUser> userManagementApprovalUserList = new ArrayList<BoUmApprovalUser>();
		BoUmApprovalUser userManagementApprovalUser = new BoUmApprovalUser();
		userManagementApprovalUserList.add(userManagementApprovalUser);

		when(approvalRepositoryMock.findOne(1)).thenReturn(approval);
		when(userRepositoryMock.findByUsername("123456")).thenReturn(null);
		when(boUmApprovalUserRepoMock.findAllByApprovalId(1)).thenReturn(userManagementApprovalUserList);

		WFUserApprovalActionDetail wfUserApprovalActionDetail = new WFUserApprovalActionDetail();
		wfUserApprovalActionDetail.setApprovalId(1);
		wfUserApprovalActionDetail.setReason("approveUpdateSuccess");

		wfUserApprovalService.approveUpdate(1, wfUserApprovalActionDetail);
	}
	
	@Test
	public void approveUpdateSuccess() throws Exception {
		Timestamp ts = new Timestamp(new Date().getTime());
		Approval approval = new Approval();
		approval.setCreatorId(1);
		approval.setId(1);
		approval.setActionType(MaintenanceActionType.EDIT.getValue());
		approval.setReason("reason");
		approval.setUpdatedTime(ts);
		List<BoUmApprovalUser> userManagementApprovalUserList = new ArrayList<BoUmApprovalUser>();
		BoUmApprovalUser userManagementApprovalUser = new BoUmApprovalUser();
		userManagementApprovalUser.setId(1);
		userManagementApprovalUser.setApprovalId(1);
		userManagementApprovalUser.setState("A");
		userManagementApprovalUser.setUpdatedTime(ts);
		userManagementApprovalUser.setPayload("{ " + 
				"    \"functionId\": 1," + 
				"    \"userId\": 2," + 
				"    \"username\": \"123456\", " + 
				"    \"email\": \"nesh@rhbgroup.com\"," + 
				"    \"name\": \"Sarjen\", " + 
				"    \"status\": \"A\", " + 
				"    \"department\": { " + 
				"        \"departmentId\": 1," + 
				"        \"departmentName\": \"BO Admin\" " + 
				"        }, " + 
				"    \"group\": [{" + 
				"        \"groupId\": 1, " + 
				"        \"groupName\": \"Group Numero Uno\" " + 
				"        },{" + 
				"        \"groupId\": 2, " + 
				"        \"groupName\": \"Group Numero Duo\" " + 
				"        }" + 
				"    ] " + 
				"}");
		userManagementApprovalUserList.add(userManagementApprovalUser);

		BoUmApprovalUser userManagementApprovalUser2 = new BoUmApprovalUser();
		userManagementApprovalUser2.setId(1);
		userManagementApprovalUser2.setApprovalId(1);
		userManagementApprovalUser2.setState("B");
		userManagementApprovalUser2.setUpdatedTime(ts);
		userManagementApprovalUser2.setPayload("{ " + 
				"    \"functionId\": 1," + 
				"    \"userId\": 2," + 
				"    \"username\": \"123456\", " + 
				"    \"email\": \"nesh@rhbgroup.com\"," + 
				"    \"name\": \"Sarjen\", " + 
				"    \"status\": \"A\", " + 
				"    \"department\": { " + 
				"        \"departmentId\": 1," + 
				"        \"departmentName\": \"BO Admin\" " + 
				"        }, " + 
				"    \"group\": [{" + 
				"        \"groupId\": 1, " + 
				"        \"groupName\": \"Group Numero Uno\" " + 
				"        },{" + 
				"        \"groupId\": 2, " + 
				"        \"groupName\": \"Group Numero Duo\" " + 
				"        }" + 
				"    ] " + 
				"}");
		userManagementApprovalUserList.add(userManagementApprovalUser2);
		
		when(approvalRepositoryMock.findOne(1)).thenReturn(approval);
		User user = new User();
		user.setUsername("user1");
		user.setId(1);
		
		User user2 = new User();
		user2.setUsername("user2");
		user2.setId(2);
		
		List<UserUsergroup> userUserGroupList = new ArrayList<UserUsergroup>();
		UserUsergroup userUserGroup = new UserUsergroup();
		userUserGroup.setUserGroupId(1);
		userUserGroup.setUserId(2);
		userUserGroupList.add(userUserGroup);
		UserUsergroup userUserGroup2 = new UserUsergroup();
		userUserGroup2.setUserGroupId(3);
		userUserGroup2.setUserId(2);
		userUserGroupList.add(userUserGroup2);
		when(userUserGroupRepoMock.findAllByUserId(2)).thenReturn(userUserGroupList);
		when(userRepositoryMock.findOne(1)).thenReturn(user);
		when(userRepositoryMock.findOne(2)).thenReturn(user2);
		when(boUmApprovalUserRepoMock.findAllByApprovalId(1)).thenReturn(userManagementApprovalUserList);
		WFUserApprovalActionDetail wfUserApprovalActionDetail = new WFUserApprovalActionDetail();
		wfUserApprovalActionDetail.setApprovalId(1);
		wfUserApprovalActionDetail.setReason("approveUpdateSuccess");

		BoData boData = wfUserApprovalService.approveUpdate(1, wfUserApprovalActionDetail);
		wfUserApprovalActionDetail = (WFUserApprovalActionDetail) boData;
		System.out.println("approveCreateSuccess result : " + JsonUtil.objectToJson(wfUserApprovalActionDetail));

		assertEquals((Integer) 1, wfUserApprovalActionDetail.getApprovalId());
	}
	
}
