package com.rhbgroup.dcpbo.user.workflow.rejection;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.transaction.Transactional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rhbgroup.dcpbo.common.audit.AdditionalDataHolder;
import com.rhbgroup.dcpbo.common.exception.CommonException;
import com.rhbgroup.dcpbo.user.common.ApprovalFundDetailRepository;
import com.rhbgroup.dcpbo.user.common.ApprovalRepository;
import com.rhbgroup.dcpbo.user.common.BoAuditEventConfigRepository;
import com.rhbgroup.dcpbo.user.common.FundDetailsRepository;
import com.rhbgroup.dcpbo.user.common.UserRepository;
import com.rhbgroup.dcpbo.user.common.model.bo.ApprovalFundDetail;
import com.rhbgroup.dcpbo.user.enums.ApprovalStatus;
import com.rhbgroup.dcpbo.user.info.model.bo.User;
import com.rhbgroup.dcpbo.user.workflow.user.delete.dto.FundVo;

@Transactional
public class WorkflowRejectionService {

	private static final String AMOUNT = "Amount";
	private static final String PERCENTAGE = "Percentage";

	public static final String JSON_PARSE_EXCEPTION="Json Parse Exception  ";
	SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMMM yyyy hh.mm aa");
	
	private static final String TIMESTAMP_FORMAT = "yyyy-MM-dd'T'HH:mm:ssXXX";

	private static final String STRING_DATE_REPLACEMENT = "+";

	private static final String STRING_DATE_PLUS = "%2B";

	private static DateTimeFormatter dateTimeformatter = DateTimeFormatter.ofPattern(TIMESTAMP_FORMAT);

	
	@Autowired
	ApprovalRepository approvalRepository;
	@Autowired
	AdditionalDataHolder additionalDataHolder;
	@Autowired
	BoAuditEventConfigRepository boAuditEventConfigRepository;
	@Autowired
	UserRepository userRepository;
	
	@Autowired
	FundDetailsRepository fundDetailsRepository;
	
	@Autowired
	ApprovalFundDetailRepository approvalFundDetailRepository;

	ObjectMapper mapper = new ObjectMapper();

	private static Logger logger = LogManager.getLogger(WorkflowRejectionService.class);

	public WorkflowRejection putWorkflowRejectionService(int approvalId, RejectReasonRequestBody rejectReason, Integer userId) {
		WorkflowRejection workflowRejection = new WorkflowRejection();
		String reason = String.valueOf(rejectReason.getRejectReason());
		String id = "", functionId="0", actionType="",description ="",eventCode="";
		Integer functionIdInt = 0;
		Timestamp updatedTime = new Timestamp(new Date().getTime());

		User user = userRepository.findById(userId);

		List<Object[]> auditDetails= approvalRepository.updateStatusByIdOutput(ApprovalStatus.REJECTED.getValue(),approvalId, reason, updatedTime, user.getUsername());

		if (auditDetails.size() == 0)
			throw new CommonException(CommonException.GENERIC_ERROR_CODE,"Approval not found with id " + approvalId);
		for (Object[] details : auditDetails ){
			functionId = String.valueOf(details[0]);
			actionType = String.valueOf(details[1]);
			description = String.valueOf(details[2]);
		}
		workflowRejection.setApprovalId(String.valueOf(approvalId));
		functionIdInt = Integer.parseInt(functionId);
		eventCode = boAuditEventConfigRepository.findEventCodeByFunctionIdAndActionType(functionIdInt,actionType);

		
		List<ApprovalFundDetail> approvalFundDetails = approvalFundDetailRepository.findByApprovalId(approvalId);
	
		if(approvalFundDetails.isEmpty()) {
			return workflowRejection;
		}
		
		ApprovalFundDetail approvalFundDetail = approvalFundDetails.get(0);
		String payload = approvalFundDetail.getPayload();
		if (payload == null) {
			String errMsg = "Null value for payload for approvalFundDetail where id = "
					+ approvalFundDetail.getId();
			logger.error(errMsg);
			throw new CommonException(CommonException.GENERIC_ERROR_CODE, errMsg);
		}
		logger.debug("    payload: {}" , payload);

		FundVo approvalFund = null;
		try {
			approvalFund = mapper.readValue(approvalFundDetail.getPayload(),
					FundVo.class);
		} catch (Exception e) {
			throw new CommonException(CommonException.GENERIC_ERROR_CODE,
					JSON_PARSE_EXCEPTION + e);
		} 
		logger.debug("    approvalFund: {}" , approvalFund);
		
		
		
		HashMap<String, Object> additionalData = new HashMap<>();
		
		additionalData.put("after",auditAdditionalData(approvalFund));
		
		
		additionalData.put("eventCode", eventCode);
		additionalData.put("description", description);
		additionalData.put("actionType", actionType);
		additionalData.put("functionId", functionId);
		additionalData.put("payload", workflowRejection);

		additionalData.put("boRefNumber", approvalId);
		additionalData.put("module", "User Management");
		additionalData.put("reason", rejectReason);

		additionalDataHolder.setMap(additionalData);
		return workflowRejection;
	}
	
