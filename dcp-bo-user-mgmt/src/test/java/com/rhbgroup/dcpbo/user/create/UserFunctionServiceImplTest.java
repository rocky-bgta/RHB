package com.rhbgroup.dcpbo.user.create;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.netflix.feign.FeignContext;
import org.springframework.test.context.junit4.SpringRunner;

import com.rhbgroup.dcpbo.common.audit.AdditionalDataHolder;
import com.rhbgroup.dcpbo.common.exception.CommonException;
import com.rhbgroup.dcpbo.common.exception.CommonExceptionAdvice;
import com.rhbgroup.dcpbo.user.common.model.bo.BoConfigDepartment;
import com.rhbgroup.dcpbo.user.common.BoConfigDepartmentRepo;
import com.rhbgroup.dcpbo.user.common.model.bo.BoUmApprovalUser;
import com.rhbgroup.dcpbo.user.common.BoUmApprovalUserRepo;
import com.rhbgroup.dcpbo.user.common.model.bo.BoUserApproval;
import com.rhbgroup.dcpbo.user.common.BoUserApprovalRepo;
import com.rhbgroup.dcpbo.user.common.UserGroupRepository;
import com.rhbgroup.dcpbo.user.common.UserRepository;
import com.rhbgroup.dcpbo.user.common.model.bo.UserUsergroup;
import com.rhbgroup.dcpbo.user.common.UserUsergroupRepository;
import com.rhbgroup.dcpbo.user.common.model.bo.Usergroup;
import com.rhbgroup.dcpbo.user.info.model.bo.ConfigFunction;
import com.rhbgroup.dcpbo.user.info.ConfigFunctionRepo;
import com.rhbgroup.dcpbo.user.info.model.bo.User;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { UserRepository.class, BoConfigDepartmentRepo.class, ConfigFunctionRepo.class,
		UserGroupRepository.class, UserUsergroupRepository.class, BoUserApprovalRepo.class, BoUmApprovalUserRepo.class,
		AdditionalDataHolder.class, UserFunctionService.class, UserFunctionServiceImpl.class })
public class UserFunctionServiceImplTest {

	@MockBean
	FeignContext feignContext;

	@MockBean
	CommonExceptionAdvice commonExceptionAdvice;

	@MockBean
	UserRepository userRepo;

	@MockBean
	BoConfigDepartmentRepo departmentRepo;

	@MockBean
	ConfigFunctionRepo configFunctionRepo;

	@MockBean
	UserGroupRepository usergroupRepo;

	@MockBean
	UserUsergroupRepository userUserGroupRepo;

	@MockBean
	BoUserApprovalRepo boUserApprovalRepo;

	@MockBean
	BoUmApprovalUserRepo boUmApprovalUserRepo;

	UserCreateResponseVo response;
	UserUpdateRequestVo updateRequest;
	UserDeleteRequestVo deleteRequest;
	UserFunctionDepartmentVo departmentVo;
	List<UserFunctionUserGroupVo> usergroupList;

	@Autowired
	UserFunctionService service;

	@Autowired
	UserFunctionServiceImpl serviceImpl;

	User creator = new User();
	User user = new User();
	ConfigFunction configFunction = new ConfigFunction();
	BoConfigDepartment department = new BoConfigDepartment();
	Usergroup usergroup = new Usergroup();
	List<BoUmApprovalUser> umApproval = new ArrayList<BoUmApprovalUser>();
	BoUserApproval boUserApproval = new BoUserApproval();
	List<BoUserApproval> approvalList = new ArrayList<BoUserApproval>();
	UserUsergroup userUserGroup = new UserUsergroup();
	List<UserUsergroup> userUserGroupInDBList = new ArrayList<UserUsergroup>();
	UserPayloadVo payloadObj = new UserPayloadVo();

