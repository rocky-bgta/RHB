package com.rhbgroup.dcpbo.user.workflow.usergroup;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.rhbgroup.dcpbo.user.common.BoData;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
@JsonInclude
public class WFUserGroupApprovalDetail implements BoData {
	private Integer approvalId;
	private WFUserGroupApprovalDetailValue groupName;
	private WFUserGroupApprovalDetailValue accessType;
	private WFUserGroupDetailFunctionValue function;
	private String actionType;
	private String creatorName;
	private String createdTime;
	private String updatedBy;
	private String updatedTime;
	private String reason;
	private String isCreator;
	private String approvalStatus;
}
