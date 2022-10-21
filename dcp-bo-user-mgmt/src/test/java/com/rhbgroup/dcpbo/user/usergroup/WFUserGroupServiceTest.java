package com.rhbgroup.dcpbo.user.usergroup;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.rhbgroup.dcpbo.user.common.*;
import com.rhbgroup.dcpbo.user.common.model.bo.*;
import com.rhbgroup.dcpbo.user.common.model.bo.Usergroup;
import com.rhbgroup.dcpbo.user.workflow.usergroup.*;
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
import com.rhbgroup.dcpbo.user.enums.MaintenanceActionType;
import com.rhbgroup.dcpbo.user.function.model.bo.Module;
import com.rhbgroup.dcpbo.user.info.model.bo.ConfigFunction;
import com.rhbgroup.dcpbo.user.info.model.bo.User;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { WFUserGroupService.class, WFUserGroupServiceTest.Config.class })
public class WFUserGroupServiceTest {

	@Autowired
	WFUserGroupService wfUserGroupService;

	@MockBean
	ApprovalRepository approvalRepositoryMock;

	@MockBean
	UserRepository userRepositoryMock;

	@MockBean
	BoUmApprovalUserGroupRepo boUmApprovalUserGroupRepoMock;

	@MockBean
	UsergroupAccessRepository userGroupAccessRepositoryMock;

	@MockBean
	ConfigFunctionRepository configFunctionRepositoryMock;
	
	@MockBean
	UserGroupRepository userGroupRepoMock;

	@MockBean
	UserUsergroupRepository userUsergroupRepoMock;

	@TestConfiguration
	static class Config {
		@Bean
		@Primary
		public WFUserGroupService getWFUserGroupService() {
			return new WFUserGroupServiceImpl();
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
		List<BoUmApprovalUserGroup> userGroupApprovalList = new ArrayList<BoUmApprovalUserGroup>();
		BoUmApprovalUserGroup boUmApprovalUserGroup = new BoUmApprovalUserGroup();
		boUmApprovalUserGroup.setId(1);
		boUmApprovalUserGroup.setApprovalId(1);

		boUmApprovalUserGroup.setUpdatedTime(ts);
		boUmApprovalUserGroup.setPayload("{\"groupId\":1,\"groupName\":\"batman\",\"accessType\":\"451232\"}");
		userGroupApprovalList.add(boUmApprovalUserGroup);

		List<UserUsergroup> userUsergroupList = new ArrayList<>();
		UserUsergroup userUsergroup = new UserUsergroup();
		userUsergroupList.add(userUsergroup);

		when(approvalRepositoryMock.findOne(1)).thenReturn(approval);
		when(boUmApprovalUserGroupRepoMock.findAllByApprovalId(1)).thenReturn(userGroupApprovalList);
		when(userUsergroupRepoMock.findByUserGroupId(1)).thenReturn(userUsergroupList);

		List<UsergroupAccess> userUserGroupList = new ArrayList<UsergroupAccess>();
		UsergroupAccess userGroupAccess1 = new UsergroupAccess();
		userGroupAccess1.setFunctionId(1);
		userUserGroupList.add(userGroupAccess1);
		UsergroupAccess userGroupAccess2 = new UsergroupAccess();
		userGroupAccess2.setFunctionId(2);
		userUserGroupList.add(userGroupAccess2);
		when(userGroupAccessRepositoryMock.findByUserGroupId(1)).thenReturn(userUserGroupList);

		ConfigFunction configFunction1 = new ConfigFunction();
		configFunction1.setFunctionName("function name 1");
		when(configFunctionRepositoryMock.findOneById(1)).thenReturn(configFunction1);
		ConfigFunction configFunction2 = new ConfigFunction();
		configFunction2.setFunctionName("function name 2");
		when(configFunctionRepositoryMock.findOneById(2)).thenReturn(configFunction2);

		BoData boData = wfUserGroupService.getWorkflowApprovalDetail(1, 1);
		WFUserGroupApprovalDetail wfUserGroupApprovalDetail = (WFUserGroupApprovalDetail) boData;
		//silly but this is for code coverage
		wfUserGroupApprovalDetail.toString();
		System.out.println("retrieveUserApprovalActionTypeDeleteSuccess result : "
				+ JsonUtil.objectToJson(wfUserGroupApprovalDetail));
		assertEquals("Y", wfUserGroupApprovalDetail.getIsCreator());
	}

	@Test
	public void retrieveUserGroupApprovalActionTypeAddSuccess() throws Exception {
		Timestamp ts = new Timestamp(new Date().getTime());
		Approval approval = new Approval();
		approval.setCreatorId(2);
		approval.setId(1);
		approval.setActionType(MaintenanceActionType.ADD.getValue());
		approval.setReason("reason");
		approval.setStatus("A");
		approval.setUpdatedTime(ts);
		approval.setCreatedTime(ts);
		List<BoUmApprovalUserGroup> userGroupApprovalList = new ArrayList<BoUmApprovalUserGroup>();
		BoUmApprovalUserGroup boUmApprovalUserGroup = new BoUmApprovalUserGroup();
		boUmApprovalUserGroup.setId(1);
		boUmApprovalUserGroup.setApprovalId(1);
		boUmApprovalUserGroup.setState(MaintenanceActionType.USER_USER_GROUP_STATUS_ACTIVE.getValue());
		boUmApprovalUserGroup.setUpdatedTime(ts);
		boUmApprovalUserGroup.setPayload("{ " + 
				"\"groupId\": 1," + 
				"\"groupName\": \"group name add\", " + 
				"\"accessType\": \"M\", " + 
				"\"function\": [{" + 
				"        \"functionId\": 1, " + 
				"        \"functionName\": \"Group Numero Uno\" " + 
				"        },{" + 
				"        \"functionId\": 2, " + 
				"        \"functionName\": \"Group Numero Duo\"" + 
				"        }" + 
				"    	]" + 
				"}");
		userGroupApprovalList.add(boUmApprovalUserGroup);

		List<UserUsergroup> userUsergroupList = new ArrayList<>();
		UserUsergroup userUsergroup = new UserUsergroup();
		userUsergroupList.add(userUsergroup);

		when(approvalRepositoryMock.findOne(1)).thenReturn(approval);
		when(boUmApprovalUserGroupRepoMock.findAllByApprovalId(1)).thenReturn(userGroupApprovalList);
		when(userUsergroupRepoMock.findByUserGroupId(1)).thenReturn(userUsergroupList);

		BoData boData = wfUserGroupService.getWorkflowApprovalDetail(1, 1);
		WFUserGroupApprovalDetail wfUserGroupApprovalDetail = (WFUserGroupApprovalDetail) boData;
		System.out.println(
				"retrieveUserApprovalActionTypeAddSuccess result : " + JsonUtil.objectToJson(wfUserGroupApprovalDetail));

		assertEquals("N", wfUserGroupApprovalDetail.getIsCreator());
	}
	
