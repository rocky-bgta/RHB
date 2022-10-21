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
import com.rhbgroup.dcpbo.user.common.model.bo.BoConfigDepartment;
import com.rhbgroup.dcpbo.user.common.BoConfigDepartmentRepo;
import com.rhbgroup.dcpbo.user.common.BoData;
import com.rhbgroup.dcpbo.user.common.model.bo.BoUmApprovalUser;
import com.rhbgroup.dcpbo.user.common.BoUmApprovalUserRepo;
import com.rhbgroup.dcpbo.user.common.UserGroupRepository;
import com.rhbgroup.dcpbo.user.common.UserRepository;
import com.rhbgroup.dcpbo.user.common.UserUsergroupRepository;
import com.rhbgroup.dcpbo.user.common.model.bo.UserUsergroup;
import com.rhbgroup.dcpbo.user.common.model.bo.Usergroup;
import com.rhbgroup.dcpbo.user.enums.MaintenanceActionType;
import com.rhbgroup.dcpbo.user.info.model.bo.User;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { WFUserApprovalService.class, WFUserApprovalServiceTest.Config.class })
public class WFUserApprovalServiceTest {

	@Autowired
	WFUserApprovalService wfUserApprovalService;

	@MockBean
	ApprovalRepository approvalRepositoryMock;

	@MockBean
	UserRepository userRepositoryMock;

	@MockBean
	BoUmApprovalUserRepo boUmApprovalUserRepoMock;

	@MockBean
	BoConfigDepartmentRepo boConfigDepartmentRepoMock;

	@MockBean
	UserUsergroupRepository userUserGroupRepoMock;

	@MockBean
	UserGroupRepository userGroupRepoMock;

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
	public void retrieveUserApprovalActionTypeDeleteSuccess() throws Exception {
		Timestamp ts = new Timestamp(new Date().getTime());
		Approval approval = new Approval();
		approval.setCreatorId(1);
		approval.setId(1);
		approval.setActionType(MaintenanceActionType.DELETE.getValue());
		approval.setReason("reason");
		approval.setUpdatedTime(ts);
		approval.setCreatedTime(ts);
		List<BoUmApprovalUser> userManagementApprovalUserList = new ArrayList<BoUmApprovalUser>();
		BoUmApprovalUser userManagementApprovalUser = new BoUmApprovalUser();
		userManagementApprovalUser.setId(1);
		userManagementApprovalUser.setApprovalId(1);

		userManagementApprovalUser.setUpdatedTime(ts);
		userManagementApprovalUser.setPayload("{\"userId\":1,\"username\":\"batman\",\"name\":\"451232\"}");
		userManagementApprovalUserList.add(userManagementApprovalUser);

		User user = new User();
		user.setId(1);
		user.setEmail("aaa@sdsd.com");
		user.setUserDepartmentId(1);
		user.setUserStatusId("ABC");

		when(approvalRepositoryMock.findOne(1)).thenReturn(approval);
		when(userRepositoryMock.findNameById(1)).thenReturn("creator name");
		when(userRepositoryMock.findOne(1)).thenReturn(user);
		when(boUmApprovalUserRepoMock.findAllByApprovalId(1)).thenReturn(userManagementApprovalUserList);

		BoConfigDepartment boConfigDepartment = new BoConfigDepartment();
		boConfigDepartment.setDepartmentName("Digital");

		when(boConfigDepartmentRepoMock.findOne(1)).thenReturn(boConfigDepartment);

		List<UserUsergroup> userUserGroupList = new ArrayList<UserUsergroup>();
		UserUsergroup userUserGroup1 = new UserUsergroup();
		userUserGroup1.setUserGroupId(1);
		userUserGroupList.add(userUserGroup1);
		UserUsergroup userUserGroup2 = new UserUsergroup();
		userUserGroup2.setUserGroupId(2);
		userUserGroupList.add(userUserGroup2);
		when(userUserGroupRepoMock.findAllByUserId(1)).thenReturn(userUserGroupList);

		Usergroup userGroup1 = new Usergroup();
		userGroup1.setGroupName("group 1");
		when(userGroupRepoMock.findOneById(1)).thenReturn(userGroup1);
		Usergroup userGroup2 = new Usergroup();
		userGroup2.setGroupName("group 2");
		when(userGroupRepoMock.findOneById(2)).thenReturn(userGroup2);

		BoData boData = wfUserApprovalService.getWorkflowApprovalDetail(1, 1);
		WFUserApprovalDetail wfUserApprovalDetail = (WFUserApprovalDetail) boData;
		System.out.println(
				"retrieveUserApprovalActionTypeDeleteSuccess result : " + JsonUtil.objectToJson(wfUserApprovalDetail));
		assertEquals("Y", wfUserApprovalDetail.getIsCreator());
	}
	
