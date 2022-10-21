package com.rhbgroup.dcpbo.user.usergroup;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.rhbgroup.dcpbo.common.audit.AdditionalDataHolder;
import com.rhbgroup.dcpbo.common.exception.CommonException;
import com.rhbgroup.dcpbo.user.common.*;
import com.rhbgroup.dcpbo.user.common.model.bo.BoUmApprovalUserGroup;
import com.rhbgroup.dcpbo.user.enums.ApprovalStatus;
import com.rhbgroup.dcpbo.user.enums.MaintenanceActionType;
import com.rhbgroup.dcpbo.user.function.model.bo.Module;
import com.rhbgroup.dcpbo.user.info.model.bo.ConfigFunction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest(classes = {
		UsergroupAddService.class,
		UsergroupAddServiceTest.class,
		CommonException.class,
		AdditionalDataHolder.class
})
@EnableWebMvc
public class UsergroupAddServiceTest {
	@Autowired
	MockMvc mockMvc;

	String successResponse = "";
	String emptyUserGroup = "{\"module\":null}";

	@Autowired
	UsergroupAddService usergroupAddService;
	@MockBean
	ConfigFunctionRepository configFunctionRepository;
	@MockBean
	ApprovalRepository approvalRepository;
	@MockBean
	BoUmApprovalUsergroupRepository boUmApprovalUsergroupRepository;
	@MockBean
	UserGroupRepository userGroupRepository;
	@MockBean
	UsergroupAccessRepository usergroupAccessRepository;
	@MockBean
	UserRepository userRepository;
	@MockBean
	BoAuditEventConfigRepository boAuditEventConfigRepository;

	@Autowired
	private WebApplicationContext context;


	private static Logger logger = LogManager.getLogger(UsergroupAddServiceTest.class);

	private static final String userId = "2";

	private static final String successStringNoApproval ="{\"approvalId\":\"0\",\"isWritten\":\"Y\"}";
	private static final String successStringApproval ="{\"approvalId\":\"2\",\"isWritten\":\"N\"}";

	private static final String requestBodyMaker = "{\n" +
			"    \"functionId\": 2,\n" +
			"    \"groupName\": \"User Admin Test 17\",\n" +
			"    \"function\": [{\n" +
			"        \"functionId\": 1,\n" +
			"        \"functionName\": \"User\"\n" +
			"    }],\n" +
			"    \"accessType\": \"M\"\n" +
			"}";
	private static final String requestBodyMakerNullGroupName = "{\n" +
			"    \"functionId\": 2,\n" +
			"    \"function\": [{\n" +
			"        \"functionId\": 1,\n" +
			"        \"functionName\": \"User\"\n" +
			"    }],\n" +
			"    \"accessType\": \"M\"\n" +
			"}";
	private static final String requestBodyInquirer = "{\n" +
			"    \"functionId\": 2,\n" +
			"    \"groupName\": \"User Admin Test 17\",\n" +
			"    \"function\": [{\n" +
			"        \"functionId\": 1,\n" +
			"        \"functionName\": \"User\"\n" +
			"    }],\n" +
			"    \"accessType\": \"I\"\n" +
			"}";
	private static final String requestBodyChecker = "{\n" +
			"    \"functionId\": 2,\n" +
			"    \"groupName\": \"User Admin Test 17\",\n" +
			"    \"function\": [{\n" +
			"        \"functionId\": 1,\n" +
			"        \"functionName\": \"User\"\n" +
			"    }],\n" +
			"    \"accessType\": \"C\"\n" +
			"}";

	@Test(expected = CommonException.class)
	public void getWorkflowOverviewTestNullUsergroupFail() throws Exception {
		Integer functionId = 2;
		String groupName = "User Admin Test 17";
		ConfigFunction configFunction = new ConfigFunction();
		configFunction.setId(functionId);
		configFunction.setApprovalRequired(false);
		Module module = new Module();
		module.setId(2);
		configFunction.setModule(module);
		List<Integer> integerList = new ArrayList<>();
		List<Integer> integerList2 = new ArrayList<>();
		integerList.add(2);
		integerList2.add(2);


		BoUmApprovalUserGroup boUmApprovalUserGroup = new BoUmApprovalUserGroup();
		List<BoUmApprovalUserGroup> boUmApprovalUserGroupList = new ArrayList<>();
		boUmApprovalUserGroupList.add(boUmApprovalUserGroup);

		Date now = new Date();
		Timestamp timeNow =  new Timestamp(now.getTime());

		when(userGroupRepository.insert(eq(groupName), eq("A"), any(), eq(userId), any(), eq(userId))).thenReturn(integerList2);
		when(boUmApprovalUsergroupRepository.findByApprovalIdAndLockingId(integerList,groupName)).thenReturn(boUmApprovalUserGroupList);
		when(configFunctionRepository.findOne(2)).thenReturn(configFunction);
		when(approvalRepository.findIdByFunctionIdAndStatus((int)(functionId),"P")).thenReturn(integerList);
		when(userGroupRepository.findCountByGroupNameAndGroupStatus("User Admin Test 17",UsergroupStatus.ACTIVE.getValue())).thenReturn(2);

		ObjectMapper mapper = new ObjectMapper();
		Gson gson = new Gson();
		assertEquals(mapper.writeValueAsString(gson.fromJson(successStringNoApproval,Usergroup.class)),mapper.writeValueAsString(usergroupAddService.postUsergroupService(mapper.readValue(requestBodyMakerNullGroupName,UsergroupRequestBody.class),userId)));
	}