	@Test
	public void retrieveUserGroupApprovalActionTypeEDITSuccess() throws Exception {
		Timestamp ts = new Timestamp(new Date().getTime());
		Approval approval = new Approval();
		approval.setCreatorId(1);
		approval.setId(1);
		approval.setActionType(MaintenanceActionType.EDIT.getValue());
		approval.setReason("reason");
		approval.setUpdatedTime(ts);
		approval.setCreatedTime(ts);
		List<BoUmApprovalUserGroup> userGroupApprovalList = new ArrayList<BoUmApprovalUserGroup>();
		BoUmApprovalUserGroup boUmApprovalUserGroup = new BoUmApprovalUserGroup();
		boUmApprovalUserGroup.setId(1);
		boUmApprovalUserGroup.setApprovalId(1);
		boUmApprovalUserGroup.setState("B");
		boUmApprovalUserGroup.setUpdatedTime(ts);
		boUmApprovalUserGroup.setPayload("{ " + 
				"\"groupId\": 1," + 
				"\"groupName\": \"group name add\", " + 
				"\"accessType\": \"M\", " + 
				"\"functionId\": [1, 2]" +
				"}");
		userGroupApprovalList.add(boUmApprovalUserGroup);

		List<UserUsergroup> userUsergroupList = new ArrayList<>();
		UserUsergroup userUsergroup = new UserUsergroup();
		userUsergroupList.add(userUsergroup);


		BoUmApprovalUserGroup boUmApprovalUserGroup2 = new BoUmApprovalUserGroup();
		boUmApprovalUserGroup2.setId(1);
		boUmApprovalUserGroup2.setApprovalId(1);
		boUmApprovalUserGroup2.setState(MaintenanceActionType.USER_USER_GROUP_STATUS_ACTIVE.getValue());
		boUmApprovalUserGroup2.setUpdatedTime(ts);
		boUmApprovalUserGroup2.setPayload("{ " + 
				"\"groupId\": 1," + 
				"\"groupName\": \"group name add\", " + 
				"\"accessType\": \"M\", " + 
				"\"functionId\": [1, 2]" +
				"}");
		userGroupApprovalList.add(boUmApprovalUserGroup2);

		when(approvalRepositoryMock.findOne(1)).thenReturn(approval);
		when(boUmApprovalUserGroupRepoMock.findAllByApprovalId(1)).thenReturn(userGroupApprovalList);
		when(userUsergroupRepoMock.findByUserGroupId(1)).thenReturn(userUsergroupList);


		BoData boData = wfUserGroupService.getWorkflowApprovalDetail(1, 1);
		WFUserGroupApprovalDetail wfUserGroupApprovalDetail = (WFUserGroupApprovalDetail) boData;
		System.out.println(
				"retrieveUserApprovalActionTypeEDITSuccess result : " + JsonUtil.objectToJson(wfUserGroupApprovalDetail));

		assertEquals("Y", wfUserGroupApprovalDetail.getIsCreator());
	}
	
	@Test(expected = CommonException.class)
	public void retrieveApprovalNotFoundException() throws Exception {
		when(approvalRepositoryMock.findOne(2)).thenReturn(null);
		wfUserGroupService.getWorkflowApprovalDetail(1, 2);
	}
	
