package com.rhbgroup.dcpbo.user.workflow.user.delete;

import com.rhbgroup.dcp.util.JsonUtil;
import com.rhbgroup.dcpbo.common.audit.AdditionalDataHolder;
import com.rhbgroup.dcpbo.common.exception.CommonException;
import com.rhbgroup.dcpbo.user.common.*;
import com.rhbgroup.dcpbo.user.common.model.bo.Approval;
import com.rhbgroup.dcpbo.user.common.model.bo.UmApprovalUser;
import com.rhbgroup.dcpbo.user.common.model.bo.UserUsergroup;
import com.rhbgroup.dcpbo.user.common.UserUsergroupRepository;
import com.rhbgroup.dcpbo.user.info.model.bo.User;
import com.rhbgroup.dcpbo.user.workflow.user.WFUserPayload;
import com.rhbgroup.dcpbo.user.workflow.user.delete.dto.ApprovalVo;
import com.rhbgroup.dcpbo.user.workflow.user.delete.dto.UserDeleteApprovalResponseVo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class WorkflowUserDeleteServiceImpl implements WorkflowUserDeleteService {
	private static Logger logger = LogManager.getLogger(WorkflowUserDeleteServiceImpl.class);

	@Autowired
	ApprovalRepository approvalRepository;

	@Autowired
	UmApprovalUserRepository umApprovalUserRepository;

	@Autowired
	UserRepository userRepository;

	@Autowired
	UserUsergroupRepository userUserGroupRepo;

	@Autowired
	AdditionalDataHolder additionalDataHolder;

	@Override
	public BoData userDeleteApproval(int id, String reason) {
		Timestamp actionTime = new Timestamp(new Date().getTime());
		Approval approval = approvalRepository.findOne(id);

		if (approval == null)
			throw new CommonException(CommonException.GENERIC_ERROR_CODE, "Approval ID does not exist in 'Approval'.", HttpStatus.INTERNAL_SERVER_ERROR);

		approval.setReason(reason);
		approval.setStatus("A");
		approval.setUpdatedTime(actionTime);
		approvalRepository.save(approval);

		ApprovalVo approvalVo = new ApprovalVo();
		approvalVo.setId(approval.getId());
		approvalVo.setFunctionId(approval.getFunctionId());
		approvalVo.setCreatorId(approval.getCreatorId());
		approvalVo.setDescription(approval.getDescription());
		approvalVo.setActionType(approval.getActionType());
		approvalVo.setStatus(approval.getStatus());
		approvalVo.setReason(approval.getReason());
		logger.info(String.format("Retrieved approval: %s", approvalVo.toString()));

		UmApprovalUser umApprovalUser = umApprovalUserRepository.findOneByApprovalId(approvalVo.getId());

		if (umApprovalUser == null)
			throw new CommonException(CommonException.GENERIC_ERROR_CODE, "Approval ID does not exist in 'UM Approval User'.", HttpStatus.INTERNAL_SERVER_ERROR);

		WFUserPayload payload = JsonUtil.jsonToObject(umApprovalUser.getPayload(), WFUserPayload.class);

		logger.info(String.format("Retrieved userID from payload: %s", payload.getUserId()));

		User user = userRepository.findOne(payload.getUserId());

		if (user == null)
			throw new CommonException(CommonException.GENERIC_ERROR_CODE, "User does not exist.", HttpStatus.INTERNAL_SERVER_ERROR);

		user.setUserStatusId("D");
		user.setUpdatedTime(actionTime);
		userRepository.save(user);


		List<UserUsergroup> userUserGroupList = userUserGroupRepo.findAllByUserId(payload.getUserId());

		if (userUserGroupList.isEmpty())
			throw new CommonException(CommonException.GENERIC_ERROR_CODE, "User Group does not exist.", HttpStatus.INTERNAL_SERVER_ERROR);

		for(UserUsergroup userUsergroup : userUserGroupList)
		{
			userUsergroup.setStatus("D");
			userUsergroup.setUpdatedTime(actionTime);
			userUserGroupRepo.save(userUsergroup);
		}
		User creator = userRepository.findOne(approvalVo.getCreatorId());

		if (creator == null)
			throw new CommonException(CommonException.GENERIC_ERROR_CODE, "Creator User does not exist.", HttpStatus.INTERNAL_SERVER_ERROR);

		String email = creator.getEmail();
		// TODO: Email.

		UserDeleteApprovalResponseVo response = new UserDeleteApprovalResponseVo();
		response.setApprovalId(id);


		//Populate additional Data for auditing
		HashMap<String, Object> additionalData = new HashMap<>();

		HashMap<String, Object> approvalMap = new HashMap<>();
		approvalMap.put("functionId", approvalVo.getFunctionId());

		HashMap<String, Object> userMap = new HashMap<>();
		userMap.put("userid", payload.getUserId());
		userMap.put("username", payload.getUsername());
		userMap.put("name", payload.getName());
		userMap.put("email", payload.getEmail());
		userMap.put("department", payload.getDepartment());
		userMap.put("usergroup", payload.getGroup());

		userMap.put("boRefNumber", approvalVo.getId());
		userMap.put("usergroupName", payload.getGroup());
		userMap.put("module", "User Management");
		userMap.put("reason", reason);

		additionalDataHolder.setMap(userMap);
		logger.debug("audit log data : {}", JsonUtil.objectToJson(additionalDataHolder.getMap()));

		return response;
	}
}
