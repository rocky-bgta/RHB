package com.rhbgroup.dcpbo.user.workflow.function.device;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rhbgroup.dcpbo.common.exception.CommonException;
import com.rhbgroup.dcpbo.user.common.ApprovalDeviceRepository;
import com.rhbgroup.dcpbo.user.common.ApprovalRepository;
import com.rhbgroup.dcpbo.user.common.BoData;
import com.rhbgroup.dcpbo.user.common.DeviceProfileRepository;
import com.rhbgroup.dcpbo.user.common.UserProfileRepository;
import com.rhbgroup.dcpbo.user.common.UserRepository;
import com.rhbgroup.dcpbo.user.common.model.bo.Approval;
import com.rhbgroup.dcpbo.user.common.model.bo.ApprovalDevice;
import com.rhbgroup.dcpbo.user.common.model.dcp.DeviceProfile;
import com.rhbgroup.dcpbo.user.common.model.dcp.UserProfile;
import com.rhbgroup.dcpbo.user.info.model.bo.User;

public class WorkflowFunctionDeviceApprovalServiceImpl implements WorkflowFunctionDeviceApprovalService {

	public static final String STATE_BEFORE = "B";
	public static final String STATE_AFTER = "A";
	public static final String APPROVAL_STATUS_APPROVED = "A";
	public static final String APPROVAL_STATUS_REJECTED = "R";
	public static final String REQUEST_APPROVED = "APPROVED";
	public static final String REQUEST_REJECTED = "REJECTED";

	private ApprovalRepository approvalRepository;
	private ApprovalDeviceRepository approvalDeviceRepository;
	private UserRepository userRepository;
	private DeviceProfileRepository deviceProfileRepository;
	private UserProfileRepository userProfileRepository;

	private static Logger logger = LogManager.getLogger(WorkflowFunctionDeviceApprovalServiceImpl.class);

	public WorkflowFunctionDeviceApprovalServiceImpl(ApprovalRepository approvalRepository,
			ApprovalDeviceRepository approvalDeviceRepository, UserRepository userRepository,
			DeviceProfileRepository deviceProfileRepository, UserProfileRepository userProfileRepository) {
		this.approvalRepository = approvalRepository;
		this.approvalDeviceRepository = approvalDeviceRepository;
		this.userRepository = userRepository;
		this.deviceProfileRepository = deviceProfileRepository;
		this.userProfileRepository = userProfileRepository;
	}