	@Test(expected = CommonException.class)
	public void retrieveUserGroupApprovalNotFoundException() throws Exception {
		Timestamp ts = new Timestamp(new Date().getTime());
		Approval approval = new Approval();
		approval.setCreatorId(1);
		approval.setId(1);
		approval.setActionType(MaintenanceActionType.EDIT.getValue());
		approval.setReason("reason");
		approval.setUpdatedTime(ts);
		approval.setCreatedTime(ts);
		when(approvalRepositoryMock.findOne(2)).thenReturn(approval);
		wfUserGroupService.getWorkflowApprovalDetail(1, 2);
	}
	@Test(expected = CommonException.class)
	public void retrieveUserGroupApprovalNotFoundException2() throws Exception {
		Timestamp ts = new Timestamp(new Date().getTime());
		Approval approval = new Approval();
		approval.setCreatorId(1);
		approval.setId(1);
		approval.setActionType(MaintenanceActionType.EDIT.getValue());
		approval.setReason("reason");
		approval.setUpdatedTime(ts);
		approval.setCreatedTime(ts);
		List<BoUmApprovalUserGroup> userGroupApprovalList = new ArrayList<BoUmApprovalUserGroup>();
		BoUmApprovalUserGroup boUmApprovalUserGroup = new BoUmApprovalUserGroup();
		boUmApprovalUserGroup.setId(1);
		boUmApprovalUserGroup.setApprovalId(1);
		boUmApprovalUserGroup.setState("B");
		boUmApprovalUserGroup.setUpdatedTime(ts);
		boUmApprovalUserGroup.setPayload("{ " + 
				"\"groupId\": 1," + 
				"\"groupName\": \"group name add\", " + 
				"\"accessType\": \"M\", " + 
				"\"function\": [{" + 
				"        \"functionId\": 1, " + 
				"        \"functionName\": \"Group Numero Uno\" " + 
				"        },{" + 
				"        \"functionId\": 2, " + 
				"        \"functionName\": \"Group Numero Duo\"" + 
				"        }" + 
				"    	]" + 
				"}");
		userGroupApprovalList.add(boUmApprovalUserGroup);

		List<UserUsergroup> userUsergroupList = new ArrayList<>();
		UserUsergroup userUsergroup = new UserUsergroup();
		userUsergroupList.add(userUsergroup);

		when(approvalRepositoryMock.findOne(2)).thenReturn(approval);
		when(boUmApprovalUserGroupRepoMock.findAllByApprovalId(2)).thenReturn(userGroupApprovalList);
		when(userUsergroupRepoMock.findByUserGroupId(1)).thenReturn(userUsergroupList);

		wfUserGroupService.getWorkflowApprovalDetail(2, 2);
	}
	
	@Test(expected = CommonException.class)
	public void retrieveUserGroupApprovalNotFoundException3() throws Exception {
		Timestamp ts = new Timestamp(new Date().getTime());
		Approval approval = new Approval();
		approval.setCreatorId(1);
		approval.setId(1);
		approval.setActionType(MaintenanceActionType.ADD.getValue());
		approval.setReason("reason");
		approval.setUpdatedTime(ts);
		approval.setCreatedTime(ts);
		when(approvalRepositoryMock.findOne(2)).thenReturn(approval);
		wfUserGroupService.getWorkflowApprovalDetail(1, 2);
	}
	@Test(expected = CommonException.class)
	public void retrieveUserGroupApprovalNotFoundException6() throws Exception {
		Timestamp ts = new Timestamp(new Date().getTime());
		Approval approval = new Approval();
		approval.setCreatorId(1);
		approval.setId(1);
		approval.setActionType(MaintenanceActionType.ADD.getValue());
		approval.setReason("reason");
		approval.setUpdatedTime(ts);
		approval.setCreatedTime(ts);
		List<BoUmApprovalUserGroup> userGroupApprovalList = new ArrayList<BoUmApprovalUserGroup>();
		BoUmApprovalUserGroup boUmApprovalUserGroup = new BoUmApprovalUserGroup();
		boUmApprovalUserGroup.setId(1);
		boUmApprovalUserGroup.setApprovalId(1);
		boUmApprovalUserGroup.setState("B");
		boUmApprovalUserGroup.setUpdatedTime(ts);
		boUmApprovalUserGroup.setPayload("{ " + 
				"\"groupId\": 1," + 
				"\"groupName\": \"group name add\", " + 
				"\"accessType\": \"M\", " + 
				"\"function\": [{" + 
				"        \"functionId\": 1, " + 
				"        \"functionName\": \"Group Numero Uno\" " + 
				"        },{" + 
				"        \"functionId\": 2, " + 
				"        \"functionName\": \"Group Numero Duo\"" + 
				"        }" + 
				"    	]" + 
				"}");
		userGroupApprovalList.add(boUmApprovalUserGroup);

		List<UserUsergroup> userUsergroupList = new ArrayList<>();
		UserUsergroup userUsergroup = new UserUsergroup();
		userUsergroupList.add(userUsergroup);

		BoUmApprovalUserGroup boUmApprovalUserGroup2 = new BoUmApprovalUserGroup();
		boUmApprovalUserGroup2.setId(1);
		boUmApprovalUserGroup2.setApprovalId(1);
		boUmApprovalUserGroup2.setState(MaintenanceActionType.USER_USER_GROUP_STATUS_ACTIVE.getValue());
		boUmApprovalUserGroup2.setUpdatedTime(ts);
		boUmApprovalUserGroup2.setPayload("{ " + 
				"\"groupId\": 1," + 
				"\"groupName\": \"group name add\", " + 
				"\"accessType\": \"M\", " + 
				"\"function\": [{" + 
				"        \"functionId\": 1, " + 
				"        \"functionName\": \"Group Numero Uno\" " + 
				"        },{" + 
				"        \"functionId\": 2, " + 
				"        \"functionName\": \"Group Numero Duo\"" + 
				"        }" + 
				"    	]" + 
				"}");
		userGroupApprovalList.add(boUmApprovalUserGroup2);
		when(approvalRepositoryMock.findOne(2)).thenReturn(approval);
		when(boUmApprovalUserGroupRepoMock.findAllByApprovalId(2)).thenReturn(userGroupApprovalList);
		when(userUsergroupRepoMock.findByUserGroupId(1)).thenReturn(userUsergroupList);

		wfUserGroupService.getWorkflowApprovalDetail(2, 2);
	}
	
