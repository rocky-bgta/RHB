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
public class WFUserGroupDetailFunctionValue implements BoData {
	private WFFunctionValue before;
	private WFFunctionValue after;

	public WFUserGroupDetailFunctionValue(WFFunctionValue before, WFFunctionValue after) {
		super();
		this.before = before;
		this.after = after;
	}
}
