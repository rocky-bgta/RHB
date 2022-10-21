package com.rhbgroup.dcpbo.user.usergroupdelete;


import com.rhbgroup.dcpbo.common.audit.AdditionalDataHolder;
import com.rhbgroup.dcpbo.common.exception.CommonException;
import com.rhbgroup.dcpbo.user.common.*;
import com.rhbgroup.dcpbo.user.common.model.bo.BoUmApprovalUserGroup;
import com.rhbgroup.dcpbo.user.common.model.bo.BoUserApproval;
import com.rhbgroup.dcpbo.user.common.model.bo.Usergroup;
import com.rhbgroup.dcpbo.user.common.model.bo.UsergroupAccess;
import com.rhbgroup.dcpbo.user.enums.MaintenanceActionType;
import com.rhbgroup.dcpbo.user.info.ConfigFunctionRepo;
import com.rhbgroup.dcpbo.user.info.model.bo.ConfigFunction;
import com.rhbgroup.dcpbo.user.info.model.bo.User;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
		UsergroupDeleteFunctionServiceImplTest.class,
		UsergroupDeleteFunctionServiceImpl.class,
		AdditionalDataHolder.class })
public class UsergroupDeleteFunctionServiceImplTest {

	private static Logger logger = LogManager.getLogger(UsergroupDeleteFunctionServiceImplTest.class);

	@Autowired
	UsergroupDeleteFunctionServiceImpl usergroupDeleteFunctionService;


	@MockBean
	UserRepository userRepoMock;

	@MockBean
	ConfigFunctionRepo configFunctionRepoMock;

	@MockBean
	UserGroupRepository usergroupRepoMock;

	@MockBean
	UserUsergroupRepository userUserGroupRepoMock;

	@MockBean
	BoUserApprovalRepo boUserApprovalRepoMock;

	@MockBean
	BoUmApprovalUserRepo boUmApprovalUserRepoMock;

	@MockBean
	UsergroupAccessRepository usergroupAccessRepoMock;

	@MockBean
	BoUmApprovalUserGroupRepo boUmApprovalUserGroupRepoMock;


	@Test
	public void testDeleteBoUsergroup_noApprovalRequired() {
		logger.debug("testDeleteBoUsergroup_noApprovalRequired()");

		Integer functionId = 1;
		String accessType = "AccessType";
		String groupName = "GroupName";

		User creatorMock = mock(User.class);
		Usergroup deleteUsergroupMock = mock(Usergroup.class);
		ConfigFunction configFunctionMock = mock(ConfigFunction.class);
		UsergroupAccess usergroupAccess1 = mock(UsergroupAccess.class);
		UsergroupAccess usergroupAccess2 = mock(UsergroupAccess.class);

		String expectedRequest = String.format("UsergroupDeleteRequestVo(functionId=%d, groupName=%s, accessType=%s)"
				, functionId, groupName, accessType);
		UsergroupDeleteRequestVo usergroupDeleteRequestVo = new UsergroupDeleteRequestVo();
		usergroupDeleteRequestVo.setFunctionId(1);
		usergroupDeleteRequestVo.setAccessType(accessType);
		usergroupDeleteRequestVo.setGroupName(groupName);

		assertEquals(expectedRequest, usergroupDeleteRequestVo.toString());

		List<UsergroupAccess> uugroupList = new ArrayList<>();
		uugroupList.add(usergroupAccess1);
		uugroupList.add(usergroupAccess2);

		when(creatorMock.getUsername()).thenReturn("creatorUsername");
		when(userRepoMock.findOne(1)).thenReturn(creatorMock);
		when(usergroupRepoMock.findOne(1)).thenReturn(deleteUsergroupMock);
		when(configFunctionRepoMock.findOne(1)).thenReturn(configFunctionMock);
		when(usergroupAccessRepoMock.findByUserGroupId(1)).thenReturn(uugroupList);

		Integer creatorId = 1;
		Integer deleteUsergroupId = 1;
		UsergroupDeleteResponseVo expected = new UsergroupDeleteResponseVo();
		expected.setApprovalId(0);
		expected.setIsWritten("Y");
		UsergroupDeleteResponseVo actual = (UsergroupDeleteResponseVo) usergroupDeleteFunctionService.deleteBoUsergroup(creatorId, usergroupDeleteRequestVo, deleteUsergroupId);

		assertEquals(expected.toString().trim(), actual.toString().trim());
	}

