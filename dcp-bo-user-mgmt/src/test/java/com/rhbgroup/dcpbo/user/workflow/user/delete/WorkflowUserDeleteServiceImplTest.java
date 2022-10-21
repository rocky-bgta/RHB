package com.rhbgroup.dcpbo.user.workflow.user.delete;

import com.rhbgroup.dcpbo.common.audit.AdditionalDataHolder;
import com.rhbgroup.dcpbo.common.exception.CommonException;
import com.rhbgroup.dcpbo.user.common.*;
import com.rhbgroup.dcpbo.user.common.model.bo.Approval;
import com.rhbgroup.dcpbo.user.common.model.bo.UmApprovalUser;
import com.rhbgroup.dcpbo.user.common.model.bo.UserUsergroup;
import com.rhbgroup.dcpbo.user.info.model.bo.User;
import com.rhbgroup.dcpbo.user.workflow.user.delete.dto.UserDeleteApprovalResponseVo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
		WorkflowUserDeleteService.class,
		WorkflowUserDeleteServiceImpl.class,
		ApprovalRepository.class,
		UmApprovalUserRepository.class,
		UserRepository.class,
		UserUsergroupRepository.class,
		AdditionalDataHolder.class
})
public class WorkflowUserDeleteServiceImplTest {

	private static Logger logger = LogManager.getLogger(WorkflowUserDeleteServiceImplTest.class);

	@Autowired
	WorkflowUserDeleteService workflowUserDeleteService;

	@MockBean
	ApprovalRepository approvalRepositoryMock;

	@MockBean
	UmApprovalUserRepository umApprovalUserRepositoryMock;

	@MockBean
	UserRepository userRepositoryMock;

	@MockBean
	UserUsergroupRepository userUserGroupRepoMock;

	Approval approval = new Approval();
	UmApprovalUser umApprovalUser = new UmApprovalUser();
	User user = new User();
	List<UserUsergroup> userUsergroupList = new ArrayList<>();
	UserUsergroup userUserGroup = new UserUsergroup();

	@Before
	public void setup() {

		Integer preId = 1;
		String preStatus = "P";
		String preReason = "";

		int preFunctionId = 123;
		int preCreatorId = 777;
		String preDescription = "Desc";
		String preAction = "DELETE";
		approval.setId(preId);
		approval.setFunctionId(preFunctionId);
		approval.setCreatorId(preCreatorId);
		approval.setDescription(preDescription);
		approval.setActionType(preAction);
		approval.setStatus(preStatus);
		approval.setReason(preReason);

		assertEquals(preId, approval.getId());
		assertEquals(preStatus, approval.getStatus());
		assertEquals(preReason, approval.getReason());

		String payload = "{ \"userId\" : 100 }";
		Integer preUmApprovalId = 430;
		umApprovalUser.setId(preUmApprovalId);
		umApprovalUser.setApprovalId(preId);
		umApprovalUser.setPayload(payload);

		assertEquals(preUmApprovalId, umApprovalUser.getId());
		assertEquals(payload, umApprovalUser.getPayload());

		Integer preUserId = 888;
		String preUserStatus = "A";
		user.setId(preUserId);
		user.setUserStatusId(preUserStatus);

		assertEquals(preUserId, user.getId());
		assertEquals(preUserStatus, user.getUserStatusId());

		Integer preUserGroupId = 999;
		String preUserGroupStatus = "P";
		userUserGroup.setStatus(preUserGroupStatus);

		userUsergroupList.add(userUserGroup);
		assertEquals(preUserGroupStatus, userUserGroup.getStatus());

	}

