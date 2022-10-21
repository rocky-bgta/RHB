package com.rhbgroup.dcpbo.user.usergroupupdate;

import com.rhbgroup.dcpbo.common.audit.AdditionalDataHolder;
import com.rhbgroup.dcpbo.user.common.*;
import com.rhbgroup.dcpbo.user.common.model.bo.BoUserApproval;
import com.rhbgroup.dcpbo.user.common.model.bo.Usergroup;
import com.rhbgroup.dcpbo.user.common.model.bo.UsergroupAccess;
import com.rhbgroup.dcpbo.user.enums.MaintenanceActionType;
import com.rhbgroup.dcpbo.user.function.model.bo.Module;
import com.rhbgroup.dcpbo.user.info.ConfigFunctionRepo;
import com.rhbgroup.dcpbo.user.info.model.bo.ConfigFunction;
import com.rhbgroup.dcpbo.user.info.model.bo.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
		UsergroupUpdateFunctionServiceImplTest.class,
		UsergroupUpdateFunctionServiceImpl.class,
		AdditionalDataHolder.class
})
public class UsergroupUpdateFunctionServiceImplTest {

	private static Logger logger = LogManager.getLogger(UsergroupUpdateFunctionServiceImplTest.class);

	@Autowired
	AdditionalDataHolder additionalDataHolder;

	@Autowired
	UsergroupUpdateFunctionService usergroupUpdateFunctionService;

	@MockBean
	UserRepository userRepoMock;

	@MockBean
	ConfigFunctionRepo configFunctionRepoMock;

	@MockBean
	ConfigFunctionRepository configFunctionRepositoryMock;

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


	final Integer updateUsergroupId = 10;
	final Integer creatorId = 1;
	final Integer functionId = 11;

	@Test
	public void testUpdateBoUsergroup_noApprovalRequired() {
		logger.debug("testUpdateBoUsergroup_noApprovalRequired()");

		UsergroupUpdateResponseVo expected = new UsergroupUpdateResponseVo();
		expected.setApprovalId(0);
		expected.setIsWritten("Y");

		when(userRepoMock.findOne(creatorId)).thenReturn(getUser());
		when(usergroupRepoMock.findOne(updateUsergroupId)).thenReturn(getUsergroup());
		when(configFunctionRepositoryMock.findOne(functionId)).thenReturn(getConfigFunction());
		when(usergroupRepoMock.saveAndFlush(anyObject())).thenReturn(getUsergroup());
		when(usergroupAccessRepoMock.findByUserGroupIdAndAccessType(anyInt(), anyString())).thenReturn(getUsergroupInDB());


		UsergroupUpdateResponseVo actual = (UsergroupUpdateResponseVo) usergroupUpdateFunctionService.updateBoUsergroup(creatorId, getRequest(), updateUsergroupId);
		assertEquals(expected.getApprovalId(), actual.getApprovalId());
		assertEquals(expected.getIsWritten(), actual.getIsWritten());
	}

	@Test
	public void testUpdateBoUsergroup_noApprovalRequired_noUsergroupInDB() {
		logger.debug("testUpdateBoUsergroup_noApprovalRequired_noUsergroupInDB()");

		UsergroupUpdateResponseVo expected = new UsergroupUpdateResponseVo();
		expected.setApprovalId(0);
		expected.setIsWritten("Y");

		when(userRepoMock.findOne(creatorId)).thenReturn(getUser());
		when(usergroupRepoMock.findOne(updateUsergroupId)).thenReturn(getUsergroup());
		when(configFunctionRepositoryMock.findOne(functionId)).thenReturn(getConfigFunction());
		when(usergroupRepoMock.saveAndFlush(anyObject())).thenReturn(getUsergroup());


		UsergroupUpdateResponseVo actual = (UsergroupUpdateResponseVo) usergroupUpdateFunctionService.updateBoUsergroup(creatorId, getRequest(), updateUsergroupId);
		assertEquals(expected.getApprovalId(), actual.getApprovalId());
		assertEquals(expected.getIsWritten(), actual.getIsWritten());
	}