	@Test
	public void testDeleteBoUsergroup_noApprovalRequired_nullUserGroupAccess() {
		logger.debug("testDeleteBoUsergroup_noApprovalRequired_nullUserGroupAccess()");

		Integer functionId = 1;
		String accessType = "AccessType";
		String groupName = "GroupName";

		User creatorMock = mock(User.class);
		Usergroup deleteUsergroupMock = mock(Usergroup.class);
		ConfigFunction configFunctionMock = mock(ConfigFunction.class);
		UsergroupAccess usergroupAccess1 = mock(UsergroupAccess.class);
		UsergroupAccess usergroupAccess2 = mock(UsergroupAccess.class);

		String expectedRequest = String.format("UsergroupDeleteRequestVo(functionId=%d, groupName=%s, accessType=%s)"
				, functionId, groupName, accessType);
		UsergroupDeleteRequestVo usergroupDeleteRequestVo = new UsergroupDeleteRequestVo();
		usergroupDeleteRequestVo.setFunctionId(1);
		usergroupDeleteRequestVo.setAccessType(accessType);
		usergroupDeleteRequestVo.setGroupName(groupName);

		assertEquals(expectedRequest, usergroupDeleteRequestVo.toString());

		when(creatorMock.getUsername()).thenReturn("creatorUsername");
		when(userRepoMock.findOne(1)).thenReturn(creatorMock);
		when(usergroupRepoMock.findOne(1)).thenReturn(deleteUsergroupMock);
		when(configFunctionRepoMock.findOne(1)).thenReturn(configFunctionMock);
		when(usergroupAccessRepoMock.findByUserGroupId(1)).thenReturn(null);

		Integer creatorId = 1;
		Integer deleteUsergroupId = 1;
		UsergroupDeleteResponseVo expected = new UsergroupDeleteResponseVo();
		expected.setApprovalId(0);
		expected.setIsWritten("Y");
		UsergroupDeleteResponseVo actual = (UsergroupDeleteResponseVo) usergroupDeleteFunctionService.deleteBoUsergroup(creatorId, usergroupDeleteRequestVo, deleteUsergroupId);

		assertEquals(expected.toString().trim(), actual.toString().trim());
	}

	@Test
	public void testDeleteBoUsergroup_noApprovalRequired_noUserGroupAccess() {
		logger.debug("testDeleteBoUsergroup_noApprovalRequired_noUserGroupAccess()");

		Integer functionId = 1;
		String accessType = "AccessType";
		String groupName = "GroupName";

		User creatorMock = mock(User.class);
		Usergroup deleteUsergroupMock = mock(Usergroup.class);
		ConfigFunction configFunctionMock = mock(ConfigFunction.class);
		UsergroupAccess usergroupAccess1 = mock(UsergroupAccess.class);
		UsergroupAccess usergroupAccess2 = mock(UsergroupAccess.class);

		String expectedRequest = String.format("UsergroupDeleteRequestVo(functionId=%d, groupName=%s, accessType=%s)"
				, functionId, groupName, accessType);
		UsergroupDeleteRequestVo usergroupDeleteRequestVo = new UsergroupDeleteRequestVo();
		usergroupDeleteRequestVo.setFunctionId(1);
		usergroupDeleteRequestVo.setAccessType(accessType);
		usergroupDeleteRequestVo.setGroupName(groupName);

		assertEquals(expectedRequest, usergroupDeleteRequestVo.toString());

		List<UsergroupAccess> uugroupList = new ArrayList<>();

		when(creatorMock.getUsername()).thenReturn("creatorUsername");
		when(userRepoMock.findOne(1)).thenReturn(creatorMock);
		when(usergroupRepoMock.findOne(1)).thenReturn(deleteUsergroupMock);
		when(configFunctionRepoMock.findOne(1)).thenReturn(configFunctionMock);
		when(usergroupAccessRepoMock.findByUserGroupId(1)).thenReturn(uugroupList);

		Integer creatorId = 1;
		Integer deleteUsergroupId = 1;
		UsergroupDeleteResponseVo expected = new UsergroupDeleteResponseVo();
		expected.setApprovalId(0);
		expected.setIsWritten("Y");
		UsergroupDeleteResponseVo actual = (UsergroupDeleteResponseVo) usergroupDeleteFunctionService.deleteBoUsergroup(creatorId, usergroupDeleteRequestVo, deleteUsergroupId);

		assertEquals(expected.toString().trim(), actual.toString().trim());
	}