	@Before
	public void setup() {
		Integer userId = 1;
		Integer creatorId = 2;
		Integer id = 1;
		String email = "lee.yaw.khang@rhbgroup.com";
		String name = "Lee Yaw Khang";
		Integer approvalId = 30;
		String lockingId = "450569";
		String payload = "{\r\n" + "    \"eventType\": \"Edit-User\",\r\n" + "    \"username\": \"123456\",\r\n"
				+ "    \"department\": \"Marketing\",\r\n" + "    \"email\": \"nesh@rhbgroup.com\",\r\n"
				+ "    \"name\": \"Nesh\"\r\n" + "}";

		user.setId(1);
		user.setName(name);
		user.setEmail(email);
		user.setFailedLoginCount(0);
		user.setUserDepartmentId(1);
		user.setDepartmentName("Administrative");
		user.setUsername("450569");

		assertEquals(userId, user.getId());
		assertEquals(name, user.getName());
		assertEquals(email, user.getEmail());

		usergroup.setId(id);
		usergroup.setGroupName("Administrative");
		usergroup.setGroupStatus("A");

		assertEquals(id, usergroup.getId());
		assertEquals("Administrative", usergroup.getGroupName());
		assertEquals("A", usergroup.getGroupStatus());

		creator.setId(creatorId);
		creator.setName(name);
		creator.setEmail(email);
		creator.setFailedLoginCount(0);
		creator.setUserDepartmentId(1);
		creator.setDepartmentName("Administrative");

		assertEquals(creatorId, creator.getId());
		assertEquals(name, creator.getName());
		assertEquals(email, creator.getEmail());

		configFunction.setId(id);
		configFunction.setFunctionName("Function Name");

		assertEquals(id, configFunction.getId());
		assertEquals("Function Name", configFunction.getFunctionName());

		department.setId(id);
		department.setDepartmentName("Administrative");
		department.setDepartmentCode("DADMIN");

		assertEquals(id, department.getId());
		assertEquals("Administrative", department.getDepartmentName());
		assertEquals("DADMIN", department.getDepartmentCode());

		BoUmApprovalUser umaprvuser1 = new BoUmApprovalUser();
		umaprvuser1.setApprovalId(approvalId);
		umaprvuser1.setId(id);
		umaprvuser1.setLockingId(lockingId);
		umaprvuser1.setPayload(payload);

		assertEquals(approvalId, umaprvuser1.getApprovalId());
		assertEquals(id, umaprvuser1.getId());
		assertEquals(lockingId, umaprvuser1.getLockingId());

		BoUmApprovalUser umaprvuser2 = new BoUmApprovalUser();
		umaprvuser2.setApprovalId(approvalId);
		umaprvuser2.setId(1);
		umaprvuser2.setLockingId(lockingId);
		umaprvuser2.setPayload(payload);

		assertEquals(approvalId, umaprvuser2.getApprovalId());
		assertEquals(id, umaprvuser2.getId());
		assertEquals(lockingId, umaprvuser2.getLockingId());

		umApproval.add(umaprvuser1);
		umApproval.add(umaprvuser2);

		assertEquals(umaprvuser1, umApproval.get(0));
		assertEquals("Administrative", department.getDepartmentName());
		assertEquals("DADMIN", department.getDepartmentCode());

		boUserApproval.setActionType("A");
		boUserApproval.setFunctionId(1);
		boUserApproval.setId(1);
		boUserApproval.setStatus("A");

		assertEquals("A", boUserApproval.getActionType());
		assertEquals(id, boUserApproval.getFunctionId());
		assertEquals(id, boUserApproval.getId());
		assertEquals("A", boUserApproval.getStatus());

		approvalList.add(boUserApproval);
		boUserApproval = new BoUserApproval();
		boUserApproval.setActionType("P");
		boUserApproval.setFunctionId(1);
		boUserApproval.setId(2);
		boUserApproval.setStatus("A");
		approvalList.add(boUserApproval);

		Integer userGroupId = 11;
		userUserGroup.setStatus("D");
		userUserGroup.setUserGroupId(userGroupId);
		userUserGroup.setUserId(1);

		assertEquals("D", userUserGroup.getStatus());
		assertEquals(userGroupId, userUserGroup.getUserGroupId());
		assertEquals(id, userUserGroup.getUserId());

		UserUsergroup uug1 = new UserUsergroup();
		uug1.setStatus("A");
		uug1.setUserGroupId(userGroupId);
		uug1.setUserId(1);

		userUserGroupInDBList.add(userUserGroup);
		userUserGroupInDBList.add(uug1);

		payloadObj.setUsername(lockingId);
		payloadObj.setDepartment(departmentVo);
		payloadObj.setEmail(email);
		payloadObj.setName(name);
		payloadObj.setGroup(usergroupList);
		payloadObj.setStatus("A");

		this.response = givenResult();
	}

	private UserCreateResponseVo givenResult() {

		response = new UserCreateResponseVo();
		response.setApprovalId(30);
		response.setIsWritten("N");

		return this.response;
	}

	private UserCreateRequestVo getRequestData() {
		UserCreateRequestVo request = new UserCreateRequestVo();
		request.setFunctionId(1);
		request.setUsername("450569");
		request.setEmail("lee.yaw.khang@rhbgroup.com");
		request.setName("Lee YK");

		departmentVo = new UserFunctionDepartmentVo();
		departmentVo.setDepartmentId(1);
		departmentVo.setDepartmentName("Administrators");
		request.setDepartment(departmentVo);

		usergroupList = new ArrayList<UserFunctionUserGroupVo>();
		UserFunctionUserGroupVo group = new UserFunctionUserGroupVo();
		group.setGroupId(1);
		group.setGroupName("Admin Maker");
		usergroupList.add(group);

		group = new UserFunctionUserGroupVo();
		group.setGroupId(3);
		group.setGroupName("Admin Viewer");
		usergroupList.add(group);
		request.setUsergroup(usergroupList);

		return request;
	}

