package com.rhbgroup.dcpbo.user.workflow.user;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WFUserGroupValue {

	private List<String> groupName;

	public WFUserGroupValue(List<String> groupName) {
		super();
		this.groupName = groupName;
	}

}