	@Test
	public void testDeleteBoUsergroup_approvalRequired() {
		logger.debug("testDeleteBoUsergroup_approvalRequired()");

		User creatorMock = mock(User.class);
		Usergroup deleteUsergroupMock = mock(Usergroup.class);
		UsergroupDeleteRequestVo requestMock = mock(UsergroupDeleteRequestVo.class);
		ConfigFunction configFunctionMock = mock(ConfigFunction.class);
		UsergroupAccess usergroupAccess1 = mock(UsergroupAccess.class);
		UsergroupAccess usergroupAccess2 = mock(UsergroupAccess.class);
		List<UsergroupAccess> uugroupList = new ArrayList<>();
		uugroupList.add(usergroupAccess1);
		uugroupList.add(usergroupAccess2);
		BoUserApproval boUserApprovalMock = mock(BoUserApproval.class);
		BoUserApproval boUserApprovalMock1 = mock(BoUserApproval.class);
		BoUserApproval boUserApprovalMock2 = mock(BoUserApproval.class);
		when(boUserApprovalMock2.getId()).thenReturn(999);
		List<BoUserApproval> approvalList = new ArrayList<>();
		approvalList.add(boUserApprovalMock1);
		approvalList.add(boUserApprovalMock2);

		when(creatorMock.getUsername()).thenReturn("creatorUsername");
		when(userRepoMock.findOne(1)).thenReturn(creatorMock);
		when(usergroupRepoMock.findOne(1)).thenReturn(deleteUsergroupMock);
		when(requestMock.getFunctionId()).thenReturn(1);
		when(configFunctionMock.isApprovalRequired()).thenReturn(true);
		when(configFunctionRepoMock.findOne(1)).thenReturn(configFunctionMock);
		when(boUserApprovalMock.getId()).thenReturn(10);
		when(boUserApprovalRepoMock.findAllByFunctionIdAndStatus(requestMock.getFunctionId(),
				MaintenanceActionType.STATUS_PENDING_APPROVAL.getValue())).thenReturn(approvalList);
		when(boUmApprovalUserGroupRepoMock.findAllByApprovalIdAndLockingId(eq(999), anyString()))
				.thenReturn(null);
		when(boUserApprovalRepoMock.saveAndFlush(any(BoUserApproval.class))).thenReturn(boUserApprovalMock);

		Integer creatorId = 1;
		Integer deleteUsergroupId = 1;
		UsergroupDeleteResponseVo expected = new UsergroupDeleteResponseVo();
		expected.setApprovalId(10);
		expected.setIsWritten("N");

		UsergroupDeleteResponseVo actual = (UsergroupDeleteResponseVo) usergroupDeleteFunctionService.deleteBoUsergroup(creatorId, requestMock, deleteUsergroupId);

		assertEquals(expected.toString().trim(), actual.toString().trim());
	}