	@Test
	public void testUpdateBoUsergroup_noApprovalRequired_nullFunctionId() {
		logger.debug("testUpdateBoUsergroup_noApprovalRequired_nullFunctionId()");

		UsergroupUpdateResponseVo expected = new UsergroupUpdateResponseVo();
		expected.setApprovalId(0);
		expected.setIsWritten("Y");

		UsergroupUpdateRequestVo request = getRequest();
		request.getInput().setFunctionId(null);

		when(userRepoMock.findOne(creatorId)).thenReturn(getUser());
		when(usergroupRepoMock.findOne(updateUsergroupId)).thenReturn(getUsergroup());
		when(configFunctionRepositoryMock.findOne(functionId)).thenReturn(getConfigFunction());
		when(usergroupRepoMock.saveAndFlush(anyObject())).thenReturn(getUsergroup());


		UsergroupUpdateResponseVo actual = (UsergroupUpdateResponseVo) usergroupUpdateFunctionService.updateBoUsergroup(creatorId, request, updateUsergroupId);
		assertEquals(expected.getApprovalId(), actual.getApprovalId());
		assertEquals(expected.getIsWritten(), actual.getIsWritten());
	}

	@Test
	public void testUpdateBoUsergroup_noApprovalRequired_emptyFunctionId() {
		logger.debug("testUpdateBoUsergroup_noApprovalRequired_emptyFunctionId()");

		UsergroupUpdateResponseVo expected = new UsergroupUpdateResponseVo();
		expected.setApprovalId(0);
		expected.setIsWritten("Y");

		UsergroupUpdateRequestVo request = getRequest();
		request.getInput().setFunctionId(new ArrayList<>());

		when(userRepoMock.findOne(creatorId)).thenReturn(getUser());
		when(usergroupRepoMock.findOne(updateUsergroupId)).thenReturn(getUsergroup());
		when(configFunctionRepositoryMock.findOne(functionId)).thenReturn(getConfigFunction());
		when(usergroupRepoMock.saveAndFlush(anyObject())).thenReturn(getUsergroup());


		UsergroupUpdateResponseVo actual = (UsergroupUpdateResponseVo) usergroupUpdateFunctionService.updateBoUsergroup(creatorId, request, updateUsergroupId);
		assertEquals(expected.getApprovalId(), actual.getApprovalId());
		assertEquals(expected.getIsWritten(), actual.getIsWritten());
	}

	@Test
	public void testUpdateBoUsergroup_noApprovalRequired_nullGroupName() {
		logger.debug("testUpdateBoUsergroup_noApprovalRequired_nullGroupName()");

		UsergroupUpdateResponseVo expected = new UsergroupUpdateResponseVo();
		expected.setApprovalId(0);
		expected.setIsWritten("Y");

		UsergroupUpdateRequestVo request = getRequest();
		request.getInput().setGroupName(null);

		UsergroupAccess usergroupAccess1 = getUsergroupAccess();
		UsergroupAccess usergroupAccess2 = getUsergroupAccess();
		usergroupAccess2.setStatus("A");


		when(userRepoMock.findOne(creatorId)).thenReturn(getUser());
		when(usergroupRepoMock.findOne(updateUsergroupId)).thenReturn(getUsergroup());
		when(configFunctionRepositoryMock.findOne(functionId)).thenReturn(getConfigFunction());
		when(usergroupRepoMock.saveAndFlush(anyObject())).thenReturn(getUsergroup());
		when(usergroupAccessRepoMock.findByUserGroupIdAndFunctionIdAndAccessType(anyInt(), eq(1), anyString()))
				.thenReturn(usergroupAccess1);
		when(usergroupAccessRepoMock.findByUserGroupIdAndFunctionIdAndAccessType(anyInt(), eq(2), anyString()))
				.thenReturn(usergroupAccess2);


		UsergroupUpdateResponseVo actual = (UsergroupUpdateResponseVo) usergroupUpdateFunctionService.updateBoUsergroup(creatorId, request, updateUsergroupId);
		assertEquals(expected.getApprovalId(), actual.getApprovalId());
		assertEquals(expected.getIsWritten(), actual.getIsWritten());
	}