	private UserUpdateRequestVo getUpdateRequestData() {
		updateRequest = new UserUpdateRequestVo();

		List<UserFunctionUserGroupVo> groupList = new ArrayList<UserFunctionUserGroupVo>();
		UserFunctionUserGroupVo group = new UserFunctionUserGroupVo();
		group.setGroupId(1);
		group.setGroupName("Admin Maker");
		groupList.add(group);

		group = new UserFunctionUserGroupVo();
		group.setGroupId(3);
		group.setGroupName("Admin Viewer");
		groupList.add(group);

		UserPayloadUpdateVo cache = new UserPayloadUpdateVo();
		cache.setName("handsome");
		cache.setEmail("thisisEmail@hotmail.com");
		cache.setDepartmentId(1);
		cache.setDepartmentName("Admin");
		cache.setGroup(groupList);
		cache.setStatus("A");
		updateRequest.setCache(cache);

		UserPayloadUpdateVo input = new UserPayloadUpdateVo();
		input.setName("handsome");
		input.setEmail("thisisEmail@hotmail.com");
		input.setDepartmentId(1);
		input.setDepartmentName("Admin");
		input.setGroup(groupList);
		input.setStatus("A");
		updateRequest.setInput(input);

		updateRequest.setFunctionId(1);

		return this.updateRequest;
	}

	private UserDeleteRequestVo getDeleteRequestData() {
		deleteRequest = new UserDeleteRequestVo();
		deleteRequest.setFunctionId(1);
		deleteRequest.setUsername("dcpbo5");
		deleteRequest.setName("Dr Strange");

		return this.deleteRequest;
	}

	@Test
	public void createBoUserTest_isApprovalRequiredFalse() {
		UserCreateResponseVo response = givenResult();
		UserCreateRequestVo request = getRequestData();

		when(userRepo.findOne(Mockito.anyInt())).thenReturn(creator);
		when(userRepo.findByUsername(Mockito.anyString())).thenReturn(null);
		when(configFunctionRepo.findOne(1)).thenReturn(configFunction);
		configFunction.setApprovalRequired(false);
		when(departmentRepo.findOne(1)).thenReturn(department);
		when(userRepo.saveAndFlush(Mockito.anyObject())).thenReturn(user);
		when(usergroupRepo.findOne(Mockito.anyInt())).thenReturn(usergroup);
		when(boUmApprovalUserRepo.findAllByApprovalIdAndLockingId(1, request.getUsername())).thenReturn(umApproval);
		service.createBoUser(6, request);
	}

	@Test(expected = CommonException.class)
	public void createBoUserTest_isApprovalRequiredFalse_UsergroupNotFound() {
		UserCreateResponseVo response = givenResult();
		UserCreateRequestVo request = getRequestData();

		when(userRepo.findOne(Mockito.anyInt())).thenReturn(creator);
		when(userRepo.findByUsername(Mockito.anyString())).thenReturn(null);
		when(configFunctionRepo.findOne(1)).thenReturn(configFunction);
		configFunction.setApprovalRequired(false);
		when(departmentRepo.findOne(1)).thenReturn(department);
		when(userRepo.saveAndFlush(Mockito.anyObject())).thenReturn(user);
		when(usergroupRepo.findOne(Mockito.anyInt())).thenReturn(null);
		when(boUmApprovalUserRepo.findAllByApprovalIdAndLockingId(1, request.getUsername())).thenReturn(umApproval);
		service.createBoUser(6, request);
	}

	@Test(expected = CommonException.class)
	public void createBoUserTest_isApprovalRequiredTrue_duplicateUmApproval() {
		UserCreateResponseVo response = givenResult();
		UserCreateRequestVo request = getRequestData();

		when(userRepo.findOne(Mockito.anyInt())).thenReturn(creator);
		when(userRepo.findByUsername(Mockito.anyString())).thenReturn(null);
		when(configFunctionRepo.findOne(1)).thenReturn(configFunction);
		configFunction.setApprovalRequired(true);
		when(departmentRepo.findOne(1)).thenReturn(department);

		when(boUserApprovalRepo.findAllByFunctionIdAndStatus(request.getFunctionId(), "P")).thenReturn(approvalList);
		when(boUmApprovalUserRepo.findAllByApprovalIdAndLockingId(1, request.getUsername())).thenReturn(umApproval);
		when(boUserApprovalRepo.saveAndFlush(Mockito.anyObject())).thenReturn(boUserApproval);
		service.createBoUser(6, request);
	}

	@Test
	public void createBoUserTest_isApprovalRequiredTrue_Success() {
		UserCreateResponseVo response = givenResult();
		UserCreateRequestVo request = getRequestData();

		when(userRepo.findOne(Mockito.anyInt())).thenReturn(creator);
		when(userRepo.findByUsername(Mockito.anyString())).thenReturn(null);
		when(configFunctionRepo.findOne(1)).thenReturn(configFunction);
		configFunction.setApprovalRequired(true);
		when(departmentRepo.findOne(1)).thenReturn(department);

		when(boUserApprovalRepo.findAllByFunctionIdAndStatus(request.getFunctionId(), "P")).thenReturn(approvalList);
		when(boUmApprovalUserRepo.findAllByApprovalIdAndLockingId(1, request.getUsername())).thenReturn(null);
		when(boUserApprovalRepo.saveAndFlush(Mockito.anyObject())).thenReturn(boUserApproval);
		service.createBoUser(6, request);
	}