	@Test
	public void testDeleteBoUsergroup_approvalRequired_noApprovalList() {
		logger.debug("testDeleteBoUsergroup_approvalRequired_noApprovalList()");

		User creatorMock = mock(User.class);
		Usergroup deleteUsergroupMock = mock(Usergroup.class);
		UsergroupDeleteRequestVo requestMock = mock(UsergroupDeleteRequestVo.class);
		ConfigFunction configFunctionMock = mock(ConfigFunction.class);
		UsergroupAccess usergroupAccess1 = mock(UsergroupAccess.class);
		UsergroupAccess usergroupAccess2 = mock(UsergroupAccess.class);
		List<UsergroupAccess> uugroupList = new ArrayList<>();
		uugroupList.add(usergroupAccess1);
		uugroupList.add(usergroupAccess2);
		BoUserApproval boUserApprovalMock = mock(BoUserApproval.class);
		List<BoUserApproval> approvalList = new ArrayList<>();

		when(creatorMock.getUsername()).thenReturn("creatorUsername");
		when(userRepoMock.findOne(1)).thenReturn(creatorMock);
		when(usergroupRepoMock.findOne(1)).thenReturn(deleteUsergroupMock);
		when(requestMock.getFunctionId()).thenReturn(1);
		when(configFunctionMock.isApprovalRequired()).thenReturn(true);
		when(configFunctionRepoMock.findOne(1)).thenReturn(configFunctionMock);
		when(boUserApprovalMock.getId()).thenReturn(10);
		when(boUserApprovalRepoMock.findAllByFunctionIdAndStatus(requestMock.getFunctionId(),
				MaintenanceActionType.STATUS_PENDING_APPROVAL.getValue())).thenReturn(approvalList);
		when(boUserApprovalRepoMock.saveAndFlush(any(BoUserApproval.class))).thenReturn(boUserApprovalMock);

		Integer creatorId = 1;
		Integer deleteUsergroupId = 1;
		UsergroupDeleteResponseVo expected = new UsergroupDeleteResponseVo();
		expected.setApprovalId(10);
		expected.setIsWritten("N");

		UsergroupDeleteResponseVo actual = (UsergroupDeleteResponseVo) usergroupDeleteFunctionService.deleteBoUsergroup(creatorId, requestMock, deleteUsergroupId);

		assertEquals(expected.toString().trim(), actual.toString().trim());
	}

	@Test
	public void testDeleteBoUsergroup_approvalRequired_nullApprovalList() {
		logger.debug("testDeleteBoUsergroup_approvalRequired_nullApprovalList()");

		User creatorMock = mock(User.class);
		Usergroup deleteUsergroupMock = mock(Usergroup.class);
		UsergroupDeleteRequestVo requestMock = mock(UsergroupDeleteRequestVo.class);
		ConfigFunction configFunctionMock = mock(ConfigFunction.class);
		UsergroupAccess usergroupAccess1 = mock(UsergroupAccess.class);
		UsergroupAccess usergroupAccess2 = mock(UsergroupAccess.class);
		List<UsergroupAccess> uugroupList = new ArrayList<>();
		uugroupList.add(usergroupAccess1);
		uugroupList.add(usergroupAccess2);
		BoUserApproval boUserApprovalMock = mock(BoUserApproval.class);
		List<BoUserApproval> approvalList = new ArrayList<>();

		when(creatorMock.getUsername()).thenReturn("creatorUsername");
		when(userRepoMock.findOne(1)).thenReturn(creatorMock);
		when(usergroupRepoMock.findOne(1)).thenReturn(deleteUsergroupMock);
		when(requestMock.getFunctionId()).thenReturn(1);
		when(configFunctionMock.isApprovalRequired()).thenReturn(true);
		when(configFunctionRepoMock.findOne(1)).thenReturn(configFunctionMock);
		when(boUserApprovalMock.getId()).thenReturn(10);
		when(boUserApprovalRepoMock.findAllByFunctionIdAndStatus(requestMock.getFunctionId(),
				MaintenanceActionType.STATUS_PENDING_APPROVAL.getValue())).thenReturn(null);
		when(boUserApprovalRepoMock.saveAndFlush(any(BoUserApproval.class))).thenReturn(boUserApprovalMock);

		Integer creatorId = 1;
		Integer deleteUsergroupId = 1;
		UsergroupDeleteResponseVo expected = new UsergroupDeleteResponseVo();
		expected.setApprovalId(10);
		expected.setIsWritten("N");

		UsergroupDeleteResponseVo actual = (UsergroupDeleteResponseVo) usergroupDeleteFunctionService.deleteBoUsergroup(creatorId, requestMock, deleteUsergroupId);

		assertEquals(expected.toString().trim(), actual.toString().trim());
	}