	@Test
	public void testUpdateBoUsergroup_approvalRequired() {
		logger.debug("testUpdateBoUsergroup_noApprovalRequired()");

		ConfigFunction configFunction = getConfigFunction();
		configFunction.setApprovalRequired(true);

		Integer boUserApprovalId = 9090;

		BoUserApproval boUserApproval = new BoUserApproval();
		boUserApproval.setId(boUserApprovalId);

		UsergroupUpdateResponseVo expected = new UsergroupUpdateResponseVo();
		expected.setApprovalId(boUserApprovalId);
		expected.setIsWritten("N");

		when(userRepoMock.findOne(creatorId)).thenReturn(getUser());
		when(usergroupRepoMock.findOne(updateUsergroupId)).thenReturn(getUsergroup());
		when(configFunctionRepositoryMock.findOne(functionId)).thenReturn(configFunction);
		when(usergroupRepoMock.saveAndFlush(anyObject())).thenReturn(getUsergroup());
		when(usergroupAccessRepoMock.findByUserGroupIdAndAccessType(anyInt(), anyString())).thenReturn(getUsergroupInDB());
		when(boUserApprovalRepoMock.saveAndFlush(anyObject())).thenReturn(boUserApproval);


		UsergroupUpdateResponseVo actual = (UsergroupUpdateResponseVo) usergroupUpdateFunctionService.updateBoUsergroup(creatorId, getRequest(), updateUsergroupId);
		assertEquals(expected.getApprovalId(), actual.getApprovalId());
		assertEquals(expected.getIsWritten(), actual.getIsWritten());
	}

	private UsergroupUpdateRequestVo getRequest() {
		List<Integer> integerList = new ArrayList<>();
		UsergroupUpdateFunctionVo functionVo = new UsergroupUpdateFunctionVo();
		functionVo.setFunctionId(1);
		functionVo.setFunctionName("User");

		List<UsergroupUpdateFunctionVo> functionVoList = new ArrayList<>();
		functionVoList.add(functionVo);

		integerList.add(1);
		integerList.add(2);

		UsergroupUpdateVo usergroupUpdateVo = new UsergroupUpdateVo();
		usergroupUpdateVo.setAccessType("AccessType");
		usergroupUpdateVo.setGroupName("GroupName");
		usergroupUpdateVo.setFunctionId(integerList);

		UsergroupUpdateRequestVo request = new UsergroupUpdateRequestVo();
		request.setFunctionId(functionId);
		request.setInput(usergroupUpdateVo);
		request.setGroupId(updateUsergroupId);
		request.setCache(usergroupUpdateVo);
		return request;
	}

	private User getUser() {
		User user = new User();
		user.setUsername("Username");

		return user;
	}

	private Usergroup getUsergroup() {
		Usergroup usergroup = new Usergroup();
		usergroup.setId(10);

		return usergroup;
	}

	private ConfigFunction getConfigFunction() {
		Module module = new Module();
		module.setId(99);

		ConfigFunction configFunction = new ConfigFunction();
		configFunction.setApprovalRequired(false);
		configFunction.setModule(module);

		return configFunction;
	}

	private UsergroupAccess getUsergroupAccess() {
		UsergroupAccess usergroupAccess = new UsergroupAccess();
		usergroupAccess.setStatus("D");

		return usergroupAccess;
	}

	private List<UsergroupAccess> getUsergroupInDB() {
		UsergroupAccess usergroupAccess = new UsergroupAccess();
		usergroupAccess.setFunctionId(123);
		usergroupAccess.setStatus(MaintenanceActionType.STATUS_PENDING_APPROVAL.getValue());

		List<UsergroupAccess> userGroupInDBList = new ArrayList<>();
		userGroupInDBList.add(usergroupAccess);

		return userGroupInDBList;
	}
}

