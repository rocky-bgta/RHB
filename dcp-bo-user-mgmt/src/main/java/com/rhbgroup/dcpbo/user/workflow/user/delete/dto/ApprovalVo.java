package com.rhbgroup.dcpbo.user.workflow.user.delete.dto;

import com.rhbgroup.dcpbo.user.common.BoData;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class ApprovalVo implements BoData {
	private int id;
	private int functionId;
	private int creatorId;
	private String description;
	private String actionType;
	private String status;
	private String reason;
	private String scopeId;
}