	@Test(expected = CommonException.class)
	public void getWorkflowOverviewTestDuplicateFail() throws Exception {
		Integer functionId = 2;
		String groupName = "User Admin Test 17";
		ConfigFunction configFunction = new ConfigFunction();
		configFunction.setId(functionId);
		configFunction.setApprovalRequired(false);
		Module module = new Module();
		module.setId(2);
		configFunction.setModule(module);
		List<Integer> integerList = new ArrayList<>();
		List<Integer> integerList2 = new ArrayList<>();
		integerList.add(2);
		integerList2.add(2);


		BoUmApprovalUserGroup boUmApprovalUserGroup = new BoUmApprovalUserGroup();
		List<BoUmApprovalUserGroup> boUmApprovalUserGroupList = new ArrayList<>();
		boUmApprovalUserGroupList.add(boUmApprovalUserGroup);

		Date now = new Date();
		Timestamp timeNow =  new Timestamp(now.getTime());

		when(userGroupRepository.insert(eq(groupName), eq("A"), any(), eq(userId), any(), eq(userId))).thenReturn(integerList2);
		when(boUmApprovalUsergroupRepository.findByApprovalIdAndLockingId(integerList,groupName)).thenReturn(boUmApprovalUserGroupList);
		when(configFunctionRepository.findOne(2)).thenReturn(configFunction);
		when(approvalRepository.findIdByFunctionIdAndStatus((int)(functionId),"P")).thenReturn(integerList);
		when(userGroupRepository.findCountByGroupNameAndGroupStatus("User Admin Test 17",UsergroupStatus.ACTIVE.getValue())).thenReturn(2);

		ObjectMapper mapper = new ObjectMapper();
		Gson gson = new Gson();
		assertEquals(mapper.writeValueAsString(gson.fromJson(successStringNoApproval,Usergroup.class)),mapper.writeValueAsString(usergroupAddService.postUsergroupService(mapper.readValue(requestBodyMaker,UsergroupRequestBody.class),userId)));
	}

	@Test
	public void getWorkflowOverviewTestSuccessNoApproval() throws Exception {
		Integer functionId = 2;
		String groupName = "User Admin Test 17";
		ConfigFunction configFunction = new ConfigFunction();
		configFunction.setId(functionId);
		configFunction.setApprovalRequired(false);
		Module module = new Module();
		module.setId(2);
		configFunction.setModule(module);
		List<Integer> integerList = new ArrayList<>();
		List<Integer> integerList2 = new ArrayList<>();
		integerList.add(2);
		integerList2.add(2);


		BoUmApprovalUserGroup boUmApprovalUserGroup = new BoUmApprovalUserGroup();
		List<BoUmApprovalUserGroup> boUmApprovalUserGroupList = new ArrayList<>();
		boUmApprovalUserGroupList.add(boUmApprovalUserGroup);

		Date now = new Date();
		Timestamp timeNow =  new Timestamp(now.getTime());

		when(userGroupRepository.insert(eq(groupName), eq("A"), any(), eq(userId), any(), eq(userId))).thenReturn(integerList2);
		when(boUmApprovalUsergroupRepository.findByApprovalIdAndLockingId(integerList,groupName)).thenReturn(boUmApprovalUserGroupList);
		when(configFunctionRepository.findOne(2)).thenReturn(configFunction);
		when(approvalRepository.findIdByFunctionIdAndStatus((int)(functionId),"P")).thenReturn(integerList);
		when(userRepository.findNameById(Integer.valueOf(userId))).thenReturn("Admin");
		when(approvalRepository.insert(any(),any(),any(),any(),any(),any(),any(),any(),any())).thenReturn(integerList);
		when(userGroupRepository.insert(any(),any(),any(),any(),any(),any())).thenReturn(integerList2);


		ObjectMapper mapper = new ObjectMapper();
		Gson gson = new Gson();
		assertEquals(mapper.writeValueAsString(gson.fromJson(successStringNoApproval,Usergroup.class)),mapper.writeValueAsString(usergroupAddService.postUsergroupService(mapper.readValue(requestBodyMaker,UsergroupRequestBody.class),userId)));
	}