	@Override
	public BoData getDeviceApproval(Integer approvalId, Integer requestUserId) {
		logger.debug("getDeviceApproval()");
		logger.debug("    approvalRepository: " + approvalRepository);
		logger.debug("    approvalDeviceRepository: " + approvalDeviceRepository);
		logger.debug("    userRepository: " + userRepository);
		logger.debug("    deviceProfileRepository: " + deviceProfileRepository);
		logger.debug("    userProfileRepository: " + userProfileRepository);

		Approval approval = approvalRepository.getOne(approvalId);
		if (approval == null)
			throw new CommonException(CommonException.GENERIC_ERROR_CODE, "Cannot find Approval for approvalId: " + approvalId,
					HttpStatus.INTERNAL_SERVER_ERROR);
		logger.debug("    approval: " + approval);

		WorkflowFunctionDeviceApproval workflowFunctionDeviceApproval = new WorkflowFunctionDeviceApproval();
		workflowFunctionDeviceApproval.setApprovalId(approvalId);
		workflowFunctionDeviceApproval.setActionType(approval.getActionType());
		workflowFunctionDeviceApproval.setCreatedTime(formatTimestamp(approval.getCreatedTime()));
		workflowFunctionDeviceApproval.setReason(approval.getReason());

		if(approval.getCreatorId() == requestUserId) workflowFunctionDeviceApproval.setIsCreator("Y");
		else workflowFunctionDeviceApproval.setIsCreator("N");

		int creatorId = approval.getCreatorId();
		logger.debug("    creatorId: " + creatorId);

		User creator = userRepository.getOne(creatorId);
		if (creator == null)
			throw new CommonException(CommonException.GENERIC_ERROR_CODE, "Cannot find User for creatorId: " + creatorId,
					HttpStatus.INTERNAL_SERVER_ERROR);
		logger.debug("    creator: " + creator);

		workflowFunctionDeviceApproval.setCreatedByName(creator.getName());
		workflowFunctionDeviceApproval.setUpdatedBy(approval.getUpdatedBy());
		workflowFunctionDeviceApproval.setUpdatedTime(approval.getUpdatedTime().toString());
		if(approval.getStatus().equals(APPROVAL_STATUS_APPROVED)) workflowFunctionDeviceApproval.setApprovalStatus(REQUEST_APPROVED);
		else if (approval.getStatus().equals(APPROVAL_STATUS_REJECTED)) workflowFunctionDeviceApproval.setApprovalStatus(REQUEST_REJECTED);

		ApprovalDevice approvalDevice = approvalDeviceRepository.findByApprovalId(approvalId);
		if (approvalDevice == null)
			throw new CommonException(CommonException.GENERIC_ERROR_CODE, "Cannot find ApprovalDevice for approvalId: " + approvalId,
					HttpStatus.INTERNAL_SERVER_ERROR);
		logger.debug("    approvalDevice: " + approvalDevice);

		if ("DELETE".equalsIgnoreCase(approval.getActionType())) {
			String payload = approvalDevice.getPayload();
			logger.debug("    payload: " + payload);
			
			ObjectMapper objectMapper = new ObjectMapper();
			ApprovalDevicePayload approvalDevicePayload = null;
			try {
				approvalDevicePayload = objectMapper.readValue(payload, ApprovalDevicePayload.class);
			} catch (Exception e) {
				throw new CommonException(CommonException.GENERIC_ERROR_CODE, "Exception " + e + " caught when parsing payload: " + payload,
						HttpStatus.INTERNAL_SERVER_ERROR);
			}
			logger.debug("    approvalDevicePayload: " + approvalDevicePayload);
			
			Integer deviceId = approvalDevicePayload.getDeviceId();
			logger.debug("    deviceId: " + deviceId);
			
			DeviceProfile deviceProfile = deviceProfileRepository.findById(deviceId);
			if (deviceProfile == null)
				throw new CommonException(CommonException.GENERIC_ERROR_CODE,
						"Cannot find DeviceProfile for deviceId: " + deviceId + ", approvalId: " + approvalId,
						HttpStatus.INTERNAL_SERVER_ERROR);
			logger.debug("    deviceProfile: " + deviceProfile);
			
			workflowFunctionDeviceApproval.setDeviceId(deviceId);
			workflowFunctionDeviceApproval.setDeviceName(deviceProfile.getDeviceName());
			workflowFunctionDeviceApproval.setOs(deviceProfile.getOs());
			workflowFunctionDeviceApproval.setLastSigned(formatTimestamp(deviceProfile.getLastLogin()));
			workflowFunctionDeviceApproval.setRegistered(formatTimestamp(deviceProfile.getCreatedTime()));
			workflowFunctionDeviceApproval.setCreatedTime(formatTimestamp(approval.getCreatedTime()));

			int userId = deviceProfile.getUserId();
			logger.debug("    userId: " + userId);

			UserProfile userProfile = userProfileRepository.findOne(userId);
			if (userProfile == null)
				throw new CommonException(CommonException.GENERIC_ERROR_CODE,
						"Cannot find UserProfile for userId: " + userId + ", approvalId: " + approvalId,
						HttpStatus.INTERNAL_SERVER_ERROR);
			logger.debug("    userProfile: " + userProfile);

            workflowFunctionDeviceApproval.setIdNo(userProfile.getIdNo());
            workflowFunctionDeviceApproval.setUsername(userProfile.getName());

			if (userProfile.getTxnSigningDevice() == deviceProfile.getId())
				workflowFunctionDeviceApproval.setPrimaryDevice("True");
			else
				workflowFunctionDeviceApproval.setPrimaryDevice("False");
		}

		return workflowFunctionDeviceApproval;
	}

	public static final String TIMESTAMP_FORMAT  = "yyyy-MM-dd'T'HH:mm:ssXXX";

	private String formatTimestamp(Timestamp timestamp) {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(TIMESTAMP_FORMAT);
		return simpleDateFormat.format(timestamp);
	}

	private String formatTimestamp(Date date) {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(TIMESTAMP_FORMAT);
		return simpleDateFormat.format(date);
	}
}