	@Test
	public void retrieveUserApprovalActionTypeDeleteSuccess2() throws Exception {
		Timestamp ts = new Timestamp(new Date().getTime());
		Approval approval = new Approval();
		approval.setCreatorId(1);
		approval.setId(1);
		approval.setActionType(MaintenanceActionType.DELETE.getValue());
		approval.setReason("reason");
		approval.setUpdatedTime(ts);
		approval.setCreatedTime(ts);
		List<BoUmApprovalUser> userManagementApprovalUserList = new ArrayList<BoUmApprovalUser>();
		BoUmApprovalUser userManagementApprovalUser = new BoUmApprovalUser();
		userManagementApprovalUser.setId(1);
		userManagementApprovalUser.setApprovalId(1);

		userManagementApprovalUser.setUpdatedTime(ts);
		userManagementApprovalUser.setPayload("{\"userId\":1,\"username\":\"batman\",\"name\":\"451232\"}");
		userManagementApprovalUserList.add(userManagementApprovalUser);

		User user = new User();
		user.setId(1);
		user.setEmail("aaa@sdsd.com");
		user.setUserDepartmentId(1);
		user.setUserStatusId("ABC");

		when(approvalRepositoryMock.findOne(1)).thenReturn(approval);
		when(userRepositoryMock.findNameById(1)).thenReturn("creator name");
		when(userRepositoryMock.findOne(1)).thenReturn(user);
		when(boUmApprovalUserRepoMock.findAllByApprovalId(1)).thenReturn(userManagementApprovalUserList);

		BoConfigDepartment boConfigDepartment = new BoConfigDepartment();
		boConfigDepartment.setDepartmentName("Digital");

		when(boConfigDepartmentRepoMock.findOne(1)).thenReturn(boConfigDepartment);

		when(userUserGroupRepoMock.findAllByUserId(1)).thenReturn(new ArrayList<UserUsergroup>());

		Usergroup userGroup1 = new Usergroup();
		userGroup1.setGroupName("group 1");
		when(userGroupRepoMock.findOneById(1)).thenReturn(userGroup1);
		Usergroup userGroup2 = new Usergroup();
		userGroup2.setGroupName("group 2");
		when(userGroupRepoMock.findOneById(2)).thenReturn(userGroup2);

		BoData boData = wfUserApprovalService.getWorkflowApprovalDetail(1, 1);
		WFUserApprovalDetail wfUserApprovalDetail = (WFUserApprovalDetail) boData;
		System.out.println(
				"retrieveUserApprovalActionTypeDeleteSuccess result : " + JsonUtil.objectToJson(wfUserApprovalDetail));
		assertEquals("Y", wfUserApprovalDetail.getIsCreator());
	}

	@Test
	public void retrieveUserApprovalActionTypeAddSuccess() throws Exception {
		Timestamp ts = new Timestamp(new Date().getTime());
		Approval approval = new Approval();
		approval.setCreatorId(2);
		approval.setId(1);
		approval.setActionType(MaintenanceActionType.ADD.getValue());
		approval.setReason("reason");
		approval.setUpdatedTime(ts);
		approval.setCreatedTime(ts);
		List<BoUmApprovalUser> userManagementApprovalUserList = new ArrayList<BoUmApprovalUser>();
		BoUmApprovalUser userManagementApprovalUser = new BoUmApprovalUser();
		userManagementApprovalUser.setId(1);
		userManagementApprovalUser.setApprovalId(1);
		userManagementApprovalUser.setState("A");
		userManagementApprovalUser.setUpdatedTime(ts);
		userManagementApprovalUser.setPayload("{\r\n" + "    \"functionId\": 1,\r\n"
				+ "    \"username\": \"123456\",\r\n" + "    \"email\": \"nesh@rhbgroup.com\",\r\n"
				+ "    \"name\": \"Sarjen\",\r\n" + "    \"department\": {\r\n" + "        \"departmentId\": 1,\r\n"
				+ "        \"departmentName\": \"BO Admin\"\r\n" + "        },\r\n" + "    \"group\": [{\r\n"
				+ "        \"groupId\": 1,\r\n" + "        \"groupName\": \"Group Numero Uno\"\r\n" + "        },{\r\n"
				+ "        \"groupId\": 2,\r\n" + "        \"groupName\": \"Group Numero Duo\"\r\n" + "        }\r\n"
				+ "    ]\r\n" + "}");
		userManagementApprovalUserList.add(userManagementApprovalUser);

		User user = new User();
		user.setId(1);
		user.setEmail("aaa@sdsd.com");
		user.setUserStatusId("ABC");
		when(approvalRepositoryMock.findOne(1)).thenReturn(approval);
		when(userRepositoryMock.findNameById(1)).thenReturn("creator name");
		when(userRepositoryMock.findOne(1)).thenReturn(user);
		when(boUmApprovalUserRepoMock.findAllByApprovalId(1)).thenReturn(userManagementApprovalUserList);

		BoData boData = wfUserApprovalService.getWorkflowApprovalDetail(1, 1);
		WFUserApprovalDetail wfUserApprovalDetail = (WFUserApprovalDetail) boData;
		System.out.println(
				"retrieveUserApprovalActionTypeAddSuccess result : " + JsonUtil.objectToJson(wfUserApprovalDetail));

		assertEquals("N", wfUserApprovalDetail.getIsCreator());
	}

