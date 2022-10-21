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
public class WFUserApprovalDetailUserGroupValue implements BoData {
	private WFUserGroupValue before;
	private WFUserGroupValue after;

	public WFUserApprovalDetailUserGroupValue(WFUserGroupValue before, WFUserGroupValue after) {
		super();
		this.before = before;
		this.after = after;
	}
}