	@Test(expected = CommonException.class)
	public void createBoUserTest_NoCreator() {
		UserCreateRequestVo request = getRequestData();
		Integer creatorId = 1;

		// when(userRepo.findOne(Mockito.anyInt())).thenReturn(null);
		// BDDMockito.given(when(service.createBoUser(creatorId,
		// request))).willThrow(new CommonException());
		UserCreateResponseVo responseVo = (UserCreateResponseVo) service.createBoUser(creatorId, request);
	}

	@Test(expected = CommonException.class)
	public void createBoUserTest_NoUserName() {
		UserCreateRequestVo request = getRequestData();
		Integer creatorId = 1;

		when(userRepo.findOne(Mockito.anyInt())).thenReturn(user);
		when(userRepo.findByUsername(Mockito.anyString())).thenReturn(null);
		// BDDMockito.given(service.createBoUser(creatorId, request)).willThrow(new
		// CommonException());
		UserCreateResponseVo responseVo = (UserCreateResponseVo) service.createBoUser(creatorId, request);
	}

	@Test(expected = CommonException.class)
	public void createBoUserTest_NoFunctionId() {
		UserCreateRequestVo request = getRequestData();
		Integer creatorId = 1;
		Integer functionId = 1;

		when(userRepo.findOne(Mockito.anyInt())).thenReturn(user);

		List<User> userList = new ArrayList<>();
		userList.add(user);

		when(userRepo.findByUsername(Mockito.anyString())).thenReturn(userList);
		when(userRepo.findOne(Mockito.anyInt())).thenReturn(null);
		UserCreateResponseVo responseVo = (UserCreateResponseVo) service.createBoUser(creatorId, request);
	}

	@Test(expected = CommonException.class)
	public void createBoUserTest_UserNameExisted() {
		UserCreateRequestVo request = getRequestData();
		request.setUsername("450569");
		Integer creatorId = 1;

		List<User> userList = new ArrayList<>();
		userList.add(user);

		when(userRepo.findOne(Mockito.anyInt())).thenReturn(user);
		when(userRepo.findByUsername(request.getUsername())).thenReturn(userList);
		UserCreateResponseVo responseVo = (UserCreateResponseVo) service.createBoUser(creatorId, request);
	}

	@Test(expected = CommonException.class)
	public void createBoUserTest_DepartmentNull() {
		UserCreateRequestVo request = getRequestData();
		Integer creatorId = 1;
		request.setDepartment(null);

		when(userRepo.findOne(Mockito.anyInt())).thenReturn(creator);
		when(userRepo.findByUsername(Mockito.anyString())).thenReturn(null);
		when(configFunctionRepo.findOne(1)).thenReturn(configFunction);
		when(departmentRepo.findOne(1)).thenReturn(null);
		when(userRepo.saveAndFlush(Mockito.anyObject())).thenReturn(user);
		when(usergroupRepo.findOne(Mockito.anyInt())).thenReturn(usergroup);
		UserCreateResponseVo responseVo = (UserCreateResponseVo) service.createBoUser(creatorId, request);
	}

	@Test(expected = CommonException.class)
	public void createBoUserTest_DepartmentNotFound() {
		UserCreateRequestVo request = getRequestData();
		Integer creatorId = 1;

		when(userRepo.findOne(Mockito.anyInt())).thenReturn(creator);
		when(userRepo.findByUsername(Mockito.anyString())).thenReturn(null);
		when(configFunctionRepo.findOne(1)).thenReturn(configFunction);
		when(departmentRepo.findOne(1)).thenReturn(null);
		when(userRepo.saveAndFlush(Mockito.anyObject())).thenReturn(user);
		when(usergroupRepo.findOne(Mockito.anyInt())).thenReturn(usergroup);
		UserCreateResponseVo responseVo = (UserCreateResponseVo) service.createBoUser(creatorId, request);
	}

	@Test(expected = CommonException.class)
	public void createBoUserTest_FunctionIdNull() {
		UserCreateRequestVo request = getRequestData();
		Integer creatorId = 1;
		request.setFunctionId(null);

		when(userRepo.findOne(Mockito.anyInt())).thenReturn(creator);
		when(userRepo.findByUsername(Mockito.anyString())).thenReturn(null);
		when(configFunctionRepo.findOne(1)).thenReturn(configFunction);
		when(departmentRepo.findOne(1)).thenReturn(null);
		when(userRepo.saveAndFlush(Mockito.anyObject())).thenReturn(user);
		when(usergroupRepo.findOne(Mockito.anyInt())).thenReturn(usergroup);
		UserCreateResponseVo responseVo = (UserCreateResponseVo) service.createBoUser(creatorId, request);
	}