	@Test(expected = CommonException.class)
	public void retrieveUserGroupApprovalNotFoundException7() throws Exception {
		Timestamp ts = new Timestamp(new Date().getTime());
		Approval approval = new Approval();
		approval.setCreatorId(1);
		approval.setId(1);
		approval.setActionType(MaintenanceActionType.DELETE.getValue());
		approval.setReason("reason");
		approval.setUpdatedTime(ts);
		approval.setCreatedTime(ts);
		List<BoUmApprovalUserGroup> userGroupApprovalList = new ArrayList<BoUmApprovalUserGroup>();
		BoUmApprovalUserGroup boUmApprovalUserGroup = new BoUmApprovalUserGroup();
		boUmApprovalUserGroup.setId(1);
		boUmApprovalUserGroup.setApprovalId(1);
		boUmApprovalUserGroup.setState("B");
		boUmApprovalUserGroup.setUpdatedTime(ts);
		boUmApprovalUserGroup.setPayload("{ " + 
				"\"groupId\": 1," + 
				"\"groupName\": \"group name add\", " + 
				"\"accessType\": \"M\", " + 
				"\"function\": [{" + 
				"        \"functionId\": 1, " + 
				"        \"functionName\": \"Group Numero Uno\" " + 
				"        },{" + 
				"        \"functionId\": 2, " + 
				"        \"functionName\": \"Group Numero Duo\"" + 
				"        }" + 
				"    	]" + 
				"}");
		userGroupApprovalList.add(boUmApprovalUserGroup);

		List<UserUsergroup> userUsergroupList = new ArrayList<>();
		UserUsergroup userUsergroup = new UserUsergroup();
		userUsergroupList.add(userUsergroup);

		BoUmApprovalUserGroup boUmApprovalUserGroup2 = new BoUmApprovalUserGroup();
		boUmApprovalUserGroup2.setId(1);
		boUmApprovalUserGroup2.setApprovalId(1);
		boUmApprovalUserGroup2.setState(MaintenanceActionType.USER_USER_GROUP_STATUS_ACTIVE.getValue());
		boUmApprovalUserGroup2.setUpdatedTime(ts);
		boUmApprovalUserGroup2.setPayload("{ " + 
				"\"groupId\": 1," + 
				"\"groupName\": \"group name add\", " + 
				"\"accessType\": \"M\", " + 
				"\"function\": [{" + 
				"        \"functionId\": 1, " + 
				"        \"functionName\": \"Group Numero Uno\" " + 
				"        },{" + 
				"        \"functionId\": 2, " + 
				"        \"functionName\": \"Group Numero Duo\"" + 
				"        }" + 
				"    	]" + 
				"}");
		userGroupApprovalList.add(boUmApprovalUserGroup2);
		when(approvalRepositoryMock.findOne(2)).thenReturn(approval);
		when(boUmApprovalUserGroupRepoMock.findAllByApprovalId(2)).thenReturn(userGroupApprovalList);
		when(userUsergroupRepoMock.findByUserGroupId(1)).thenReturn(userUsergroupList);

		wfUserGroupService.getWorkflowApprovalDetail(2, 2);
	}
	
	@Test(expected = CommonException.class)
	public void retrieveUserApprovalNotFoundException7() throws Exception {
		Timestamp ts = new Timestamp(new Date().getTime());
		Approval approval = new Approval();
		approval.setCreatorId(1);
		approval.setId(1);
		approval.setActionType("UNKWON");
		approval.setReason("reason");
		approval.setUpdatedTime(ts);
		approval.setCreatedTime(ts);
		when(approvalRepositoryMock.findOne(2)).thenReturn(approval);
		wfUserGroupService.getWorkflowApprovalDetail(2, 2);
	}
	
	@Test(expected = CommonException.class)
	public void retrieveUserApprovalNotFoundException8() throws Exception {
		Timestamp ts = new Timestamp(new Date().getTime());
		Approval approval = new Approval();
		approval.setCreatorId(1);
		approval.setId(1);
		approval.setActionType(MaintenanceActionType.DELETE.getValue());
		approval.setReason("reason");
		approval.setUpdatedTime(ts);
		approval.setCreatedTime(ts);
		when(approvalRepositoryMock.findOne(2)).thenReturn(approval);
		wfUserGroupService.getWorkflowApprovalDetail(2, 2);
	}
	
	@Test(expected = CommonException.class)
	public void requestUSerNotFoundException() throws Exception {
		WFUserGroupApprovalActionDetail wfUserGroupApprovalActionDetail = new WFUserGroupApprovalActionDetail();
		wfUserGroupApprovalActionDetail.setApprovalId(1);
		wfUserGroupService.approveCreate(12121, wfUserGroupApprovalActionDetail);
	}
	
	@Test(expected = CommonException.class)
	public void ApprovalNotFoundException() throws Exception {
		WFUserGroupApprovalActionDetail wfUserGroupApprovalActionDetail = new WFUserGroupApprovalActionDetail();
		wfUserGroupApprovalActionDetail.setApprovalId(1);
		User user = new User();
		when(userRepositoryMock.findOne(1)).thenReturn(user);
		wfUserGroupService.approveCreate(1, wfUserGroupApprovalActionDetail);
	}
	
	@Test(expected = CommonException.class)
	public void ApprovalDataActionTypeNotMatchException() throws Exception {
		WFUserGroupApprovalActionDetail wfUserGroupApprovalActionDetail = new WFUserGroupApprovalActionDetail();
		wfUserGroupApprovalActionDetail.setApprovalId(1);
		User user = new User();
		when(userRepositoryMock.findOne(1)).thenReturn(user);
		
		Timestamp ts = new Timestamp(new Date().getTime());
		Approval approval = new Approval();
		approval.setCreatorId(1);
		approval.setId(1);
		approval.setActionType(MaintenanceActionType.DELETE.getValue());
		approval.setReason("reason");
		approval.setUpdatedTime(ts);
		when(approvalRepositoryMock.findOne(1)).thenReturn(approval);
		wfUserGroupService.approveCreate(1, wfUserGroupApprovalActionDetail);
	}
	
