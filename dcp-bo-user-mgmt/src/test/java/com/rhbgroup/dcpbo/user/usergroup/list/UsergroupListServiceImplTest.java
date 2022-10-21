package com.rhbgroup.dcpbo.user.usergroup.list;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import com.rhbgroup.dcpbo.common.exception.CommonException;
import com.rhbgroup.dcpbo.user.common.UserGroupRepository;
import com.rhbgroup.dcpbo.user.common.model.bo.Usergroup;
import com.rhbgroup.dcpbo.user.usergroup.list.dto.UsergroupListVo;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
		UsergroupListService.class,
		UsergroupListServiceImpl.class,
		UserGroupRepository.class
})
public class UsergroupListServiceImplTest {

	private static Logger logger = LogManager.getLogger(UsergroupListServiceImplTest.class);

	@Autowired
	UsergroupListService usergroupListService;

	@MockBean
	UserGroupRepository userGroupRepositoryMock;

	@Test
	public void getUsergroupListTest() {
		logger.debug("getUsergroupListTest()");

		Usergroup usergroup1 = new Usergroup();
		usergroup1.setId(1);
		usergroup1.setGroupName("Admin Maker");

		Usergroup usergroup2 = new Usergroup();
		usergroup2.setId(2);
		usergroup2.setGroupName("Admin Approver");

		Usergroup usergroup3 = new Usergroup();
		usergroup3.setId(3);
		usergroup3.setGroupName("Call Center Approver");

		List<Usergroup> usergroups = new ArrayList<>();
		usergroups.add(usergroup1);
		usergroups.add(usergroup2);
		usergroups.add(usergroup3);

		when(userGroupRepositoryMock.findDistinctByGroupNameContaining(Mockito.anyString())).thenReturn(usergroups);

		UsergroupListVo actualResult = (UsergroupListVo) usergroupListService.getUsergroupList("Admin");

		assertNotNull(actualResult);
		assertEquals(3, actualResult.getUsergroup().size());

		assertEquals(usergroup1.getId().intValue(), actualResult.getUsergroup().get(0).getGroupId());
		assertEquals(usergroup1.getGroupName(), actualResult.getUsergroup().get(0).getGroupName());

		assertEquals(usergroup2.getId().intValue(), actualResult.getUsergroup().get(1).getGroupId());
		assertEquals(usergroup2.getGroupName(), actualResult.getUsergroup().get(1).getGroupName());

		assertEquals(usergroup3.getId().intValue(), actualResult.getUsergroup().get(2).getGroupId());
		assertEquals(usergroup3.getGroupName(), actualResult.getUsergroup().get(2).getGroupName());
	}

	@Test(expected = CommonException.class)
	public void getUsergroupListTest_EmptyResult() {
		logger.debug("getUsergroupListTest_EmptyResult()");
		List<Usergroup> usergroups = new ArrayList<>();
		when(userGroupRepositoryMock.findDistinctByGroupNameContaining(Mockito.anyString())).thenReturn(usergroups);

		usergroupListService.getUsergroupList("clerk");
	}

	@Test(expected = CommonException.class)
	public void getUsergroupListTest_GeneralException() {
		logger.debug("getUsergroupListTest_GeneralException()");
		when(userGroupRepositoryMock.findDistinctByGroupNameContaining(Mockito.anyString())).thenReturn(null);

		usergroupListService.getUsergroupList("clerk");
	}
}
