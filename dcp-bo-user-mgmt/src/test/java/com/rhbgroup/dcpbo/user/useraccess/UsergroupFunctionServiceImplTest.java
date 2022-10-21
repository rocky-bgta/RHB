package com.rhbgroup.dcpbo.user.useraccess;

import com.rhbgroup.dcpbo.common.audit.AdditionalDataHolder;
import com.rhbgroup.dcpbo.user.common.*;
import com.rhbgroup.dcpbo.user.common.model.bo.Usergroup;
import com.rhbgroup.dcpbo.user.common.model.bo.UsergroupAccess;
import com.rhbgroup.dcpbo.user.info.model.bo.ConfigFunction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
		UsergroupFunctionServiceImplTest.class,
		UsergroupFunctionServiceImpl.class,
		UsergroupFunctionService.class,
		AdditionalDataHolder.class,
		BoRepositoryHelper.class
})
public class UsergroupFunctionServiceImplTest {

	private static Logger logger = LogManager.getLogger(UsergroupFunctionServiceImplTest.class);

	@Autowired
	UsergroupFunctionService usergroupFunctionService;

	@MockBean
	UserGroupRepository userGroupRepositoryMock;

	@MockBean
	UsergroupAccessRepository usergroupAccessRepositoryMock;

	@MockBean
	ConfigFunctionRepository configFunctionRepositoryMock;

	@MockBean
	UserRepository userRepositoryMock;

	@Autowired
	BoRepositoryHelper boRepositoryHelper = new BoRepositoryHelper();

	private final Integer pageNum = 1;
	private final String keyword = "ibk";
	private final String accessType = "I";
	private final String functionId = "1";
	private final int usergroupId = 8;
	private final Integer funcId = 1;
	List<Integer> usergroupIdList = Arrays.asList(8,9);

	@Test
	public void testGetUserGroupFunctionService() {
		logger.debug("testGetUserGroupFunctionService()");

		when(userGroupRepositoryMock.getUsergroupByKeyword(keyword)).thenReturn(getUsergroupList());
		when(usergroupAccessRepositoryMock.findUserGroupIdByFunctionId(Collections.singletonList(funcId))).thenReturn(usergroupIdList);
		when(usergroupAccessRepositoryMock.findUsergroupIdByAccessType(Collections.singletonList(accessType))).thenReturn(usergroupIdList);
		when(usergroupAccessRepositoryMock.findByUserGroupIdAndStatus(anyInt())).thenReturn(getUsergroupAccessList());
		when(configFunctionRepositoryMock.findOne(anyInt())).thenReturn(getConfigFunction());

		UsergroupFunction actual = usergroupFunctionService.getUserGroupFunctionService(keyword, pageNum, accessType, functionId);
	}

	@Test
	public void testGetUserGroupFunctionService_noUsergroupAccess() {
		logger.debug("testGetUserGroupFunctionService()");

		when(userGroupRepositoryMock.getUsergroupByKeyword(keyword)).thenReturn(getUsergroupList());
		when(usergroupAccessRepositoryMock.findUserGroupIdByFunctionId(Collections.singletonList(funcId))).thenReturn(usergroupIdList);
		when(usergroupAccessRepositoryMock.findUsergroupIdByAccessType(Collections.singletonList(accessType))).thenReturn(usergroupIdList);
		when(usergroupAccessRepositoryMock.findByUserGroupIdAndStatus(anyInt())).thenReturn(new ArrayList<>());
		when(configFunctionRepositoryMock.findOne(anyInt())).thenReturn(getConfigFunction());

		UsergroupFunction actual = usergroupFunctionService.getUserGroupFunctionService(keyword, pageNum, accessType, functionId);
	}

	private List<Usergroup> getUsergroupList() {
		Usergroup usergroup = new Usergroup();
		usergroup.setId(usergroupId);

		List<Usergroup> usergroupList = new ArrayList<>();
		usergroupList.add(usergroup);

		return usergroupList;
	}

	private List<Usergroup> getUsergroupListByGroupName() {
		Usergroup usergroup = new Usergroup();
		usergroup.setId(usergroupId);

		List<Usergroup> usergroupList = new ArrayList<>();
		usergroupList.add(usergroup);

		return usergroupList;
	}

	private List<UsergroupAccess> getUsergroupAccessList() {
		UsergroupAccess usergroupAccess = new UsergroupAccess();
		usergroupAccess.setFunctionId(1);
		usergroupAccess.setUserGroupId(2);
		usergroupAccess.setAccessType("AccessType");

		List<UsergroupAccess> usergroupAccessList = new ArrayList<>();
		usergroupAccessList.add(usergroupAccess);

		return usergroupAccessList;
	}

	private ConfigFunction getConfigFunction() {
		ConfigFunction configFunction = new ConfigFunction();
		configFunction.setFunctionName("User");

		return configFunction;
	}
}