	@Test(expected = CommonException.class)
	public void ApprovalDataNotFoundException() throws Exception {
		WFUserGroupApprovalActionDetail wfUserGroupApprovalActionDetail = new WFUserGroupApprovalActionDetail();
		wfUserGroupApprovalActionDetail.setApprovalId(1);
		User user = new User();
		when(userRepositoryMock.findOne(1)).thenReturn(user);
		
		Timestamp ts = new Timestamp(new Date().getTime());
		Approval approval = new Approval();
		approval.setCreatorId(1);
		approval.setId(1);
		approval.setActionType(MaintenanceActionType.ADD.getValue());
		approval.setReason("reason");
		approval.setUpdatedTime(ts);
		when(approvalRepositoryMock.findOne(1)).thenReturn(approval);
		wfUserGroupService.approveCreate(1, wfUserGroupApprovalActionDetail);
	}
	
	@Test(expected = CommonException.class)
	public void ApprovalDataMismatchedAddException() throws Exception {
		WFUserGroupApprovalActionDetail wfUserGroupApprovalActionDetail = new WFUserGroupApprovalActionDetail();
		wfUserGroupApprovalActionDetail.setApprovalId(1);
		User user = new User();
		when(userRepositoryMock.findOne(1)).thenReturn(user);
		
		Timestamp ts = new Timestamp(new Date().getTime());
		Approval approval = new Approval();
		approval.setCreatorId(1);
		approval.setId(1);
		approval.setActionType(MaintenanceActionType.ADD.getValue());
		approval.setReason("reason");
		approval.setUpdatedTime(ts);
		when(approvalRepositoryMock.findOne(1)).thenReturn(approval);
		
		List<BoUmApprovalUserGroup> userGroupApprovalList = new ArrayList<BoUmApprovalUserGroup>();
		userGroupApprovalList.add(new BoUmApprovalUserGroup());
		userGroupApprovalList.add(new BoUmApprovalUserGroup());
		when(boUmApprovalUserGroupRepoMock.findAllByApprovalId(1)).thenReturn(userGroupApprovalList);
		
		wfUserGroupService.approveCreate(1, wfUserGroupApprovalActionDetail);
	}

	@Test(expected = CommonException.class)
	public void ApprovalDataMismatchedEditException() throws Exception {
		WFUserGroupApprovalActionDetail wfUserGroupApprovalActionDetail = new WFUserGroupApprovalActionDetail();
		wfUserGroupApprovalActionDetail.setApprovalId(1);
		User user = new User();
		when(userRepositoryMock.findOne(1)).thenReturn(user);
		
		Timestamp ts = new Timestamp(new Date().getTime());
		Approval approval = new Approval();
		approval.setCreatorId(1);
		approval.setId(1);
		approval.setActionType(MaintenanceActionType.EDIT.getValue());
		approval.setReason("reason");
		approval.setUpdatedTime(ts);
		when(approvalRepositoryMock.findOne(1)).thenReturn(approval);
		
		List<BoUmApprovalUserGroup> userGroupApprovalList = new ArrayList<BoUmApprovalUserGroup>();
		userGroupApprovalList.add(new BoUmApprovalUserGroup());
		when(boUmApprovalUserGroupRepoMock.findAllByApprovalId(1)).thenReturn(userGroupApprovalList);
		
		wfUserGroupService.approveUpdate(1, wfUserGroupApprovalActionDetail);
	}
	
	@Test(expected = CommonException.class)
	public void ApprovalDataMismatchedDeleteException() throws Exception {
		WFUserGroupApprovalActionDetail wfUserGroupApprovalActionDetail = new WFUserGroupApprovalActionDetail();
		wfUserGroupApprovalActionDetail.setApprovalId(1);
		User user = new User();
		when(userRepositoryMock.findOne(1)).thenReturn(user);
		
		Timestamp ts = new Timestamp(new Date().getTime());
		Approval approval = new Approval();
		approval.setCreatorId(1);
		approval.setId(1);
		approval.setActionType(MaintenanceActionType.DELETE.getValue());
		approval.setReason("reason");
		approval.setUpdatedTime(ts);
		when(approvalRepositoryMock.findOne(1)).thenReturn(approval);
		
		List<BoUmApprovalUserGroup> userGroupApprovalList = new ArrayList<BoUmApprovalUserGroup>();
		userGroupApprovalList.add(new BoUmApprovalUserGroup());
		userGroupApprovalList.add(new BoUmApprovalUserGroup());
		when(boUmApprovalUserGroupRepoMock.findAllByApprovalId(1)).thenReturn(userGroupApprovalList);
		
		wfUserGroupService.approveDelete(1, wfUserGroupApprovalActionDetail);
	}
	
	@Test(expected = CommonException.class)
	public void ApprovalUnknownActionTypeException() throws Exception {
		WFUserGroupApprovalActionDetail wfUserGroupApprovalActionDetail = new WFUserGroupApprovalActionDetail();
		wfUserGroupApprovalActionDetail.setApprovalId(1);
		User user = new User();
		when(userRepositoryMock.findOne(1)).thenReturn(user);
		
		Timestamp ts = new Timestamp(new Date().getTime());
		Approval approval = new Approval();
		approval.setCreatorId(1);
		approval.setId(1);
		approval.setActionType("UNKNOWN");
		approval.setReason("reason");
		approval.setUpdatedTime(ts);
		when(approvalRepositoryMock.findOne(1)).thenReturn(approval);
		
		List<BoUmApprovalUserGroup> userGroupApprovalList = new ArrayList<BoUmApprovalUserGroup>();
		userGroupApprovalList.add(new BoUmApprovalUserGroup());
		userGroupApprovalList.add(new BoUmApprovalUserGroup());
		when(boUmApprovalUserGroupRepoMock.findAllByApprovalId(1)).thenReturn(userGroupApprovalList);
		
		wfUserGroupService.approveDelete(1, wfUserGroupApprovalActionDetail);
	}
	