	@Test(expected = CommonException.class)
	public void testDeleteBoUsergroup_sameApprovalExist() {
		logger.debug("testDeleteBoUsergroup_sameApprovalExist()");

		User creatorMock = mock(User.class);
		Usergroup deleteUsergroupMock = mock(Usergroup.class);
		UsergroupDeleteRequestVo requestMock = mock(UsergroupDeleteRequestVo.class);
		ConfigFunction configFunctionMock = mock(ConfigFunction.class);
		UsergroupAccess usergroupAccess1 = mock(UsergroupAccess.class);
		UsergroupAccess usergroupAccess2 = mock(UsergroupAccess.class);
		List<UsergroupAccess> uugroupList = new ArrayList<>();
		uugroupList.add(usergroupAccess1);
		uugroupList.add(usergroupAccess2);
		BoUserApproval boUserApprovalMock = mock(BoUserApproval.class);
		BoUserApproval boUserApprovalMock1 = mock(BoUserApproval.class);
		BoUserApproval boUserApprovalMock2 = mock(BoUserApproval.class);
		List<BoUserApproval> approvalList = new ArrayList<>();
		approvalList.add(boUserApprovalMock1);
		approvalList.add(boUserApprovalMock2);
		List<BoUmApprovalUserGroup> umApprovalUsergroup = new ArrayList<>();
		umApprovalUsergroup.add(mock(BoUmApprovalUserGroup.class));

		when(creatorMock.getUsername()).thenReturn("creatorUsername");
		when(userRepoMock.findOne(1)).thenReturn(creatorMock);
		when(usergroupRepoMock.findOne(1)).thenReturn(deleteUsergroupMock);
		when(requestMock.getFunctionId()).thenReturn(1);
		when(configFunctionMock.isApprovalRequired()).thenReturn(true);
		when(configFunctionRepoMock.findOne(1)).thenReturn(configFunctionMock);
		when(boUserApprovalMock.getId()).thenReturn(10);
		when(boUserApprovalRepoMock.findAllByFunctionIdAndStatus(requestMock.getFunctionId(),
				MaintenanceActionType.STATUS_PENDING_APPROVAL.getValue())).thenReturn(approvalList);
		when(boUmApprovalUserGroupRepoMock.findAllByApprovalIdAndLockingId(anyInt(), anyString())).thenReturn(umApprovalUsergroup);

		Integer creatorId = 1;
		Integer deleteUsergroupId = 1;
		UsergroupDeleteResponseVo actual = (UsergroupDeleteResponseVo) usergroupDeleteFunctionService.deleteBoUsergroup(creatorId, requestMock, deleteUsergroupId);
	}

	@Test(expected = CommonException.class)
	public void testDeleteBoUsergroup_creatorIdNotExist() {
		logger.debug("testDeleteBoUsergroup_creatorIdNotExist()");

		User creatorMock = mock(User.class);
		UsergroupDeleteRequestVo requestMock = mock(UsergroupDeleteRequestVo.class);

		when(creatorMock.getUsername()).thenReturn("creatorUsername");
		when(userRepoMock.findOne(1)).thenReturn(null);

		Integer creatorId = 1;
		Integer deleteUsergroupId = 1;
		UsergroupDeleteResponseVo actual = (UsergroupDeleteResponseVo) usergroupDeleteFunctionService.deleteBoUsergroup(creatorId, requestMock, deleteUsergroupId);
	}