	@Test
	public void retrieveUserApprovalActionTypeEDITSuccess() throws Exception {
		Timestamp ts = new Timestamp(new Date().getTime());
		Approval approval = new Approval();
		approval.setCreatorId(1);
		approval.setId(1);
		approval.setActionType(MaintenanceActionType.EDIT.getValue());
		approval.setReason("reason");
		approval.setUpdatedTime(ts);
		approval.setCreatedTime(ts);
		List<BoUmApprovalUser> userManagementApprovalUserList = new ArrayList<BoUmApprovalUser>();
		BoUmApprovalUser userManagementApprovalUser = new BoUmApprovalUser();
		userManagementApprovalUser.setId(1);
		userManagementApprovalUser.setApprovalId(1);
		userManagementApprovalUser.setState("B");
		userManagementApprovalUser.setUpdatedTime(ts);
		userManagementApprovalUser.setPayload("{\r\n" + "    \"functionId\": 1,\r\n"
				+ "    \"username\": \"123456\",\r\n" + "    \"email\": \"nesh@rhbgroup.com\",\r\n"
				+ "    \"name\": \"Sarjen\",\r\n" + "    \"department\": {\r\n" + "        \"departmentId\": 1,\r\n"
				+ "        \"departmentName\": \"BO Admin\"\r\n" + "        },\r\n" + "    \"group\": [{\r\n"
				+ "        \"groupId\": 1,\r\n" + "        \"groupName\": \"Group Numero Uno\"\r\n" + "        },{\r\n"
				+ "        \"groupId\": 2,\r\n" + "        \"groupName\": \"Group Numero Duo\"\r\n" + "        }\r\n"
				+ "    ]\r\n" + "}");
		userManagementApprovalUserList.add(userManagementApprovalUser);

		BoUmApprovalUser userManagementApprovalUser2 = new BoUmApprovalUser();
		userManagementApprovalUser2.setId(1);
		userManagementApprovalUser2.setApprovalId(1);
		userManagementApprovalUser2.setState("A");
		userManagementApprovalUser2.setUpdatedTime(ts);
		userManagementApprovalUser2.setPayload("{\r\n" + "    \"functionId\": 1,\r\n"
				+ "    \"username\": \"123456\",\r\n" + "    \"email\": \"nesh@rhbgroup.com\",\r\n"
				+ "    \"name\": \"Sarjen\",\r\n" + "    \"department\": {\r\n" + "        \"departmentId\": 1,\r\n"
				+ "        \"departmentName\": \"BO Admin2\"\r\n" + "        },\r\n" + "    \"group\": [{\r\n"
				+ "        \"groupId\": 1,\r\n" + "        \"groupName\": \"Group Numero Uno2\"\r\n" + "        },{\r\n"
				+ "        \"groupId\": 2,\r\n" + "        \"groupName\": \"Group Numero Duo2\"\r\n" + "        }\r\n"
				+ "    ]\r\n" + "}");
		userManagementApprovalUserList.add(userManagementApprovalUser2);
		User user = new User();
		user.setId(1);
		user.setEmail("aaa@sdsd.com");
		user.setUserStatusId("ABC");
		when(approvalRepositoryMock.findOne(1)).thenReturn(approval);
		when(userRepositoryMock.findNameById(1)).thenReturn("creator name");
		when(userRepositoryMock.findOne(1)).thenReturn(user);
		when(boUmApprovalUserRepoMock.findAllByApprovalId(1)).thenReturn(userManagementApprovalUserList);

		BoData boData = wfUserApprovalService.getWorkflowApprovalDetail(1, 1);
		WFUserApprovalDetail wfUserApprovalDetail = (WFUserApprovalDetail) boData;
		System.out.println(
				"retrieveUserApprovalActionTypeEDITSuccess result : " + JsonUtil.objectToJson(wfUserApprovalDetail));

		assertEquals("Y", wfUserApprovalDetail.getIsCreator());
	}