	@Test(expected = CommonException.class)
	public void createBoUserTest_Failed() throws CommonException {
		UserCreateRequestVo request = getRequestData();

		when(userRepo.findOne(Mockito.anyInt())).thenReturn(null);
		when(configFunctionRepo.findOne(1)).thenReturn(configFunction);
		when(departmentRepo.findOne(1)).thenReturn(department);
		when(usergroupRepo.findOne(1)).thenReturn(usergroup);
		when(boUmApprovalUserRepo.findAllByApprovalIdAndLockingId(1, "leeyk")).thenReturn(umApproval);
		service.createBoUser(6, request);
	}

	@Test
	public void successUpdateBoUser() throws Exception {
		Integer creatorId = 6;
		String userid = "6";

		List<User> userList = new ArrayList<>();
		userList.add(user);

		when(userRepo.findOne(Mockito.anyInt())).thenReturn(user);
		when(userRepo.findByUsername(Mockito.anyString())).thenReturn(userList);
		when(configFunctionRepo.findOne(1)).thenReturn(configFunction);
		when(departmentRepo.findOne(1)).thenReturn(department);
		when(userRepo.saveAndFlush(Mockito.anyObject())).thenReturn(user);
		when(usergroupRepo.findOne(Mockito.anyInt())).thenReturn(usergroup);
		when(boUmApprovalUserRepo.findAllByApprovalIdAndLockingId(1, "leeyk")).thenReturn(umApproval);

		service.updateBoUser(creatorId, getUpdateRequestData(), userid);
	}

	@Test(expected = CommonException.class)
	public void failedUpdateBoUser_isApprovalRequiredTrue_UMApprovalExisted() throws Exception {
		Integer creatorId = 6;
		String userid = "6";
		UserUpdateRequestVo request = getUpdateRequestData();

		List<User> userList = new ArrayList<>();
		userList.add(user);

		when(userRepo.findOne(Mockito.anyInt())).thenReturn(user);
		when(userRepo.findByUsername(Mockito.anyString())).thenReturn(userList);
		when(configFunctionRepo.findOne(Mockito.anyInt())).thenReturn(configFunction);
		configFunction.setApprovalRequired(Boolean.TRUE);
		when(departmentRepo.findOne(Mockito.anyInt())).thenReturn(department);
		when(boUserApprovalRepo.findAllByFunctionIdAndStatus(request.getFunctionId(), "P")).thenReturn(approvalList);
		when(boUmApprovalUserRepo.findAllByApprovalIdAndLockingId(1, user.getUsername())).thenReturn(umApproval);
		when(boUserApprovalRepo.saveAndFlush(Mockito.anyObject())).thenReturn(boUserApproval);

		service.updateBoUser(creatorId, getUpdateRequestData(), userid);
	}

	@Test
	public void failedUpdateBoUser_isApprovalRequiredTrue_UMApprovalNotExisted() throws Exception {
		Integer creatorId = 6;
		String userid = "6";
		UserUpdateRequestVo request = getUpdateRequestData();

		List<User> userList = new ArrayList<>();
		userList.add(user);

		when(userRepo.findOne(Mockito.anyInt())).thenReturn(user);
		when(userRepo.findByUsername(Mockito.anyString())).thenReturn(userList);
		when(configFunctionRepo.findOne(Mockito.anyInt())).thenReturn(configFunction);
		configFunction.setApprovalRequired(Boolean.TRUE);
		when(departmentRepo.findOne(Mockito.anyInt())).thenReturn(department);
		when(boUserApprovalRepo.findAllByFunctionIdAndStatus(request.getFunctionId(), "P")).thenReturn(null);
		when(boUserApprovalRepo.saveAndFlush(Mockito.anyObject())).thenReturn(boUserApproval);

		service.updateBoUser(creatorId, getUpdateRequestData(), userid);
	}

	@Test(expected = CommonException.class)
	public void failedUpdateBoUser_isApprovalRequiredFalse_UsergroupNull() throws Exception {
		Integer creatorId = 6;
		String userid = "6";
		UserUpdateRequestVo request = getUpdateRequestData();

		List<User> userList = new ArrayList<>();
		userList.add(user);

		when(userRepo.findOne(Mockito.anyInt())).thenReturn(user);
		when(userRepo.findByUsername(Mockito.anyString())).thenReturn(userList);
		when(configFunctionRepo.findOne(Mockito.anyInt())).thenReturn(configFunction);
		configFunction.setApprovalRequired(Boolean.FALSE);
		when(departmentRepo.findOne(Mockito.anyInt())).thenReturn(department);
		when(userRepo.saveAndFlush(Mockito.anyObject())).thenReturn(user);
		when(usergroupRepo.findOne(Mockito.anyInt())).thenReturn(null);
		when(boUmApprovalUserRepo.findAllByApprovalIdAndLockingId(1, "leeyk")).thenReturn(umApproval);

		service.updateBoUser(creatorId, getUpdateRequestData(), userid);
	}

