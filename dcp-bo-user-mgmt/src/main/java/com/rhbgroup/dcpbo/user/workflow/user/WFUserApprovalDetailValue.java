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
public class WFUserApprovalDetailValue implements BoData {
	public WFUserApprovalDetailValue(String before, String after) {
		super();
		this.before = before;
		this.after = after;
	}

	private String before;
	private String after;
}