	@Test(expected = CommonException.class)
	public void retrieveUserApprovalActionTypeEDITFailed() throws Exception {
		Timestamp ts = new Timestamp(new Date().getTime());
		Approval approval = new Approval();
		approval.setCreatorId(1);
		approval.setId(1);
		approval.setActionType(MaintenanceActionType.EDIT.getValue());
		approval.setReason("reason");
		approval.setUpdatedTime(ts);
		approval.setCreatedTime(ts);
		List<BoUmApprovalUser> userManagementApprovalUserList = new ArrayList<BoUmApprovalUser>();
		BoUmApprovalUser userManagementApprovalUser = new BoUmApprovalUser();
		userManagementApprovalUser.setId(1);
		userManagementApprovalUser.setApprovalId(1);
		userManagementApprovalUser.setState("B");
		userManagementApprovalUser.setUpdatedTime(ts);
		userManagementApprovalUser.setPayload("{\r\n" + "    \"functionId\": 1,\r\n"
				+ "    \"username\": \"123456\",\r\n" + "    \"email\": \"nesh@rhbgroup.com\",\r\n"
				+ "    \"name\": \"Sarjen\",\r\n" + "    \"department\": {\r\n" + "        \"departmentId\": 1,\r\n"
				+ "        \"departmentName\": \"BO Admin\"\r\n" + "        },\r\n" + "    \"usergroup\": [{\r\n"
				+ "        \"groupId\": 1,\r\n" + "        \"groupName\": \"Group Numero Uno\"\r\n" + "        },{\r\n"
				+ "        \"groupId\": 2,\r\n" + "        \"groupName\": \"Group Numero Duo\"\r\n" + "        }\r\n"
				+ "    ]\r\n" + "}");
		userManagementApprovalUserList.add(userManagementApprovalUser);

		User user = new User();
		user.setId(1);
		user.setEmail("aaa@sdsd.com");
		user.setUserStatusId("ABC");
		when(approvalRepositoryMock.findOne(1)).thenReturn(approval);
		when(userRepositoryMock.findNameById(1)).thenReturn("creator name");
		when(userRepositoryMock.findOne(1)).thenReturn(user);
		when(boUmApprovalUserRepoMock.findAllByApprovalId(1)).thenReturn(userManagementApprovalUserList);

		wfUserApprovalService.getWorkflowApprovalDetail(1, 1);

	}

	@Test(expected = CommonException.class)
	public void retrieveUserApprovalNotFoundException1() throws Exception {
		when(approvalRepositoryMock.findOne(2)).thenReturn(null);
		wfUserApprovalService.getWorkflowApprovalDetail(1, 2);
	}

	@Test(expected = CommonException.class)
	public void retrieveUserApprovalNotFoundException2() throws Exception {
		Timestamp ts = new Timestamp(new Date().getTime());
		Approval approval = new Approval();
		approval.setCreatorId(2);
		approval.setId(2);
		approval.setActionType(MaintenanceActionType.EDIT.getValue());
		approval.setUpdatedTime(ts);
		approval.setCreatedTime(ts);
		when(approvalRepositoryMock.findOne(2)).thenReturn(approval);
		when(boUmApprovalUserRepoMock.findAllByApprovalId(2)).thenReturn(new ArrayList<BoUmApprovalUser>());
		wfUserApprovalService.getWorkflowApprovalDetail(1, 2);
	}

	@Test(expected = CommonException.class)
	public void retrieveUserApprovalNotFoundException3() throws Exception {
		Timestamp ts = new Timestamp(new Date().getTime());
		Approval approval = new Approval();
		approval.setCreatorId(2);
		approval.setId(2);
		approval.setActionType(MaintenanceActionType.ADD.getValue());
		approval.setUpdatedTime(ts);
		approval.setCreatedTime(ts);
		when(approvalRepositoryMock.findOne(2)).thenReturn(approval);
		when(boUmApprovalUserRepoMock.findAllByApprovalId(2)).thenReturn(new ArrayList<BoUmApprovalUser>());
		wfUserApprovalService.getWorkflowApprovalDetail(1, 2);
	}