	@Test(expected = CommonException.class)
	public void testDeleteBoUsergroup_usergroupIdNotExist() {
		logger.debug("testDeleteBoUsergroup_usergroupIdNotExist()");

		User creatorMock = mock(User.class);
		UsergroupDeleteRequestVo requestMock = mock(UsergroupDeleteRequestVo.class);

		when(creatorMock.getUsername()).thenReturn("creatorUsername");
		when(userRepoMock.findOne(1)).thenReturn(creatorMock);
		when(usergroupRepoMock.findOne(1)).thenReturn(null);

		Integer creatorId = 1;
		Integer deleteUsergroupId = 1;
		UsergroupDeleteResponseVo actual = (UsergroupDeleteResponseVo) usergroupDeleteFunctionService.deleteBoUsergroup(creatorId, requestMock, deleteUsergroupId);
	}

	@Test(expected = CommonException.class)
	public void testDeleteBoUsergroup_functionIdNull() {
		logger.debug("testDeleteBoUsergroup_functionIdNull()");

		User creatorMock = mock(User.class);
		Usergroup deleteUsergroupMock = mock(Usergroup.class);
		UsergroupDeleteRequestVo requestMock = mock(UsergroupDeleteRequestVo.class);

		when(creatorMock.getUsername()).thenReturn("creatorUsername");
		when(userRepoMock.findOne(1)).thenReturn(creatorMock);
		when(usergroupRepoMock.findOne(1)).thenReturn(deleteUsergroupMock);
		when(requestMock.getFunctionId()).thenReturn(null);

		Integer creatorId = 1;
		Integer deleteUsergroupId = 1;
		UsergroupDeleteResponseVo actual = (UsergroupDeleteResponseVo) usergroupDeleteFunctionService.deleteBoUsergroup(creatorId, requestMock, deleteUsergroupId);
	}

	@Test(expected = CommonException.class)
	public void testDeleteBoUsergroup_functionNotFound() {
		logger.debug("testDeleteBoUsergroup_functionNotFound()");

		User creatorMock = mock(User.class);
		Usergroup deleteUsergroupMock = mock(Usergroup.class);
		UsergroupDeleteRequestVo requestMock = mock(UsergroupDeleteRequestVo.class);

		when(creatorMock.getUsername()).thenReturn("creatorUsername");
		when(userRepoMock.findOne(1)).thenReturn(creatorMock);
		when(usergroupRepoMock.findOne(1)).thenReturn(deleteUsergroupMock);
		when(requestMock.getFunctionId()).thenReturn(1);
		when(configFunctionRepoMock.findOne(1)).thenReturn(null);

		Integer creatorId = 1;
		Integer deleteUsergroupId = 1;
		UsergroupDeleteResponseVo actual = (UsergroupDeleteResponseVo) usergroupDeleteFunctionService.deleteBoUsergroup(creatorId, requestMock, deleteUsergroupId);
	}

	@Test
	public void testConvertObjectToJsonString() {
		logger.debug("testConvertObjectToJsonString()");

		String expected = "{\"groupId\":1,\"groupname\":\"GroupName\",\"accessType\":\"AccessType\"}";
		UsergroupPayloadDeleteVo payloadDelete = new UsergroupPayloadDeleteVo();
		payloadDelete.setGroupId(1);
		payloadDelete.setGroupname("GroupName");
		payloadDelete.setAccessType("AccessType");

		String actual = usergroupDeleteFunctionService.convertObjectToJsonString(payloadDelete);
		assertEquals(expected, actual);
	}

	@Test
	public void testConvertObjectToJsonString_JsonProcessingException() {
		logger.debug("testConvertObjectToJsonString()");

		String expected = "";
		UsergroupPayloadDeleteVo payloadDelete = new UsergroupPayloadDeleteVo();
		payloadDelete.setGroupId(1);
		payloadDelete.setGroupname("GroupName");
		payloadDelete.setAccessType("AccessType");

		String actual = usergroupDeleteFunctionService.convertObjectToJsonString(logger);
		assertEquals(expected, actual);
	}
}
