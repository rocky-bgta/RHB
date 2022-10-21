package com.rhbgroup.dcpbo.user.create;

import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.netflix.feign.FeignContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import com.rhbgroup.dcp.util.JsonUtil;
import com.rhbgroup.dcpbo.common.exception.CommonException;
import com.rhbgroup.dcpbo.common.exception.CommonExceptionAdvice;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest(classes = { UserFunctionControllerTest.class, UserFunctionController.class })
@EnableWebMvc
public class UserFunctionControllerTest {

	private static Logger logger = LogManager.getLogger(UserFunctionControllerTest.class);

	@Autowired
	MockMvc mockMvc;

	@MockBean
	FeignContext feignContext;

	@MockBean
	CommonExceptionAdvice commonExceptionAdvice;

	@MockBean(name = "userFunctionService")
	private UserFunctionService userFunctionService;

	UserCreateResponseVo response;
	UserCreateRequestVo createRequest;
	UserUpdateRequestVo updateRequest;
	UserDeleteRequestVo deleteRequest;
	UserFunctionDepartmentVo departmentVo;
	List<UserFunctionUserGroupVo> usergroupList;

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

	private UserCreateRequestVo getCreateRequestData() {
		createRequest = new UserCreateRequestVo();
		createRequest.setFunctionId(1);
		createRequest.setUsername("450569");
		createRequest.setEmail("lee.yaw.khang@rhbgroup.com");
		createRequest.setName("Lee YK");

		departmentVo = new UserFunctionDepartmentVo();
		departmentVo.setDepartmentId(1);
		departmentVo.setDepartmentName("Administrators");
		createRequest.setDepartment(departmentVo);

		usergroupList = new ArrayList<UserFunctionUserGroupVo>();
		UserFunctionUserGroupVo group = new UserFunctionUserGroupVo();
		group.setGroupId(1);
		group.setGroupName("Admin Maker");
		usergroupList.add(group);

		group = new UserFunctionUserGroupVo();
		group.setGroupId(3);
		group.setGroupName("Admin Viewer");
		usergroupList.add(group);
		createRequest.setUsergroup(usergroupList);

		return this.createRequest;
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
	public void successCreateBoUser() throws Exception {
		UserCreateResponseVo response = givenResult();
		BDDMockito.given(userFunctionService.createBoUser(Mockito.anyInt(), Mockito.anyObject())).willReturn(response);

		this.response = givenResult();

		this.mockMvc.perform(MockMvcRequestBuilders.post("/bo/user").header("userid", "6")
				.contentType(MediaType.APPLICATION_JSON_UTF8_VALUE).content(asJsonString(getCreateRequestData())))
				.andExpect(MockMvcResultMatchers.jsonPath("approvalId", Matchers.is(30)))
				.andExpect(MockMvcResultMatchers.jsonPath("isWritten", Matchers.is("N")));

	}

	@Test
	public void failedCreateBoUser() throws Exception {
		String creatorId = "6";

		when(userFunctionService.createBoUser(6, getCreateRequestData())).thenThrow(new CommonException());

		mockMvc.perform(MockMvcRequestBuilders.post("/bo/user").header("userid", creatorId))
				.andDo(MockMvcResultHandlers.print()).andExpect(MockMvcResultMatchers.status().is4xxClientError());
	}

	public static String asJsonString(final Object obj) {
		return JsonUtil.objectToJson(obj);
	}

	@Test
	public void successUpdateBoUser() throws Exception {
		UserCreateResponseVo response = givenResult();
		BDDMockito.given(userFunctionService.updateBoUser(Mockito.anyInt(), Mockito.anyObject(), Mockito.anyString()))
				.willReturn(response);

		this.response = givenResult();

		String userid = "6";
		this.mockMvc.perform(MockMvcRequestBuilders.put("/bo/user/" + userid + "/update").header("creatorId", "6").header("userid",6)
				.contentType(MediaType.APPLICATION_JSON_UTF8_VALUE).content(asJsonString(getUpdateRequestData())))
				.andExpect(MockMvcResultMatchers.jsonPath("approvalId", Matchers.is(30)))
				.andExpect(MockMvcResultMatchers.jsonPath("isWritten", Matchers.is("N")));

	}

	@Test
	public void failedUpdateBoUser() throws Exception {
		String creatorId = "6";
		String userid = "dcpbo6";

		when(userFunctionService.updateBoUser(6, getUpdateRequestData(), userid)).thenThrow(new CommonException());

		mockMvc.perform(MockMvcRequestBuilders.put("/bo/user/" + userid + "/update").header("creatorId", creatorId))
				.andDo(MockMvcResultHandlers.print()).andExpect(MockMvcResultMatchers.status().is4xxClientError());
	}

	@Test
	public void successDeleteBoUser() throws Exception {
		UserCreateResponseVo response = givenResult();
		BDDMockito.given(userFunctionService.deleteBoUser(Mockito.anyInt(), Mockito.anyObject(), Mockito.anyInt()))
				.willReturn(response);

		this.response = givenResult();

		Integer userid = 7;
		this.mockMvc.perform(MockMvcRequestBuilders.put("/bo/user/" + userid + "/delete").header("userid", "6")
				.contentType(MediaType.APPLICATION_JSON_UTF8_VALUE).content(asJsonString(getDeleteRequestData())))
				.andExpect(MockMvcResultMatchers.jsonPath("approvalId", Matchers.is(30)))
				.andExpect(MockMvcResultMatchers.jsonPath("isWritten", Matchers.is("N")));

	}
	
	@Test
	public void failedDeleteBoUser() throws Exception {
		Integer creatorId = 6;
		Integer userid = 7;

		when(userFunctionService.deleteBoUser(creatorId, getDeleteRequestData(), userid)).thenThrow(new CommonException());

		mockMvc.perform(MockMvcRequestBuilders.put("/bo/user/" + userid + "/delete").header("userid", creatorId))
				.andDo(MockMvcResultHandlers.print()).andExpect(MockMvcResultMatchers.status().is4xxClientError());
	}
}
