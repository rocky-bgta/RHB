package com.rhbgroup.dcpbo.user.workflow.user;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.rhbgroup.dcpbo.user.common.BoData;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
@JsonInclude
public class WFUserApprovalDetail implements BoData {
	private Integer approvalId;
	private WFUserApprovalDetailValue username;
	private WFUserApprovalDetailValue email;
	private WFUserApprovalDetailValue name;
	private WFUserApprovalDetailValue departmentName;
	private WFUserApprovalDetailValue status;
	private WFUserApprovalDetailUserGroupValue usergroup;
	private String actionType;
	private String creatorName;
	private String createdTime;
	private String updatedBy;
	private String updatedTime;
	private String reason;
	private String isCreator;
	private String approvalStatus;
}