	@Test
	public void successUpdateBoUser_isApprovalRequiredFalse_UserusergroupNotNull() throws Exception {
		Integer creatorId = 6;
		String userid = "6";
		UserUpdateRequestVo request = getUpdateRequestData();

		List<User> userList = new ArrayList<>();
		userList.add(user);

		when(userRepo.findOne(Mockito.anyInt())).thenReturn(user);
		when(userRepo.findByUsername(Mockito.anyString())).thenReturn(userList);
		when(configFunctionRepo.findOne(Mockito.anyInt())).thenReturn(configFunction);
		configFunction.setApprovalRequired(Boolean.FALSE);
		when(departmentRepo.findOne(Mockito.anyInt())).thenReturn(department);
		when(userRepo.saveAndFlush(Mockito.anyObject())).thenReturn(user);
		when(usergroupRepo.findOne(Mockito.anyInt())).thenReturn(usergroup);
		when(userUserGroupRepo.findOneByUserIdAndUserGroupId(user.getId(), usergroup.getId()))
				.thenReturn(userUserGroup);
		when(userUserGroupRepo.findAllByUserId(Mockito.anyInt())).thenReturn(userUserGroupInDBList);
		when(boUmApprovalUserRepo.findAllByApprovalIdAndLockingId(1, "leeyk")).thenReturn(umApproval);
		service.updateBoUser(creatorId, getUpdateRequestData(), userid);
	}

	@Test
	public void successUpdateBoUser_isApprovalRequiredFalse_UserusergroupNull() throws Exception {
		Integer creatorId = 6;
		String userid = "6";
		UserUpdateRequestVo request = getUpdateRequestData();

		List<User> userList = new ArrayList<>();
		userList.add(user);

		when(userRepo.findOne(Mockito.anyInt())).thenReturn(user);
		when(userRepo.findByUsername(Mockito.anyString())).thenReturn(userList);
		when(configFunctionRepo.findOne(Mockito.anyInt())).thenReturn(configFunction);
		configFunction.setApprovalRequired(Boolean.FALSE);
		when(departmentRepo.findOne(Mockito.anyInt())).thenReturn(department);
		when(userRepo.saveAndFlush(Mockito.anyObject())).thenReturn(user);
		when(usergroupRepo.findOne(Mockito.anyInt())).thenReturn(usergroup);
		when(userUserGroupRepo.findOneByUserIdAndUserGroupId(Mockito.anyInt(), Mockito.anyInt())).thenReturn(null);
		service.updateBoUser(creatorId, getUpdateRequestData(), userid);
	}

	@Test(expected = CommonException.class)
	public void failedUpdateBoUser_CreatorNotFound() throws Exception {
		Integer creatorId = 6;
		String userid = "6";

		when(userRepo.findOne(Mockito.anyInt())).thenReturn(null);
		service.updateBoUser(creatorId, getUpdateRequestData(), userid);
	}

	@Test(expected = CommonException.class)
	public void failedUpdateBoUser_UpdateUserNotFound() throws Exception {
		Integer creatorId = 6;
		String userid = "6";

		List<User> userList = new ArrayList<>();
		userList.add(user);

		when(userRepo.findOne(Mockito.anyInt())).thenReturn(creator);
		when(userRepo.findByUsername(Mockito.anyString())).thenReturn(null);
		service.updateBoUser(creatorId, getUpdateRequestData(), userid);
	}

	@Test(expected = CommonException.class)
	public void failedUpdateBoUser_FunctionIdNull() throws Exception {
		Integer creatorId = 6;
		String userid = "6";
		UserUpdateRequestVo request = getUpdateRequestData();
		request.setFunctionId(null);

		List<User> userList = new ArrayList<>();
		userList.add(user);

		when(userRepo.findOne(Mockito.anyInt())).thenReturn(user);
		when(userRepo.findByUsername(Mockito.anyString())).thenReturn(userList);
		UserCreateResponseVo responseVo = (UserCreateResponseVo) service.updateBoUser(creatorId, request, userid);
	}

	@Test(expected = CommonException.class)
	public void failedUpdateBoUser_ConfigFunctionNull() throws Exception {
		Integer creatorId = 6;
		String userid = "6";
		UserUpdateRequestVo request = getUpdateRequestData();

		List<User> userList = new ArrayList<>();
		userList.add(user);

		when(userRepo.findOne(Mockito.anyInt())).thenReturn(user);
		when(userRepo.findByUsername(Mockito.anyString())).thenReturn(userList);
		when(configFunctionRepo.findOne(Mockito.anyInt())).thenReturn(null);
		UserCreateResponseVo responseVo = (UserCreateResponseVo) service.updateBoUser(creatorId, request, userid);
	}

	@Test(expected = CommonException.class)
	public void failedUpdateBoUser_InputNull() throws Exception {
		Integer creatorId = 6;
		String userid = "6";
		UserUpdateRequestVo request = getUpdateRequestData();
		request.setInput(null);

		List<User> userList = new ArrayList<>();
		userList.add(user);

		when(userRepo.findOne(Mockito.anyInt())).thenReturn(user);
		when(userRepo.findByUsername(Mockito.anyString())).thenReturn(userList);
		when(configFunctionRepo.findOne(Mockito.anyInt())).thenReturn(configFunction);
		UserCreateResponseVo responseVo = (UserCreateResponseVo) service.updateBoUser(creatorId, request, userid);
	}