	private HashMap<String, Object> auditAdditionalData(FundVo fundVo) {
		
		// Audit additional data - before/after
		HashMap<String, Object> additionalSubDataMap = new HashMap<>();
		additionalSubDataMap.put("id", fundVo.getId());
		additionalSubDataMap.put("fundCode", fundVo.getFundId());
		additionalSubDataMap.put("fundLongName", fundVo.getFundLongName());
		additionalSubDataMap.put("fundShortName", fundVo.getFundShortName());
		additionalSubDataMap.put("fundType", fundVo.getFundType());
		additionalSubDataMap.put("collectionAccountNumber", fundVo.getCollectionAccountNumber());
		String type = fundVo.getBankChargeAmount() != null ? AMOUNT : PERCENTAGE;
		additionalSubDataMap.put("bankChargeType", type);
		additionalSubDataMap.put("bankChargeValue", type.equalsIgnoreCase(AMOUNT) ? fundVo.getBankChargeAmount() : fundVo.getBankChargePercentage());

		if(fundVo.getSuspensionFromDateTime() != null && !fundVo.getSuspensionFromDateTime().equals("")) {
		String suspensionFromDateTime = simpleDateFormat.format(parseInputDateTime(fundVo.getSuspensionFromDateTime()));
		additionalSubDataMap.put("suspensionFromDateTime", suspensionFromDateTime);
		}
		if(fundVo.getSuspensionToDateTime() != null && !fundVo.getSuspensionToDateTime().equals("")) {
		String suspensionToDateTime = simpleDateFormat.format(parseInputDateTime(fundVo.getSuspensionToDateTime()));
		additionalSubDataMap.put("suspensionToDateTime", suspensionToDateTime);

		}
		additionalSubDataMap.put("imageUrl", fundVo.getImageUrl());

	return additionalSubDataMap;

	}

	public static Timestamp parseInputDateTime(String dateStr) {

		LocalDateTime localDateTime = null;

		dateStr = dateStr.replace(STRING_DATE_PLUS, STRING_DATE_REPLACEMENT);

		if (dateStr != null && !dateStr.isEmpty()) {
			try {
				localDateTime = LocalDateTime.parse(dateStr, dateTimeformatter);
			} catch (DateTimeParseException e) {
				throw new CommonException(CommonException.GENERIC_ERROR_CODE, "Invalid datetime format: " + dateStr);
			}
		}

		return Timestamp.valueOf(localDateTime);
	}
	
	
	List<ApprovalFundDetail> retrieveApprovalFundDetailsValidate(Integer id) {

		List<ApprovalFundDetail> approvalFundDetails = approvalFundDetailRepository
				.findByApprovalId(id);

		if (approvalFundDetails == null || approvalFundDetails.isEmpty()) {
			String errMsg = "ApprovalFundDetail is not valid for approvalId: " + id;
			logger.error(errMsg);
			throw new CommonException(CommonException.GENERIC_ERROR_CODE, errMsg);
		}

		return approvalFundDetails;
	}
}