	@Test(expected = CommonException.class)
	public void ApprovalDataGroupNotFoundException() throws Exception {
		WFUserGroupApprovalActionDetail wfUserGroupApprovalActionDetail = new WFUserGroupApprovalActionDetail();
		wfUserGroupApprovalActionDetail.setApprovalId(1);
		User user = new User();
		when(userRepositoryMock.findOne(1)).thenReturn(user);
		
		Timestamp ts = new Timestamp(new Date().getTime());
		Approval approval = new Approval();
		approval.setCreatorId(1);
		approval.setId(1);
		approval.setActionType(MaintenanceActionType.ADD.getValue());
		approval.setReason("reason");
		approval.setUpdatedTime(ts);
		when(approvalRepositoryMock.findOne(1)).thenReturn(approval);
		
		List<BoUmApprovalUserGroup> userGroupApprovalList = new ArrayList<BoUmApprovalUserGroup>();
		BoUmApprovalUserGroup boUmApprovalUserGroup = new BoUmApprovalUserGroup();
		boUmApprovalUserGroup.setId(1);
		boUmApprovalUserGroup.setApprovalId(1);
		boUmApprovalUserGroup.setState("B");
		boUmApprovalUserGroup.setUpdatedTime(ts);
		boUmApprovalUserGroup.setPayload("{ " + 
				"\"groupId\": 1," + 
				"\"groupName\": \"group name add\", " + 
				"\"accessType\": \"M\", " + 
				"\"function\": [{" + 
				"        \"functionId\": 1, " + 
				"        \"functionName\": \"Group Numero Uno\" " + 
				"        },{" + 
				"        \"functionId\": 2, " + 
				"        \"functionName\": \"Group Numero Duo\"" + 
				"        }" + 
				"    	]" + 
				"}");
		userGroupApprovalList.add(boUmApprovalUserGroup);
		List<UserUsergroup> userUsergroupList = new ArrayList<>();
		UserUsergroup userUsergroup = new UserUsergroup();
		userUsergroupList.add(userUsergroup);

		when(boUmApprovalUserGroupRepoMock.findAllByApprovalId(1)).thenReturn(userGroupApprovalList);
		when(userUsergroupRepoMock.findByUserGroupId(1)).thenReturn(userUsergroupList);


		List<Usergroup> userGroupList = new ArrayList<Usergroup>();
		Usergroup userGroup = new Usergroup();
		userGroupList.add(userGroup);
		when(userGroupRepoMock.findByGroupName("group name add")).thenReturn(userGroupList);
		wfUserGroupService.approveCreate(1, wfUserGroupApprovalActionDetail);
	}
	

	@Test
	public void approveCreateSuccess() throws Exception {
		WFUserGroupApprovalActionDetail wfUserGroupApprovalActionDetail = new WFUserGroupApprovalActionDetail();
		wfUserGroupApprovalActionDetail.setApprovalId(1);
		User user = new User();
		when(userRepositoryMock.findOne(1)).thenReturn(user);
		
		Timestamp ts = new Timestamp(new Date().getTime());
		Approval approval = new Approval();
		approval.setCreatorId(1);
		approval.setId(1);
		approval.setActionType(MaintenanceActionType.ADD.getValue());
		approval.setReason("reason");
		approval.setUpdatedTime(ts);
		when(approvalRepositoryMock.findOne(1)).thenReturn(approval);
		
		List<BoUmApprovalUserGroup> userGroupApprovalList = new ArrayList<BoUmApprovalUserGroup>();
		BoUmApprovalUserGroup boUmApprovalUserGroup = new BoUmApprovalUserGroup();
		boUmApprovalUserGroup.setId(1);
		boUmApprovalUserGroup.setApprovalId(1);
		boUmApprovalUserGroup.setState("B");
		boUmApprovalUserGroup.setUpdatedTime(ts);
		boUmApprovalUserGroup.setPayload("{ " + 
				"\"groupId\": 1," + 
				"\"groupName\": \"group name add\", " + 
				"\"accessType\": \"I\", " + 
				"\"function\": [{" + 
				"        \"functionId\": 1, " + 
				"        \"functionName\": \"Group Numero Uno\" " + 
				"        },{" + 
				"        \"functionId\": 2, " + 
				"        \"functionName\": \"Group Numero Duo\"" + 
				"        }" + 
				"    	]" + 
				"}");
		userGroupApprovalList.add(boUmApprovalUserGroup);

		List<UserUsergroup> userUsergroupList = new ArrayList<>();
		UserUsergroup userUsergroup = new UserUsergroup();
		userUsergroupList.add(userUsergroup);

		when(boUmApprovalUserGroupRepoMock.findAllByApprovalId(1)).thenReturn(userGroupApprovalList);
		when(userUsergroupRepoMock.findByUserGroupId(1)).thenReturn(userUsergroupList);
		when(userGroupRepoMock.findByGroupName("group name add")).thenReturn(new ArrayList<Usergroup>());
		
		Module module = new Module();
		module.setId(1);
		ConfigFunction configFunction1 = new ConfigFunction();
		configFunction1.setId(1);
		configFunction1.setFunctionName("User");;
		configFunction1.setMakerScope("maker_user");
		configFunction1.setCheckerScope("checker_user");
		configFunction1.setInquirerScope("inquirer_user");
		configFunction1.setModule(module);
		when(configFunctionRepositoryMock.findOneById(1)).thenReturn(configFunction1);
		
		ConfigFunction configFunction2 = new ConfigFunction();
		configFunction2.setId(2);
		configFunction2.setFunctionName("User Group");;
		configFunction2.setMakerScope("maker_usergroup");
		configFunction2.setCheckerScope("checker_usergroup");
		configFunction2.setInquirerScope("inquirer_usergroup");
		configFunction2.setModule(module);
		when(configFunctionRepositoryMock.findOneById(2)).thenReturn(configFunction2);
		
		wfUserGroupService.approveCreate(1, wfUserGroupApprovalActionDetail);
	}
	