	@Test(expected = CommonException.class)
	public void retrieveUserApprovalNotFoundException4() throws Exception {
		Timestamp ts = new Timestamp(new Date().getTime());
		Approval approval = new Approval();
		approval.setCreatorId(2);
		approval.setId(2);
		approval.setActionType(MaintenanceActionType.ADD.getValue());
		approval.setUpdatedTime(ts);
		approval.setCreatedTime(ts);
		List<BoUmApprovalUser> userManagementApprovalUserList = new ArrayList<BoUmApprovalUser>();
		BoUmApprovalUser userManagementApprovalUser = new BoUmApprovalUser();
		userManagementApprovalUser.setId(2);
		userManagementApprovalUser.setApprovalId(2);
		userManagementApprovalUser.setUpdatedTime(ts);
		userManagementApprovalUser.setPayload("{\"userId\":1,\"username\":\"batman\",\"name\":\"451232\"}");

		BoUmApprovalUser userManagementApprovalUser2 = new BoUmApprovalUser();
		userManagementApprovalUser.setId(2);
		userManagementApprovalUser.setApprovalId(2);
		userManagementApprovalUser.setUpdatedTime(ts);
		userManagementApprovalUser.setPayload("{\"userId\":1,\"username\":\"batman\",\"name\":\"451232\"}");

		userManagementApprovalUserList.add(userManagementApprovalUser);
		userManagementApprovalUserList.add(userManagementApprovalUser2);
		when(approvalRepositoryMock.findOne(2)).thenReturn(approval);
		when(boUmApprovalUserRepoMock.findAllByApprovalId(2)).thenReturn(userManagementApprovalUserList);
		wfUserApprovalService.getWorkflowApprovalDetail(1, 2);
	}

	@Test(expected = CommonException.class)
	public void retrieveUserApprovalNotFoundException5() throws Exception {
		Timestamp ts = new Timestamp(new Date().getTime());
		Approval approval = new Approval();
		approval.setCreatorId(2);
		approval.setId(2);
		approval.setActionType(MaintenanceActionType.DELETE.getValue());
		approval.setUpdatedTime(ts);
		approval.setCreatedTime(ts);
		when(approvalRepositoryMock.findOne(2)).thenReturn(approval);
		when(boUmApprovalUserRepoMock.findAllByApprovalId(2)).thenReturn(new ArrayList<BoUmApprovalUser>());
		wfUserApprovalService.getWorkflowApprovalDetail(1, 2);
	}

	@Test(expected = CommonException.class)
	public void retrieveUserApprovalNotFoundException6() throws Exception {
		Timestamp ts = new Timestamp(new Date().getTime());
		Approval approval = new Approval();
		approval.setCreatorId(2);
		approval.setId(2);
		approval.setActionType(MaintenanceActionType.DELETE.getValue());
		approval.setUpdatedTime(ts);
		approval.setCreatedTime(ts);
		List<BoUmApprovalUser> userManagementApprovalUserList = new ArrayList<BoUmApprovalUser>();
		BoUmApprovalUser userManagementApprovalUser = new BoUmApprovalUser();
		userManagementApprovalUser.setId(2);
		userManagementApprovalUser.setApprovalId(2);
		userManagementApprovalUser.setUpdatedTime(ts);
		userManagementApprovalUser.setPayload("{\"userId\":1,\"username\":\"batman\",\"name\":\"451232\"}");

		BoUmApprovalUser userManagementApprovalUser2 = new BoUmApprovalUser();
		userManagementApprovalUser.setId(2);
		userManagementApprovalUser.setApprovalId(2);
		userManagementApprovalUser.setUpdatedTime(ts);
		userManagementApprovalUser.setPayload("{\"userId\":1,\"username\":\"batman\",\"name\":\"451232\"}");

		userManagementApprovalUserList.add(userManagementApprovalUser);
		userManagementApprovalUserList.add(userManagementApprovalUser2);
		when(approvalRepositoryMock.findOne(2)).thenReturn(approval);
		when(boUmApprovalUserRepoMock.findAllByApprovalId(2)).thenReturn(userManagementApprovalUserList);
		wfUserApprovalService.getWorkflowApprovalDetail(1, 2);
	}

	@Test(expected = CommonException.class)
	public void retrieveUserApprovalNotFoundException7() throws Exception {
		Timestamp ts = new Timestamp(new Date().getTime());
		Approval approval = new Approval();
		approval.setCreatorId(2);
		approval.setId(2);
		approval.setActionType("UNKNOWN");
		approval.setUpdatedTime(ts);
		approval.setCreatedTime(ts);
		when(approvalRepositoryMock.findOne(2)).thenReturn(approval);
		when(boUmApprovalUserRepoMock.findAllByApprovalId(2)).thenReturn(new ArrayList<BoUmApprovalUser>());
		wfUserApprovalService.getWorkflowApprovalDetail(1, 2);
	}
}