	@Test
	public void userDeleteApprovalTest() {
		logger.debug("userDeleteApprovalTest");

		Integer preId = 1;
		int preCreatorId = 777;
		String preReason = "";
		
		when(approvalRepositoryMock.findOne(Mockito.anyInt())).thenReturn(approval);
		when(umApprovalUserRepositoryMock.findOneByApprovalId(preId)).thenReturn(umApprovalUser);
		when(userRepositoryMock.findOne(100)).thenReturn(user);
		when(userRepositoryMock.findOne(preCreatorId)).thenReturn(user);
		when(userUserGroupRepoMock.findAllByUserId(100)).thenReturn(userUsergroupList);

		UserDeleteApprovalResponseVo responseVo = (UserDeleteApprovalResponseVo) workflowUserDeleteService.userDeleteApproval(preId, preReason);

		assertEquals(preId.intValue(), responseVo.getApprovalId());
	}

	@Test(expected = CommonException.class)
	public void userDeleteApprovalTest_NoApprovalById() {
		logger.debug("userDeleteApprovalTest_NoApprovalById");

		when(approvalRepositoryMock.findOne(Mockito.anyInt())).thenReturn(null);

		UserDeleteApprovalResponseVo responseVo = (UserDeleteApprovalResponseVo) workflowUserDeleteService.userDeleteApproval(123, "");
	}

	@Test(expected = CommonException.class)
	public void userDeleteApprovalTest_NoUmApprovalId() {
		logger.debug("userDeleteApprovalTest_NoUmApprovalId");

		Integer preId = 1;
		String preStatus = "P";
		String preReason = "";

		when(approvalRepositoryMock.findOne(Mockito.anyInt())).thenReturn(approval);
		when(umApprovalUserRepositoryMock.findOneByApprovalId(preId)).thenReturn(null);

		UserDeleteApprovalResponseVo responseVo = (UserDeleteApprovalResponseVo) workflowUserDeleteService.userDeleteApproval(preId, preReason);
	}

	@Test(expected = CommonException.class)
	public void userDeleteApprovalTest_NoUser() {
		logger.debug("userDeleteApprovalTest");

		Integer preId = 1;
		int preCreatorId = 777;
		String preReason = "";

		when(approvalRepositoryMock.findOne(Mockito.anyInt())).thenReturn(approval);
		when(umApprovalUserRepositoryMock.findOneByApprovalId(preId)).thenReturn(umApprovalUser);
		when(userRepositoryMock.findOne(100)).thenReturn(null);

		UserDeleteApprovalResponseVo responseVo = (UserDeleteApprovalResponseVo) workflowUserDeleteService.userDeleteApproval(preId, preReason);
	}

	@Test(expected = CommonException.class)
	public void userDeleteApprovalTest_NoUserGroup() {
		logger.debug("userDeleteApprovalTest_NoUserGroup");

		Integer preId = 1;
		int preCreatorId = 777;
		String preReason = "";

		when(approvalRepositoryMock.findOne(Mockito.anyInt())).thenReturn(approval);
		when(umApprovalUserRepositoryMock.findOneByApprovalId(preId)).thenReturn(umApprovalUser);
		when(userRepositoryMock.findOne(100)).thenReturn(user);
		when(userUserGroupRepoMock.findAllByUserId(100)).thenReturn(new ArrayList<UserUsergroup>());

		UserDeleteApprovalResponseVo responseVo = (UserDeleteApprovalResponseVo) workflowUserDeleteService.userDeleteApproval(preId, preReason);
	}

	@Test(expected = CommonException.class)
	public void userDeleteApprovalTest_NoCreator() {
		logger.debug("userDeleteApprovalTest");

		Integer preId = 1;
		int preCreatorId = 777;
		String preReason = "";

		when(approvalRepositoryMock.findOne(Mockito.anyInt())).thenReturn(approval);
		when(umApprovalUserRepositoryMock.findOneByApprovalId(preId)).thenReturn(umApprovalUser);
		when(userRepositoryMock.findOne(100)).thenReturn(user);
		when(userUserGroupRepoMock.findAllByUserId(100)).thenReturn(userUsergroupList);
		when(userRepositoryMock.findOne(preCreatorId)).thenReturn(null);

		UserDeleteApprovalResponseVo responseVo = (UserDeleteApprovalResponseVo) workflowUserDeleteService.userDeleteApproval(preId, preReason);
	}
}