	@Test
	public void usergroupAddServiceTestSuccessInquirerApproval() throws Exception {
		Integer functionId = 2;
		String groupName = "User Admin Test 17";
		ConfigFunction configFunction = new ConfigFunction();
		configFunction.setId(functionId);
		configFunction.setApprovalRequired(true);
		Module module = new Module();
		module.setId(2);
		configFunction.setModule(module);
		List<Integer> integerList = new ArrayList<>();
		List<Integer> integerList2 = new ArrayList<>();
		integerList.add(2);
		integerList2.add(2);


		BoUmApprovalUserGroup boUmApprovalUserGroup = new BoUmApprovalUserGroup();
		List<BoUmApprovalUserGroup> boUmApprovalUserGroupList = new ArrayList<>();

		Date now = new Date();
		Timestamp timeNow =  new Timestamp(now.getTime());

		when(approvalRepository.insert(eq(functionId), eq(Integer.valueOf(userId)), anyString(),anyString(),eq("P"), any(), eq(userId), any(), eq(userId))).thenReturn(integerList);

		when(userGroupRepository.insert(eq(groupName), eq("A"), any(), eq(userId), any(), eq(userId))).thenReturn(integerList2);
		when(boUmApprovalUsergroupRepository.findByApprovalIdAndLockingId(anyList(),any())).thenReturn(boUmApprovalUserGroupList);
		when(configFunctionRepository.findOne(2)).thenReturn(configFunction);
		when(approvalRepository.findIdByFunctionIdAndStatus((int)(functionId),"P")).thenReturn(integerList);
		when(userRepository.findNameById(Integer.valueOf(userId))).thenReturn("Admin");
		when(approvalRepository.insert(any(),any(),any(),any(),any(),any(),any(),any(),any())).thenReturn(integerList);


		ObjectMapper mapper = new ObjectMapper();
		Gson gson = new Gson();
		assertEquals(mapper.writeValueAsString(gson.fromJson(successStringApproval,Usergroup.class)),mapper.writeValueAsString(usergroupAddService.postUsergroupService(mapper.readValue(requestBodyInquirer,UsergroupRequestBody.class),userId)));
	}

	@Test
	public void usergroupAddServiceTestSuccessCheckerApproval() throws Exception {
		Integer functionId = 2;
		String groupName = "User Admin Test 17";
		ConfigFunction configFunction = new ConfigFunction();
		configFunction.setId(functionId);
		configFunction.setApprovalRequired(true);
		Module module = new Module();
		module.setId(2);
		configFunction.setModule(module);
		List<Integer> integerList = new ArrayList<>();
		List<Integer> integerList2 = new ArrayList<>();
		integerList.add(2);
		integerList2.add(2);


		BoUmApprovalUserGroup boUmApprovalUserGroup = new BoUmApprovalUserGroup();
		List<BoUmApprovalUserGroup> boUmApprovalUserGroupList = new ArrayList<>();

		Date now = new Date();
		Timestamp timeNow =  new Timestamp(now.getTime());

		when(approvalRepository.insert(eq(functionId), eq(Integer.valueOf(userId)), anyString(),anyString(),eq("P"), any(), eq(userId), any(), eq(userId))).thenReturn(integerList);

		when(userGroupRepository.insert(eq(groupName), eq("A"), any(), eq(userId), any(), eq(userId))).thenReturn(integerList2);
		when(boUmApprovalUsergroupRepository.findByApprovalIdAndLockingId(anyList(),any())).thenReturn(boUmApprovalUserGroupList);
		when(configFunctionRepository.findOne(2)).thenReturn(configFunction);
		when(approvalRepository.findIdByFunctionIdAndStatus((int)(functionId),"P")).thenReturn(integerList);
		when(userRepository.findNameById(Integer.valueOf(userId))).thenReturn("Admin");
		when(approvalRepository.insert(any(),any(),any(),any(),any(),any(),any(),any(),any())).thenReturn(integerList);

		ObjectMapper mapper = new ObjectMapper();
		Gson gson = new Gson();
		assertEquals(mapper.writeValueAsString(gson.fromJson(successStringApproval,Usergroup.class)),mapper.writeValueAsString(usergroupAddService.postUsergroupService(mapper.readValue(requestBodyChecker,UsergroupRequestBody.class),userId)));
	}