	@Test(expected = CommonException.class)
	public void failedUpdateBoUser_InputDepartmentIdNull() throws Exception {
		Integer creatorId = 6;
		String userid = "6";
		UserUpdateRequestVo request = getUpdateRequestData();
		request.getInput().setDepartmentId(null);

		List<User> userList = new ArrayList<>();
		userList.add(user);

		when(userRepo.findOne(Mockito.anyInt())).thenReturn(user);
		when(userRepo.findByUsername(Mockito.anyString())).thenReturn(userList);
		when(configFunctionRepo.findOne(Mockito.anyInt())).thenReturn(configFunction);
		when(userRepo.saveAndFlush(Mockito.anyObject())).thenReturn(user);
		UserCreateResponseVo responseVo = (UserCreateResponseVo) service.updateBoUser(creatorId, request, userid);
	}

	@Test(expected = CommonException.class)
	public void failedUpdateBoUser_DepartmentIdNotFound() throws Exception {
		Integer creatorId = 6;
		String userid = "6";
		UserUpdateRequestVo request = getUpdateRequestData();

		List<User> userList = new ArrayList<>();
		userList.add(user);

		when(userRepo.findOne(Mockito.anyInt())).thenReturn(user);
		when(userRepo.findByUsername(Mockito.anyString())).thenReturn(userList);
		when(configFunctionRepo.findOne(Mockito.anyInt())).thenReturn(configFunction);
		when(departmentRepo.findOne(request.getInput().getDepartmentId())).thenReturn(null);
		UserCreateResponseVo responseVo = (UserCreateResponseVo) service.updateBoUser(creatorId, request, userid);
	}

	@Test(expected = CommonException.class)
	public void failedUpdateBoUser() throws Exception {
		Integer creatorId = 6;
		String userid = "6";

		when(userRepo.findOne(Mockito.anyInt())).thenReturn(null);
		when(configFunctionRepo.findOne(1)).thenReturn(configFunction);
		when(departmentRepo.findOne(1)).thenReturn(department);
		when(usergroupRepo.findOne(1)).thenReturn(usergroup);
		when(boUmApprovalUserRepo.findAllByApprovalIdAndLockingId(1, "leeyk")).thenReturn(umApproval);

		UserCreateResponseVo responseVo = (UserCreateResponseVo) service.updateBoUser(creatorId, getUpdateRequestData(),
				userid);
	}

	@Test
	public void successDeleteBoUser() throws Exception {
		Integer creatorId = 6;
		Integer userid = 7;

		List<User> userList = new ArrayList<>();
		userList.add(user);

		when(userRepo.findOne(Mockito.anyInt())).thenReturn(user);
		when(userRepo.findByUsername(Mockito.anyString())).thenReturn(userList);
		when(configFunctionRepo.findOne(1)).thenReturn(configFunction);
		when(departmentRepo.findOne(1)).thenReturn(department);
		when(userRepo.saveAndFlush(Mockito.anyObject())).thenReturn(user);
		when(usergroupRepo.findOne(Mockito.anyInt())).thenReturn(usergroup);
		when(boUmApprovalUserRepo.findAllByApprovalIdAndLockingId(1, "leeyk")).thenReturn(umApproval);
		UserCreateResponseVo responseVo = (UserCreateResponseVo) service.deleteBoUser(creatorId, getDeleteRequestData(),
				userid);

	}

	@Test(expected = CommonException.class)
	public void failedDeleteBoUser_CreatorNotExisted() throws Exception {
		Integer creatorId = 2;
		Integer userid = 7;
		UserDeleteRequestVo request = getDeleteRequestData();

		when(userRepo.findOne(creatorId)).thenReturn(null);
		UserCreateResponseVo responseVo = (UserCreateResponseVo) service.deleteBoUser(creatorId, request, userid);
	}

	@Test(expected = CommonException.class)
	public void failedDeleteBoUser_DeleteUserNotExisted() throws Exception {
		Integer creatorId = 2;
		Integer userid = 7;
		UserDeleteRequestVo request = getDeleteRequestData();

		when(userRepo.findOne(creatorId)).thenReturn(creator);
		when(userRepo.findOne(userid)).thenReturn(null);
		UserCreateResponseVo responseVo = (UserCreateResponseVo) service.deleteBoUser(creatorId, request, userid);
	}

	@Test
	public void successDeleteBoUser_isApprovalRequiredFalse_UserUsergroupNotNull() throws Exception {
		Integer creatorId = 2;
		Integer userid = 7;
		UserDeleteRequestVo request = getDeleteRequestData();

		List<User> userList = new ArrayList<>();
		userList.add(user);

		when(userRepo.findOne(Mockito.anyInt())).thenReturn(creator);
		when(userRepo.findByUsername(Mockito.anyString())).thenReturn(userList);
		when(configFunctionRepo.findOne(request.getFunctionId())).thenReturn(configFunction);
		configFunction.setApprovalRequired(Boolean.FALSE);
		when(userUserGroupRepo.findAllByUserId(Mockito.anyInt())).thenReturn(userUserGroupInDBList);
		when(userUserGroupRepo.saveAndFlush(Mockito.anyObject())).thenReturn(userUserGroup);
		UserCreateResponseVo responseVo = (UserCreateResponseVo) service.deleteBoUser(creatorId, request, userid);
	}