	@Test
	public void approveUpdateSuccess() throws Exception {
		updateSuccess("C");
	}
	
	@Test
	public void approveUpdateSuccess2() throws Exception {
		updateSuccess("M");
	}

	private void updateSuccess(String accessType) {
		WFUserGroupApprovalActionDetail wfUserGroupApprovalActionDetail = new WFUserGroupApprovalActionDetail();
		wfUserGroupApprovalActionDetail.setApprovalId(1);
		User user = new User();
		when(userRepositoryMock.findOne(1)).thenReturn(user);
		
		Timestamp ts = new Timestamp(new Date().getTime());
		Approval approval = new Approval();
		approval.setCreatorId(1);
		approval.setId(1);
		approval.setActionType(MaintenanceActionType.EDIT.getValue());
		approval.setReason("reason");
		approval.setUpdatedTime(ts);
		when(approvalRepositoryMock.findOne(1)).thenReturn(approval);
		
		List<BoUmApprovalUserGroup> userGroupApprovalList = new ArrayList<BoUmApprovalUserGroup>();
		BoUmApprovalUserGroup boUmApprovalUserGroup = new BoUmApprovalUserGroup();
		boUmApprovalUserGroup.setId(1);
		boUmApprovalUserGroup.setApprovalId(1);
		boUmApprovalUserGroup.setState("B");
		boUmApprovalUserGroup.setUpdatedTime(ts);
		boUmApprovalUserGroup.setPayload("{ " + 
				"\"groupId\": 1," + 
				"\"groupName\": \"group name add\", " + 
				"\"accessType\": \"I\", " + 
				"\"function\": [{" + 
				"        \"functionId\": 1, " + 
				"        \"functionName\": \"Group Numero Uno\" " + 
				"        },{" + 
				"        \"functionId\": 2, " + 
				"        \"functionName\": \"Group Numero Duo\"" + 
				"        }" + 
				"    	]" + 
				"}");
		userGroupApprovalList.add(boUmApprovalUserGroup);
		BoUmApprovalUserGroup boUmApprovalUserGroup2 = new BoUmApprovalUserGroup();
		boUmApprovalUserGroup2.setId(1);
		boUmApprovalUserGroup2.setApprovalId(1);
		boUmApprovalUserGroup2.setState("A");
		boUmApprovalUserGroup2.setUpdatedTime(ts);
		boUmApprovalUserGroup2.setPayload("{ " + 
				"\"groupId\": 1," + 
				"\"groupName\": \"group name add\", " + 
				"\"accessType\": \""+accessType+"\", " + 
				"\"functionId\": [1, 2, 4]" +
				"}");
		userGroupApprovalList.add(boUmApprovalUserGroup2);

		List<UserUsergroup> userUsergroupList = new ArrayList<>();
		UserUsergroup userUsergroup = new UserUsergroup();
		userUsergroupList.add(userUsergroup);

		when(boUmApprovalUserGroupRepoMock.findAllByApprovalId(1)).thenReturn(userGroupApprovalList);
		when(userUsergroupRepoMock.findByUserGroupId(1)).thenReturn(userUsergroupList);

		Usergroup userGroup = new Usergroup();
		userGroup.setId(1);
		when(userGroupRepoMock.findOneById(1)).thenReturn(userGroup);
		
		Module module = new Module();
		module.setId(1);
		Module module2 = new Module();
		module2.setId(2);
		
		ConfigFunction configFunction1 = new ConfigFunction();
		configFunction1.setId(1);
		configFunction1.setFunctionName("User");;
		configFunction1.setMakerScope("maker_user");
		configFunction1.setCheckerScope("checker_user");
		configFunction1.setInquirerScope("inquirer_user");
		configFunction1.setModule(module);
		when(configFunctionRepositoryMock.findOneById(1)).thenReturn(configFunction1);
		
		ConfigFunction configFunction2 = new ConfigFunction();
		configFunction2.setId(2);
		configFunction2.setFunctionName("User Group");;
		configFunction2.setMakerScope("maker_usergroup");
		configFunction2.setCheckerScope("checker_usergroup");
		configFunction2.setInquirerScope("inquirer_usergroup");
		configFunction2.setModule(module);
		when(configFunctionRepositoryMock.findOneById(2)).thenReturn(configFunction2);
		
		ConfigFunction configFunction3 = new ConfigFunction();
		configFunction3.setId(3);
		configFunction3.setFunctionName("Activities");;
		configFunction3.setMakerScope("maker_actv");
		configFunction3.setCheckerScope("checker_actv");
		configFunction3.setInquirerScope("inquirer_actv");
		configFunction3.setModule(module2);
		when(configFunctionRepositoryMock.findOneById(3)).thenReturn(configFunction3);
		
		ConfigFunction configFunction4 = new ConfigFunction();
		configFunction4.setId(4);
		configFunction4.setFunctionName("Account");;
		configFunction4.setMakerScope("maker_actlog");
		configFunction4.setCheckerScope("checker_actlog");
		configFunction4.setInquirerScope("inquirer_actlog");
		configFunction4.setModule(module2);
		when(configFunctionRepositoryMock.findOneById(4)).thenReturn(configFunction4);
		
		List<UsergroupAccess> userGroupAccessList = new ArrayList<UsergroupAccess>();
		UsergroupAccess userGroupAccess1 = new UsergroupAccess();
		userGroupAccess1.setFunctionId(1);
		userGroupAccess1.setAccessType("C");
		userGroupAccessList.add(userGroupAccess1);
		UsergroupAccess userGroupAccess2 = new UsergroupAccess();
		userGroupAccess2.setFunctionId(2);
		userGroupAccess2.setAccessType("C");
		userGroupAccessList.add(userGroupAccess2);
		UsergroupAccess userGroupAccess3 = new UsergroupAccess();
		userGroupAccess3.setFunctionId(3);
		userGroupAccess3.setAccessType("C");
		userGroupAccessList.add(userGroupAccess3);
		when(userGroupAccessRepositoryMock.findByUserGroupId(1)).thenReturn(userGroupAccessList);

		wfUserGroupService.approveUpdate(1, wfUserGroupApprovalActionDetail);
	}
	