	@Test
	public void usergroupAddServiceTestSuccessMakerApproval() throws Exception {
		Integer functionId = 2;
		String groupName = "User Admin Test 17";
		ConfigFunction configFunction = new ConfigFunction();
		configFunction.setId(functionId);
		configFunction.setApprovalRequired(true);
		Module module = new Module();
		module.setId(2);
		configFunction.setModule(module);
		List<Integer> integerList = new ArrayList<>();
		List<Integer> integerList2 = new ArrayList<>();
		integerList.add(2);
		integerList2.add(2);


		BoUmApprovalUserGroup boUmApprovalUserGroup = new BoUmApprovalUserGroup();
		List<BoUmApprovalUserGroup> boUmApprovalUserGroupList = new ArrayList<>();

		Date now = new Date();
		Timestamp timeNow =  new Timestamp(now.getTime());

		when(approvalRepository.insert(eq(functionId), eq(Integer.valueOf(userId)), anyString(),anyString(),eq("P"), any(), eq(userId), any(), eq(userId))).thenReturn(integerList);

		when(userGroupRepository.insert(eq(groupName), eq("A"), any(), eq(userId), any(), eq(userId))).thenReturn(integerList2);
		when(boUmApprovalUsergroupRepository.findByApprovalIdAndLockingId(anyList(),any())).thenReturn(boUmApprovalUserGroupList);
		when(configFunctionRepository.findOne(2)).thenReturn(configFunction);
		when(approvalRepository.findIdByFunctionIdAndStatus((int)(functionId),"P")).thenReturn(integerList);
		when(userRepository.findNameById(Integer.valueOf(userId))).thenReturn("Admin");
		when(approvalRepository.insert(any(),any(),any(),any(),any(),any(),any(),any(),any())).thenReturn(integerList);

		ObjectMapper mapper = new ObjectMapper();
		Gson gson = new Gson();
		assertEquals(mapper.writeValueAsString(gson.fromJson(successStringApproval,Usergroup.class)),mapper.writeValueAsString(usergroupAddService.postUsergroupService(mapper.readValue(requestBodyMaker,UsergroupRequestBody.class),userId)));
	}

	@Test(expected = CommonException.class)
	public void usergroupAddServiceTestFailDuplicateGroupName() throws Exception {
		Integer functionId = 2;
		String groupName = "User Admin Test 17";
		ConfigFunction configFunction = new ConfigFunction();
		configFunction.setId(functionId);
		configFunction.setApprovalRequired(true);
		Module module = new Module();
		module.setId(2);
		configFunction.setModule(module);
		List<Integer> integerList = new ArrayList<>();
		List<Integer> integerList2 = new ArrayList<>();
		integerList.add(2);
		integerList2.add(2);


		BoUmApprovalUserGroup boUmApprovalUserGroup = new BoUmApprovalUserGroup();
		List<BoUmApprovalUserGroup> boUmApprovalUserGroupList = new ArrayList<>();
		boUmApprovalUserGroupList.add(boUmApprovalUserGroup);
		Date now = new Date();
		Timestamp timeNow =  new Timestamp(now.getTime());

		when(approvalRepository.insert(eq(functionId), eq(Integer.valueOf(userId)), anyString(),anyString(),eq("P"), any(), eq(userId), any(), eq(userId))).thenReturn(integerList);

		when(userGroupRepository.insert(eq(groupName), eq("A"), any(), eq(userId), any(), eq(userId))).thenReturn(integerList2);
		when(boUmApprovalUsergroupRepository.findByApprovalIdAndLockingId(anyList(),any())).thenReturn(boUmApprovalUserGroupList);
		when(configFunctionRepository.findOne(2)).thenReturn(configFunction);
		when(approvalRepository.findIdByFunctionIdAndStatus((int)(functionId),"P")).thenReturn(integerList);

		ObjectMapper mapper = new ObjectMapper();
		Gson gson = new Gson();
		assertEquals(mapper.writeValueAsString(gson.fromJson(successStringApproval,Usergroup.class)),mapper.writeValueAsString(usergroupAddService.postUsergroupService(mapper.readValue(requestBodyMaker,UsergroupRequestBody.class),userId)));
	}

}