	@Test(expected = CommonException.class)
	public void failedDeleteBoUser_FunctionIdNull() throws Exception {
		Integer creatorId = 6;
		Integer userid = 7;
		UserDeleteRequestVo request = getDeleteRequestData();
		request.setFunctionId(null);

		List<User> userList = new ArrayList<>();
		userList.add(user);

		when(userRepo.findOne(Mockito.anyInt())).thenReturn(creator);
		when(userRepo.findByUsername(Mockito.anyString())).thenReturn(userList);
		UserCreateResponseVo responseVo = (UserCreateResponseVo) service.deleteBoUser(creatorId, request, userid);
	}

	@Test(expected = CommonException.class)
	public void failedDeleteBoUser_ConfigFunctionNull() throws Exception {
		Integer creatorId = 6;
		Integer userid = 7;
		UserDeleteRequestVo request = getDeleteRequestData();

		List<User> userList = new ArrayList<>();
		userList.add(user);

		when(userRepo.findOne(Mockito.anyInt())).thenReturn(creator);
		when(userRepo.findByUsername(Mockito.anyString())).thenReturn(userList);
		when(configFunctionRepo.findOne(Mockito.anyInt())).thenReturn(null);
		UserCreateResponseVo responseVo = (UserCreateResponseVo) service.deleteBoUser(creatorId, request, userid);
	}

	@Test
	public void failedDeleteBoUser_isApprovalRequiredTrue() throws Exception {
		Integer creatorId = 6;
		Integer userid = 7;
		UserDeleteRequestVo request = getDeleteRequestData();

		List<User> userList = new ArrayList<>();
		userList.add(user);

		when(userRepo.findOne(Mockito.anyInt())).thenReturn(creator);
		when(userRepo.findByUsername(Mockito.anyString())).thenReturn(userList);
		when(configFunctionRepo.findOne(request.getFunctionId())).thenReturn(configFunction);
		configFunction.setApprovalRequired(true);
		when(boUserApprovalRepo.findAllByFunctionIdAndStatus(request.getFunctionId(), "P")).thenReturn(approvalList);
		when(boUmApprovalUserRepo.findAllByApprovalIdAndLockingId(1, request.getUsername())).thenReturn(umApproval);
		when(boUserApprovalRepo.saveAndFlush(Mockito.anyObject())).thenReturn(boUserApproval);
		when(boUmApprovalUserRepo.saveAndFlush(Mockito.anyObject())).thenReturn(umApproval);

		UserCreateResponseVo responseVo = (UserCreateResponseVo) service.deleteBoUser(creatorId, request, userid);
	}

	@Test(expected = CommonException.class)
	public void failedDeleteBoUser_isApprovalRequiredTrue_UMApprovalListNull() throws Exception {
		Integer creatorId = 6;
		Integer userid = 7;
		UserDeleteRequestVo request = getDeleteRequestData();

		List<User> userList = new ArrayList<>();
		userList.add(user);

		when(userRepo.findOne(Mockito.anyInt())).thenReturn(creator);
		when(userRepo.findByUsername(Mockito.anyString())).thenReturn(userList);
		when(configFunctionRepo.findOne(request.getFunctionId())).thenReturn(configFunction);
		configFunction.setApprovalRequired(true);
		when(boUserApprovalRepo.findAllByFunctionIdAndStatus(request.getFunctionId(), "P")).thenReturn(approvalList);
		when(boUmApprovalUserRepo.findAllByApprovalIdAndLockingId(Mockito.anyInt(), Mockito.anyString()))
				.thenReturn(umApproval);
		when(boUserApprovalRepo.saveAndFlush(Mockito.anyObject())).thenReturn(boUserApproval);
		when(boUmApprovalUserRepo.saveAndFlush(Mockito.anyObject())).thenReturn(umApproval);

		UserCreateResponseVo responseVo = (UserCreateResponseVo) service.deleteBoUser(creatorId, request, userid);
	}

	@Test(expected = CommonException.class)
	public void failedDeleteBoUser() throws Exception {
		Integer creatorId = 6;
		Integer userid = 7;

		when(userRepo.findOne(Mockito.anyInt())).thenReturn(null);
		when(configFunctionRepo.findOne(1)).thenReturn(configFunction);
		when(departmentRepo.findOne(1)).thenReturn(department);
		when(usergroupRepo.findOne(1)).thenReturn(usergroup);
		when(boUmApprovalUserRepo.findAllByApprovalIdAndLockingId(1, "leeyk")).thenReturn(umApproval);

		UserCreateResponseVo responseVo = (UserCreateResponseVo) service.deleteBoUser(creatorId, getDeleteRequestData(),
				userid);
	}
}