	@Test
	public void approveDeleteSuccess() throws Exception {
		WFUserGroupApprovalActionDetail wfUserGroupApprovalActionDetail = new WFUserGroupApprovalActionDetail();
		wfUserGroupApprovalActionDetail.setApprovalId(1);
		User user = new User();
		when(userRepositoryMock.findOne(1)).thenReturn(user);
		
		Timestamp ts = new Timestamp(new Date().getTime());
		Approval approval = new Approval();
		approval.setCreatorId(1);
		approval.setId(1);
		approval.setActionType(MaintenanceActionType.DELETE.getValue());
		approval.setReason("reason");
		approval.setUpdatedTime(ts);
		when(approvalRepositoryMock.findOne(1)).thenReturn(approval);
		
		List<BoUmApprovalUserGroup> userGroupApprovalList = new ArrayList<BoUmApprovalUserGroup>();
		BoUmApprovalUserGroup boUmApprovalUserGroup = new BoUmApprovalUserGroup();
		boUmApprovalUserGroup.setId(1);
		boUmApprovalUserGroup.setApprovalId(1);
		boUmApprovalUserGroup.setState("A");
		boUmApprovalUserGroup.setUpdatedTime(ts);
		boUmApprovalUserGroup.setPayload("{ " + 
				"\"groupId\": 1," + 
				"\"groupName\": \"group name add\", " + 
				"\"accessType\": \"M\", " + 
				"\"function\": [{" + 
				"        \"functionId\": 1, " + 
				"        \"functionName\": \"Group Numero Uno\" " + 
				"        },{" + 
				"        \"functionId\": 2, " + 
				"        \"functionName\": \"Group Numero Duo\"" + 
				"        }" + 
				"    	]" + 
				"}");
		userGroupApprovalList.add(boUmApprovalUserGroup);

		List<UserUsergroup> userUsergroupList = new ArrayList<>();
		UserUsergroup userUsergroup = new UserUsergroup();
		userUsergroupList.add(userUsergroup);

		when(boUmApprovalUserGroupRepoMock.findAllByApprovalId(1)).thenReturn(userGroupApprovalList);
		when(userUsergroupRepoMock.findByUserGroupId(1)).thenReturn(userUsergroupList);

		Usergroup userGroup = new Usergroup();
		userGroup.setId(1);
		when(userGroupRepoMock.findOneById(1)).thenReturn(userGroup);
		
		Module module = new Module();
		module.setId(1);
		Module module2 = new Module();
		module2.setId(2);
		
		ConfigFunction configFunction1 = new ConfigFunction();
		configFunction1.setId(1);
		configFunction1.setFunctionName("User");;
		configFunction1.setMakerScope("maker_user");
		configFunction1.setCheckerScope("checker_user");
		configFunction1.setInquirerScope("inquirer_user");
		configFunction1.setModule(module);
		when(configFunctionRepositoryMock.findOneById(1)).thenReturn(configFunction1);
		
		ConfigFunction configFunction2 = new ConfigFunction();
		configFunction2.setId(2);
		configFunction2.setFunctionName("User Group");;
		configFunction2.setMakerScope("maker_usergroup");
		configFunction2.setCheckerScope("checker_usergroup");
		configFunction2.setInquirerScope("inquirer_usergroup");
		configFunction2.setModule(module);
		when(configFunctionRepositoryMock.findOneById(2)).thenReturn(configFunction2);
		
		ConfigFunction configFunction3 = new ConfigFunction();
		configFunction3.setId(3);
		configFunction3.setFunctionName("Activities");;
		configFunction3.setMakerScope("maker_actv");
		configFunction3.setCheckerScope("checker_actv");
		configFunction3.setInquirerScope("inquirer_actv");
		configFunction3.setModule(module2);
		when(configFunctionRepositoryMock.findOneById(3)).thenReturn(configFunction3);
		
		ConfigFunction configFunction4 = new ConfigFunction();
		configFunction4.setId(4);
		configFunction4.setFunctionName("Account");;
		configFunction4.setMakerScope("maker_actlog");
		configFunction4.setCheckerScope("checker_actlog");
		configFunction4.setInquirerScope("inquirer_actlog");
		configFunction4.setModule(module2);
		when(configFunctionRepositoryMock.findOneById(4)).thenReturn(configFunction4);
		
		List<UsergroupAccess> userGroupAccessList = new ArrayList<UsergroupAccess>();
		UsergroupAccess userGroupAccess1 = new UsergroupAccess();
		userGroupAccess1.setFunctionId(1);
		userGroupAccessList.add(userGroupAccess1);
		UsergroupAccess userGroupAccess2 = new UsergroupAccess();
		userGroupAccess2.setFunctionId(2);
		userGroupAccessList.add(userGroupAccess2);
		UsergroupAccess userGroupAccess3 = new UsergroupAccess();
		userGroupAccess3.setFunctionId(3);
		userGroupAccessList.add(userGroupAccess3);
		when(userGroupAccessRepositoryMock.findByUserGroupId(1)).thenReturn(userGroupAccessList);

		wfUserGroupService.approveDelete(1, wfUserGroupApprovalActionDetail);
	}
}
