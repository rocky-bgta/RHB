package com.rhbgroup.dcpbo.user.create;

import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.netflix.feign.FeignContext;
import org.springframework.test.context.junit4.SpringRunner;

import com.rhbgroup.dcpbo.common.exception.CommonException;
import com.rhbgroup.dcpbo.common.exception.CommonExceptionAdvice;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { UserFunctionService.class, UserFunctionServiceTest.class })
public class UserFunctionServiceTest {

	@MockBean
	FeignContext feignContext;

	@MockBean
	CommonExceptionAdvice commonExceptionAdvice;

	UserCreateResponseVo response;
	UserUpdateRequestVo updateRequest;
	UserDeleteRequestVo deleteRequest;
	UserFunctionDepartmentVo departmentVo;
	List<UserFunctionUserGroupVo> usergroupList;

	@MockBean
	private UserFunctionService userFunctionService;

	@Autowired
	private UserFunctionService service;

	@Before
	public void setup() {
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
	public void createBoUserTest() {
		UserCreateResponseVo response = givenResult();
		UserCreateRequestVo request = getRequestData();
		BDDMockito.given(userFunctionService.createBoUser(Mockito.anyInt(), Mockito.anyObject())).willReturn(response);

		UserCreateResponseVo actualResponse = (UserCreateResponseVo) this.service.createBoUser(6, request);

		Assert.assertThat(actualResponse.getApprovalId(), Matchers.is(30));
		Assert.assertThat(actualResponse.getIsWritten(), Matchers.is("N"));
		BDDMockito.verify(userFunctionService, Mockito.atLeastOnce()).createBoUser(6, request);
	}

	@Test(expected = CommonException.class)
	public void createBoUserTest_Failed() throws CommonException {
		UserCreateRequestVo request = getRequestData();
		when(userFunctionService.createBoUser(Mockito.anyInt(), Mockito.anyObject())).thenThrow(new CommonException());
		service.createBoUser(6, request);
	}

	@Test
	public void successUpdateBoUser() throws Exception {
		UserCreateResponseVo response = givenResult();
		UserUpdateRequestVo request = getUpdateRequestData();
		BDDMockito.given(userFunctionService.updateBoUser(Mockito.anyInt(), Mockito.anyObject(), Mockito.anyString()))
				.willReturn(response);

		UserCreateResponseVo actualResponse = (UserCreateResponseVo) this.service.updateBoUser(6, request, "dcpbo6");

		Assert.assertThat(actualResponse.getApprovalId(), Matchers.is(30));
		Assert.assertThat(actualResponse.getIsWritten(), Matchers.is("N"));
		BDDMockito.verify(userFunctionService, Mockito.atLeastOnce()).updateBoUser(6, request, "dcpbo6");

	}

	@Test(expected = CommonException.class)
	public void failedUpdateBoUser() throws Exception {
		Integer creatorId = 6;
		String userid = "dcpbo6";

		when(userFunctionService.updateBoUser(Mockito.anyInt(), Mockito.anyObject(), Mockito.anyString()))
				.thenThrow(new CommonException());
		service.updateBoUser(creatorId, getUpdateRequestData(), userid);
	}

	@Test
	public void successDeleteBoUser() throws Exception {
		Integer creatorId = 6;
		Integer userid = 7;
		UserCreateResponseVo response = givenResult();
		UserDeleteRequestVo request = getDeleteRequestData();
		BDDMockito.given(userFunctionService.deleteBoUser(Mockito.anyInt(), Mockito.anyObject(), Mockito.anyInt()))
				.willReturn(response);

		UserCreateResponseVo actualResponse = (UserCreateResponseVo) this.service.deleteBoUser(creatorId, request,
				userid);

		Assert.assertThat(actualResponse.getApprovalId(), Matchers.is(30));
		Assert.assertThat(actualResponse.getIsWritten(), Matchers.is("N"));
		BDDMockito.verify(service, Mockito.atLeastOnce()).deleteBoUser(creatorId, request, userid);

	}

	@Test(expected = CommonException.class)
	public void failedDeleteBoUser() throws Exception {
		Integer creatorId = 6;
		Integer userid = 7;

		when(userFunctionService.deleteBoUser(Mockito.anyInt(), Mockito.anyObject(), Mockito.anyInt()))
				.thenThrow(new CommonException());
		service.deleteBoUser(creatorId, getDeleteRequestData(), userid);
	}

}
